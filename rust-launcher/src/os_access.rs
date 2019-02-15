use std;
use dirs_paths_helper;
use std::env;
use std::fmt::Write;

pub fn create_java_cmd(os: &Os,jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Command {
    let mut bin_java = jre_dir.clone();
    bin_java.push("bin");
    bin_java.push("java");
    let mut cmd = std::process::Command::new(&bin_java);
    for ar in args.into_iter() {
        cmd.arg(ar);
    }
    let mut info = String::new();
    write!(&mut info, "itw-rust-debug: command {}", format!("{:?}", cmd)).expect("unwrap failed");
    os.log(&info);
    return cmd;
}

fn spawn_java_process(os: &Os, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child {
    let mut cmd = create_java_cmd(os, jre_dir, args);
    cmd.stdin(std::process::Stdio::inherit());
    cmd.stdout(std::process::Stdio::inherit());
    cmd.stderr(std::process::Stdio::inherit());
    let res = cmd.spawn();
    match res {
        Ok(child) => child,
        Err(_) => panic!("Error spawning JVM process, \
                 java executable: [{}], arguments: [{:?}]", jre_dir.clone().into_os_string().to_str().expect("path should unwrap"), args)
    }
}

pub trait Os {
    // logging "api" can change
    fn log(&self, s: &str);
    fn info(&self, s: &str);
    fn get_registry_jdk(&self) -> Option<std::path::PathBuf>;
    // next to system and home cfg dir, there is also by-jre config dir, but that do not need to be handled os-specific way
    // https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/jcp/properties.html
    fn get_system_config_javadir(&self) -> Option<std::path::PathBuf>;
    fn get_user_config_dir(&self) -> Option<std::path::PathBuf>;
    // is valid  only on linux, otherwise returns get_system_config_javadir
    fn get_legacy_system_config_javadir(&self) -> Option<std::path::PathBuf>;
    // is valid  only on linux, otherwise returns get_user_config_dir
    fn get_legacy_user_config_dir(&self) -> Option<std::path::PathBuf>;
    fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child;
    // should probe HOME on linux and USERPROFILE on windows.
    // it should have fallback in env::home_dir as it is doing a bit more
    // see https://doc.rust-lang.org/std/env/fn.home_dir.html
    fn get_home(&self) -> Option<std::path::PathBuf>;
    fn get_classpath_separator(&self) -> char;
    fn get_exec_suffixes(&self) -> &'static [&'static str];
}

#[cfg(not(windows))]
pub struct Linux {
    verbose: bool,
}

#[cfg(not(windows))]
impl Linux {
    pub fn new(debug: bool) -> Linux {
        Linux { verbose: debug }
    }
}

#[cfg(not(windows))]
impl Os for Linux {
    fn log(&self, s: &str) {
        if self.verbose {
            println!("{}", s);
        }
    }

    fn info(&self, s: &str) {
        println!("{}", s);
    }

    fn get_registry_jdk(&self) -> Option<std::path::PathBuf> {
        None
    }

    fn get_system_config_javadir(&self) -> Option<std::path::PathBuf> {
        let path = std::path::PathBuf::from("/etc/.java/deployment");
        Some(path)
    }

    fn get_user_config_dir(&self) -> Option<std::path::PathBuf> {
        match dirs_paths_helper::get_xdg_config_dir(self) {
            Some(mut p) => {
                p.push(dirs_paths_helper::ICEDTEA_WEB);
                Some(p)
            }
            None => None
        }
    }

    fn get_legacy_system_config_javadir(&self) -> Option<std::path::PathBuf> {
        let path = std::path::PathBuf::from("/etc/.java/.deploy");
        Some(path)
    }

    fn get_legacy_user_config_dir(&self) -> Option<std::path::PathBuf> {
        match self.get_home() {
            Some(mut p) => {
                p.push(".icedtea");
                Some(p)
            }
            None => None
        }
    }

    fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child {
        spawn_java_process(self, jre_dir, args)
    }

    fn get_home(&self) -> Option<std::path::PathBuf> {
        match env::var("HOME") {
            Ok(war) => {
                let home_var_path = std::path::PathBuf::from(war);
                if dirs_paths_helper::is_dir(&home_var_path) {
                    return Some(home_var_path);
                }
            }
            Err(_) => {}
        }
        // Not failing to env::get_home
        // if this will ever be bugged, the fix should be to set HOME
        // locally, or fix the distribution itslef
        None
    }

    fn get_classpath_separator(&self) -> char {
         ':'
    }

    //on linux, java is known to be compiled witout any suffix, on windows, it should be .exe
    fn get_exec_suffixes(&self) -> &'static [&'static str] {
        &[""]
    }
}


#[cfg(windows)]
pub struct Windows {
    verbose: bool,
}

#[cfg(windows)]
impl Windows {
    pub fn new(debug: bool) -> Windows {
        Windows { verbose: debug }
    }

}

#[cfg(windows)]
impl Os for Windows {
    fn log(&self, s: &str) {
        if self.verbose {
            println!("{}", s);
        }
    }

    fn info(&self, s: &str) {
        println!("{}", s);
    }

    fn get_registry_jdk(&self) -> Option<std::path::PathBuf> {
        std::panic::catch_unwind(|| {
            let path = win::jdk_registry_path();
            Some(std::path::PathBuf::from(path))
        }).unwrap_or_else(|_e| {
            // show_error_message(errloc_msg(&e));
            None
        })
    }

    fn get_system_config_javadir(&self) -> Option<std::path::PathBuf> {
        None
    }

    fn get_user_config_dir(&self) -> Option<std::path::PathBuf> {
        match self.get_home() {
            Some(mut p) => {
                p.push(".config");
                p.push(dirs_paths_helper::ICEDTEA_WEB);
                Some(p)
            }
            None => None
        }
    }

    fn get_legacy_system_config_javadir(&self) -> Option<std::path::PathBuf> {
        None
    }

    fn get_legacy_user_config_dir(&self) -> Option<std::path::PathBuf> {
        None
    }

    fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child {
        //there was an intention, which caused all the os trait, to implement this better. However developer (alex) faield and gave up
        spawn_java_process(self, jre_dir, args)
    }

    fn get_home(&self) -> Option<std::path::PathBuf> {
        match env::var("USERPROFILE") {
            Ok(war) => {
                let home_var_path = std::path::PathBuf::from(war);
                if dirs_paths_helper::is_dir(&home_var_path) {
                    return Some(home_var_path);
                }
            }
            Err(_) => {}
        }
        None
    }

    fn get_classpath_separator(&self) -> char {
         ';'
    }

    //on linux, java is known to be compiled witout any suffix, on windows, it should be .exe
    fn get_exec_suffixes(&self) -> &'static [&'static str] {
        &[".exe"]
    }
}


#[cfg(windows)]
#[allow(non_snake_case)]
#[allow(non_camel_case_types)]
mod win {
    // https://crates.io/crates/scopeguard
    macro_rules! defer {
        ($e:expr) => {
            let _deferred = ScopeGuard::new((), |_| $e);
        }
    }

    pub struct ScopeGuard<T, F> where F: FnMut(&mut T) {
        __dropfn: F,
        __value: T
    }

    impl<T, F> ScopeGuard<T, F> where F: FnMut(&mut T) {
        pub fn new(v: T, dropfn: F) -> ScopeGuard<T, F> {
            ScopeGuard {
                __value: v,
                __dropfn: dropfn
            }
        }
    }

    impl<T, F> Drop for ScopeGuard<T, F> where F: FnMut(&mut T) {
        fn drop(&mut self) {
            (self.__dropfn)(&mut self.__value)
        }
    }

    // https://crates.io/crates/errloc_macros
    macro_rules! errloc {
        () => {
            concat!(file!(), ':', line!())
        }
    }

    fn errloc_msg<'a>(e: &'a Box<std::any::Any + Send + 'static>) -> &'a str {
        match e.downcast_ref::<&str>() {
            Some(st) => st,
            None => {
                match e.downcast_ref::<String>() {
                    Some(stw) => stw.as_str(),
                    None => "()",
                }
            },
        }
    }

    // implementation

    use std;
    use std::os::raw::*;
    use std::ptr::{null, null_mut};

    // constants
    const CP_UTF8: c_ulong = 65001;
    const FORMAT_MESSAGE_ALLOCATE_BUFFER: c_ulong = 0x00000100;
    const FORMAT_MESSAGE_FROM_SYSTEM: c_ulong = 0x00001000;
    const FORMAT_MESSAGE_IGNORE_INSERTS: c_ulong = 0x00000200;
    const LANG_NEUTRAL: c_ushort = 0x00;
    const SUBLANG_DEFAULT: c_ushort = 0x01;
    const ERROR_SUCCESS: c_ulong = 0;
    const READ_CONTROL: c_ulong = 0x00020000;
    const STANDARD_RIGHTS_READ: c_ulong = READ_CONTROL;
    const KEY_QUERY_VALUE: c_ulong = 0x0001;
    const KEY_ENUMERATE_SUB_KEYS: c_ulong = 0x0008;
    const KEY_NOTIFY: c_ulong = 0x0010;
    const SYNCHRONIZE: c_ulong = 0x00100000;
    const REG_SZ: c_ulong = 1;
    const KEY_READ: c_ulong = (
        STANDARD_RIGHTS_READ |
            KEY_QUERY_VALUE |
            KEY_ENUMERATE_SUB_KEYS |
            KEY_NOTIFY
    ) & (!SYNCHRONIZE);
    const HKEY_LOCAL_MACHINE: *mut c_void = 0x80000002 as *mut c_void;

    // function declarations

    extern "system" {
        fn MultiByteToWideChar(
            CodePage: c_uint,
            dwFlags: c_ulong,
            lpMultiByteStr: *const c_char,
            cbMultiByte: c_int,
            lpWideCharStr: *mut c_ushort,
            cchWideChar: c_int
        ) -> c_int;

        fn WideCharToMultiByte(
            CodePage: c_uint,
            dwFlags: c_ulong,
            lpWideCharStr: *const c_ushort,
            cchWideChar: c_int,
            lpMultiByteStr: *mut c_char,
            cbMultiByte: c_int,
            lpDefaultChar: *const c_char,
            lpUsedDefaultChar: *mut c_int
        ) -> c_int;

        fn GetLastError() -> c_ulong;

        fn FormatMessageW(
            dwFlags: c_ulong,
            lpSource: *const c_void,
            dwMessageId: c_ulong,
            dwLanguageId: c_ulong,
            lpBuffer: *mut c_ushort,
            nSize: c_ulong,
            Arguments: *mut *mut c_char
        ) -> c_ulong;

        fn LocalFree(
            hMem: *mut c_void
        ) -> *mut c_void;

        fn RegOpenKeyExW(
            hKey: *mut c_void,
            lpSubKey: *const c_ushort,
            ulOptions: c_ulong,
            samDesired: c_ulong,
            phkResult: *mut *mut c_void
        ) -> c_long;

        fn RegCloseKey(
            hKey: *mut c_void
        ) -> c_long;

        fn RegQueryValueExW(
            hKey: *mut c_void,
            lpValueName: *const c_ushort,
            lpReserved: *mut c_ulong,
            lpType: *mut c_ulong,
            lpData: *mut c_uchar,
            lpcbData: *mut c_ulong
        ) -> c_long;
    }

    // windows-specific utilities

    fn MAKELANGID(p: c_ushort, s: c_ushort) -> c_ushort {
        (s << 10 | p)
    }

    fn widen(st: &str) -> Vec<u16> {
        unsafe {
            let size_needed = MultiByteToWideChar(
                CP_UTF8,
                0,
                st.as_ptr() as *mut i8,
                st.len() as c_int,
                null_mut::<u16>(),
                0);
            if 0 == size_needed {
                panic!(format!("Error on string widen calculation, \
                string: [{}], error: [{}]", st, errcode_to_string(GetLastError())));
            }
            let mut res: Vec<u16> = Vec::new();
            res.resize((size_needed + 1) as usize, 0);
            let chars_copied = MultiByteToWideChar(
                CP_UTF8,
                0,
                st.as_ptr() as *mut i8,
                st.len() as c_int,
                res.as_mut_ptr(),
                size_needed);
            if chars_copied != size_needed {
                panic!(format!("Error on string widen execution, \
                string: [{}], error: [{}]", st, errcode_to_string(GetLastError())));
            }
            res.resize(size_needed as usize, 0);
            res
        }
    }

    fn narrow(wst: &[u16]) -> String {
        unsafe {
            let size_needed = WideCharToMultiByte(
                CP_UTF8,
                0,
                wst.as_ptr(),
                wst.len() as c_int,
                null_mut::<i8>(),
                0,
                null::<c_char>(),
                null_mut::<c_int>());
            if 0 == size_needed {
                panic!(format!("Error on string narrow calculation, \
                string length: [{}], error code: [{}]", wst.len(), GetLastError()));
            }
            let mut vec: Vec<u8> = Vec::new();
            vec.resize(size_needed as usize, 0);
            let bytes_copied = WideCharToMultiByte(
                CP_UTF8,
                0,
                wst.as_ptr(),
                wst.len() as c_int,
                vec.as_mut_ptr() as *mut i8,
                size_needed,
                null::<c_char>(),
                null_mut::<c_int>());
            if bytes_copied != size_needed {
                panic!(format!("Error on string narrow execution, \
                string length: [{}], error code: [{}]", vec.len(), GetLastError()));
            }
            String::from_utf8(vec).expect(errloc!())
        }
    }

    fn errcode_to_string(code: c_ulong) -> String {
        if 0 == code {
            return String::new();
        }
        unsafe {
            let mut buf: *mut u16 = null_mut::<u16>();
            let size = FormatMessageW(
                FORMAT_MESSAGE_ALLOCATE_BUFFER |
                    FORMAT_MESSAGE_FROM_SYSTEM |
                    FORMAT_MESSAGE_IGNORE_INSERTS,
                null::<c_void>(),
                code,
                MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT) as c_ulong,
                std::mem::transmute::<*mut *mut u16, *mut u16>(&mut buf),
                0,
                null_mut::<*mut c_char>());
            if 0 == size {
                return format!("Cannot format code: [{}] \
                 into message, error code: [{}]", code, GetLastError());
            }
            defer!({
                LocalFree(buf as *mut c_void);
            });
            if size <= 2 {
                return format!("code: [{}], message: []", code);
            }
            std::panic::catch_unwind(|| {
                let slice = std::slice::from_raw_parts(buf, (size - 2) as usize);
                let msg = narrow(slice);
                format!("code: [{}], message: [{}]", code, msg)
            }).unwrap_or_else(|e| {
                format!("Cannot format code: [{}] \
                 into message, narrow error: [{}]", code, errloc_msg(&e))
            })
        }
    }

    pub fn jdk_registry_path() -> String {
        let jdk_key_name = "SOFTWARE\\JavaSoft\\Java Development Kit\\1.8";
        let wjdk_key_name = widen(jdk_key_name);
        let java_home = "JavaHome";
        let wjava_home = widen("JavaHome");
        unsafe {
            // open root
            let mut jdk_key = null_mut::<c_void>();
            let err_jdk = RegOpenKeyExW(
                HKEY_LOCAL_MACHINE,
                wjdk_key_name.as_ptr(),
                0,
                KEY_READ | KEY_ENUMERATE_SUB_KEYS,
                &mut jdk_key) as u32;
            if ERROR_SUCCESS != err_jdk {
                panic!(format!("Error opening registry key, \
                    name: [{}], message: [{}]", jdk_key_name, errcode_to_string(err_jdk)));
            }
            defer!({
                RegCloseKey(jdk_key);
            });
            // find out value len
            let mut value_len: c_ulong = 0;
            let mut value_type: c_ulong = 0;
            let err_len = RegQueryValueExW(
                jdk_key,
                wjava_home.as_ptr(),
                null_mut::<c_ulong>(),
                &mut value_type,
                null_mut::<c_uchar>(),
                &mut value_len) as u32;
            if ERROR_SUCCESS != err_len || !(value_len > 0) || REG_SZ != value_type {
                panic!(format!("Error opening registry value len, \
                    key: [{}], value: [{}], message: [{}]", jdk_key_name, java_home, errcode_to_string(err_len)));
            }
            // get value
            let mut wvalue: Vec<u16> = Vec::new();
            wvalue.resize((value_len as usize) / std::mem::size_of::<u16>(), 0);
            let err_val = RegQueryValueExW(
                jdk_key,
                wjava_home.as_ptr(),
                null_mut::<c_ulong>(),
                null_mut::<c_ulong>(),
                wvalue.as_mut_ptr() as *mut c_uchar,
                &mut value_len) as u32;
            if ERROR_SUCCESS != err_val {
                panic!(format!("Error opening registry value, \
                    key: [{}], value: [{}], message: [{}]", jdk_key_name, java_home, errcode_to_string(err_val)));
            }
            // format and return path
            let slice = std::slice::from_raw_parts(wvalue.as_ptr(), wvalue.len() - 1 as usize);
            let jpath_badslash = narrow(slice);
            let mut jpath = jpath_badslash.replace("\\", "/");
            if '/' as u8 != jpath.as_bytes()[jpath.len() - 1] {
                jpath.push('/');
            }
            return jpath;
        }
    }


}
