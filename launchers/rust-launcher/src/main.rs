#![windows_subsystem = "windows"]

mod hardcoded_paths;
mod property_from_file;
mod os_access;
mod dirs_paths_helper;
mod property_from_files_resolver;
mod utils;
mod property;
mod jars_helper;
mod log_helper;

use std::string::String;
use std::fmt::Write;
use os_access::Os;
use std::env;

#[cfg(not(windows))]
fn get_os(debug: bool, al: bool) -> os_access::Linux {
    os_access::Linux::new(debug, al)
}

#[cfg(windows)]
fn get_os(debug: bool, al: bool, ic: bool) -> os_access::Windows {
    os_access::Windows::new(debug, al, ic)
}

fn is_debug_on() -> bool {
    match is_debug_on_testable(env::args().collect::<Vec<_>>()) {
        Some(val) => {
            return val;
        }
        _none => {
            #[cfg(not(windows))]
            let os = get_os(false, false);
            #[cfg(windows)]
            let os = get_os(false, false, true);
            return property_from_files_resolver::try_main_verbose_from_properties(&os);
        }
    }
}

fn is_debug_on_testable(aargs: Vec<String>) -> Option<bool> {
    for s in aargs {
        if clean_param(s) == ("-verbose") {
            return Some(true);
        }
    }
    None
}

fn is_headless_enforced() -> bool {
    is_headless_enforced_testable(env::args().collect::<Vec<_>>())
}

fn is_headless_enforced_testable(aargs: Vec<String>) -> bool {
    for s in aargs {
        if clean_param(s) == ("-headless") {
            return true;
        }
    }
    false
}

fn is_splash_forbidden() -> bool {
    is_splash_forbidden_testable(env::vars().collect::<Vec<_>>())
}

fn is_splash_forbidden_testable(vars: Vec<(String, String)>) -> bool {
    for (key, value) in vars {
        if key == "ICEDTEA_WEB_SPLASH" {
            if value.to_lowercase() == "true" {
                return false;
            }
            return true;
        }
    }
    false
}

fn main() {
    let os;
    #[cfg(windows)]
    {
        use os_access::win;
        let acr: i32;
        unsafe { acr = win::AttachConsole(win::ATTACH_PARENT_PROCESS) };
        os = get_os(is_debug_on(), true, acr != 0);
    }
    #[cfg(not(windows))]
    {
        os = get_os(is_debug_on(), true);
    }
    os.log(&dirs_paths_helper::path_to_string(&dirs_paths_helper::current_program()));
    let mut info1 = String::new();
    write!(&mut info1, "itw-rust-debug: itw mode: {}", hardcoded_paths::get_libsearch(&os)).expect("unwrap failed");
    os.log(&info1);
    let java_dir = utils::find_jre(&os);
    let mut info2 = String::new();
    write!(&mut info2, "selected jre: {}", java_dir.display()).expect("unwrap failed");
    os.info(&info2);

    let a = env::args();
    let s = a.skip(1);
    let c: std::vec::Vec<String> = s.collect();

    let mut child = os.spawn_java_process(&java_dir, &compose_arguments(&java_dir, &c, &os));
    let ecode = child.wait().expect("failed to wait on child");
    let code = ecode.code().expect("code should be always here");
    std::process::exit(code)
}

fn compose_arguments(java_dir: &std::path::PathBuf, original_args: &std::vec::Vec<String>, os: &os_access::Os) -> Vec<String> {
    let bootcp = jars_helper::get_bootclasspath(&java_dir, os);
    let cp = jars_helper::get_classpath(&java_dir, os);
    let current_name = dirs_paths_helper::current_program_name();
    let current_bin = dirs_paths_helper::current_program();
    let mut info2 = String::new();
    write!(&mut info2, "itw-rust-debug: used boot classpath: {}", bootcp).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: used classpath: {}", cp).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: current name: {}", current_name).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: current bin: {}", &dirs_paths_helper::path_to_string(&current_bin)).expect("unwrap failed");
    os.log(&info2);

    let mut bin_name = String::from("-Dicedtea-web.bin.name=");
    let mut bin_location = String::from("-Dicedtea-web.bin.location=");
    //no matter what ITW_LIBS are saying, imho using current pgm is always correct comapred to hardcoded values
    bin_name.push_str(&current_name);
    bin_location.push_str(&dirs_paths_helper::path_to_string(&current_bin));

    let mut all_args = std::vec::Vec::new();

    include_dashJs_values(&original_args, &mut all_args, os);

    match get_splash(os) {
        Some(switch) => {
            all_args.push(switch);
        }
        _none => {
            os.log("itw-rust-debug: splash excluded");
        }
    }
    if is_modular_jdk(os, &java_dir) {
        all_args.push(resolve_argsfile(os));
    }
    all_args.push(bootcp);
    all_args.push(String::from("-classpath"));
    all_args.push(cp);
    all_args.push(bin_name);
    all_args.push(bin_location);
    all_args.push(hardcoded_paths::get_main().to_string());

    include_not_dashJs(&original_args, &mut all_args);

    all_args
}

fn is_modular_jdk(os: &os_access::Os, jre_dir: &std::path::PathBuf) -> bool {
    if jdk_version(os, jre_dir) > 8 {
        os.log("itw-rust-debug: modular jdk");
        true
    } else {
        os.log("itw-rust-debug: non-modular jdk");
        false
    }
}

fn jdk_version(os: &os_access::Os, jre_dir: &std::path::PathBuf) -> i32 {
    let vec = vec!["-version".to_string()];
    //this of  course fails during tests
    let output_result = os_access::create_java_cmd(os, jre_dir, &vec).output();
    match output_result {
        Ok(output) => {
            for line in String::from_utf8(output.stderr).expect("java version was supopsed to return output").lines() {
                if line.contains("version")
                    && (line.contains("\"1")
                    || line.contains("\"2")
                    || line.contains("\"3")) {
                    if line.contains("\"1.7.0") || line.contains("\"1.7.1") {
                        os.log("itw-rust-debug: detected jdk 7");
                        return 7
                    } else if line.contains("\"1.8.0") {
                        os.log("itw-rust-debug: detected jdk 8");
                        return 8
                    } else {
                        //currently this serves only to determine module/non modular jdk
                        os.log("itw-rust-debug: detected jdk 9 or up");
                        return 9
                    }
                }
            }
            os.log("itw-rust-debug: unrecognized jdk! Fallback to 8!");
            return 8;
        }
        _error => {
            os.log("itw-rust-debug: failed to launch jdk recognition. fallback to 8");
            return 8
        }
    }
}

fn resolve_argsfile(os: &os_access::Os) -> String {
    let args_location = dirs_paths_helper::path_to_string(&jars_helper::resolve_argsfile(os));
    let mut owned_string: String = args_location.to_owned();
    let splash_switch: &str = "@";
    owned_string.insert_str(0, splash_switch);
    let r = String::from(owned_string);
    r
}

fn get_splash(os: &os_access::Os) -> Option<String> {
    let headless = is_headless_enforced();
    let splash_forbidden = is_splash_forbidden();
    get_splash_testable(headless, splash_forbidden, os)
}

fn get_splash_testable(headless: bool, splash_forbidden: bool, os: &os_access::Os) -> Option<String> {
    if !headless && !splash_forbidden {
        let splash_location = dirs_paths_helper::path_to_string(&jars_helper::resolve_splash(os));
        let mut owned_string: String = splash_location.to_owned();
        let splash_switch: &str = "-splash:";
        owned_string.insert_str(0, splash_switch);
        let r = String::from(owned_string);
        Some(r)
    } else {
        None
    }
}

fn clean_param(s: String) -> String {
    let mut ss = String::from(s);
    let was = ss.starts_with("-");
    while ss.starts_with("-") {
        ss = ss[1..ss.len()].to_string();
    }
    if was {
        ss.insert_str(0, "-");
    }
    String::from(ss)
}

#[allow(non_snake_case)]
fn include_not_dashJs(srcs: &Vec<std::string::String>, target: &mut Vec<std::string::String>) {
    for f in srcs.iter() {
        if !f.to_string().starts_with("-J") {
            target.push(f.to_string());
        }
    }
}

#[allow(non_snake_case)]
fn include_dashJs_values(srcs: &Vec<std::string::String>, target: &mut Vec<std::string::String>, os: &os_access::Os) {
    for f in srcs.iter() {
        if f.to_string().starts_with("-J") {
            let s = String::from(f.to_string().get(2..).expect("-J should be substring-able by 2"));
            if s.is_empty() {
                os.info("Warning, empty -J switch")
            } else {
                target.push(s);
            }
        }
    }
}

#[cfg(test)]
pub mod tests_main {
    use utils::tests_utils as tu;

    #[test]
    fn is_splash_forbidden_test() {
        let mut vec: Vec<(String, String)> = Vec::new();
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("".to_string(), "".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("-verbose".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("ICEDTEA_WEB_SPLASH".to_string(), "".to_string()));
        vec.push(("-headless".to_string(),"-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), true);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("ICEDTEA_WEB_SPLASH".to_string(), "".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), true);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("aICEDTEA_WEB_SPLASH".to_string(), "".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("ICEDTEA_WEB_SPLASHb".to_string(), "".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("-blah".to_string(), "-blah".to_string()));
        vec.push(("aICEDTEA_WEB_SPLASHb".to_string(), "".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
        vec = Vec::new();
        vec.push(("ICEDTEA_WEB_SPLASH".to_string(), "value".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), true);
        vec = Vec::new();
        vec.push(("ICEDTEA_WEB_SPLASH".to_string(), "true".to_string()));
        vec.push(("---headless".to_string(), "-blah".to_string()));
        assert_eq!(super::is_splash_forbidden_testable(vec), false);
    }

    #[test]
    fn is_headless_enforced_test() {
        let mut vec: Vec<String> = Vec::new();
        assert_eq!(super::is_headless_enforced_testable(vec), false);
        vec = Vec::new();
        vec.push("".to_string());
        assert_eq!(super::is_headless_enforced_testable(vec), false);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-verbose".to_string());
        assert_eq!(super::is_headless_enforced_testable(vec), false);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-verbose".to_string());
        vec.push("headless".to_string());
        assert_eq!(super::is_headless_enforced_testable(vec), false);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-verbose".to_string());
        vec.push("-headless".to_string());
        assert_eq!(super::is_headless_enforced_testable(vec), true);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-verbose".to_string());
        vec.push("---headless".to_string());
        assert_eq!(super::is_headless_enforced_testable(vec), true);
    }

    #[test]
    fn is_debug_on_test() {
        let mut vec: Vec<String> = Vec::new();
        assert_eq!(super::is_debug_on_testable(vec), None);
        vec = Vec::new();
        vec.push("".to_string());
        assert_eq!(super::is_debug_on_testable(vec), None);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-headless".to_string());
        assert_eq!(super::is_debug_on_testable(vec), None);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("verbose".to_string());
        vec.push("-headless".to_string());
        assert_eq!(super::is_debug_on_testable(vec), None);
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("-verbose".to_string());
        vec.push("-headless".to_string());
        assert_eq!(super::is_debug_on_testable(vec), Some(true));
        vec = Vec::new();
        vec.push("-blah".to_string());
        vec.push("---verbose".to_string());
        vec.push("-headless".to_string());
        assert_eq!(super::is_debug_on_testable(vec), Some(true));
    }

    #[test]
    fn get_splash_test() {
        assert_eq!(super::get_splash_testable(true, false, &tu::TestLogger::create_new()), None);
        assert_eq!(super::get_splash_testable(false, true, &tu::TestLogger::create_new()), None);
        assert_eq!(super::get_splash_testable(true, true, &tu::TestLogger::create_new()), None);
        let some = super::get_splash_testable(false, false, &tu::TestLogger::create_new());
        assert_eq!(some == None, false);
        let val = some.expect("is known to be not none");
        assert_eq!(val.starts_with("-splash:"), true);
    }

    #[test]
    fn clean_param_test() {
        assert_eq!(super::clean_param(String::from("-verbose")), String::from("-verbose"));
        assert_eq!(super::clean_param(String::from("--verbose")), String::from("-verbose"));
        assert_eq!(super::clean_param(String::from("------verbose")), String::from("-verbose"));
        assert_eq!(super::clean_param(String::from("a-headless")), String::from("a-headless"));
        assert_eq!(super::clean_param(String::from("-a-headless")), String::from("-a-headless"));
        assert_eq!(super::clean_param(String::from("----a-headless")), String::from("-a-headless"));
        assert_eq!(super::clean_param(String::from("test-")), String::from("test-"));
        assert_eq!(super::clean_param(String::from("-test-")), String::from("-test-"));
        assert_eq!(super::clean_param(String::from("verbose")), String::from("verbose"));
    }

    #[test]
    fn compose_arguments_test() {
        // this test just ensures -Js are first, and all others are last, and something get betwen them
        let switches = vec![
            String::from("-a"),
            String::from("-J-b")];
        let result = super::compose_arguments(&std::path::PathBuf::from("/some/jre"), &switches, &tu::TestLogger::create_new());
        assert_eq!(result.len() > 3, true);
        assert_eq!(result.get(0).expect("first item should exists"), &String::from("-b"));
        assert_eq!(result.get(result.len() - 1).expect("last item should exists"), &String::from("-a"));
    }

    #[test]
    #[allow(non_snake_case)]
    fn include_not_dashJs_test() {
        let switches = vec![
            String::from("-J-a"),
            String::from("-b"),
            String::from("--Jc"),
            String::from("d")];
        let mut result = Vec::new();
        super::include_not_dashJs(&switches, &mut result);
        let ex = vec![
            String::from("-b"),
            String::from("--Jc"),
            String::from("d")];
        assert_eq!(ex, result);
    }

    #[test]
    #[allow(non_snake_case)]
    fn include_not_dashJs_test_empty() {
        let switches: Vec<std::string::String> = vec![];
        let mut result: Vec<std::string::String> = Vec::new();
        let ex: Vec<std::string::String> = Vec::new();
        super::include_not_dashJs(&switches, &mut result);
        assert_eq!(ex, result);
    }


    #[test]
    #[allow(non_snake_case)]
    fn include_dashJs_valuess_test() {
        let switches = vec![
            String::from("-J-a"),
            String::from("-b"),
            String::from("--Jc"),
            String::from("-J"), //not added, have no arg
            String::from("-J-"), //added
            String::from("-Jd")];
        let mut result = Vec::new();
        super::include_dashJs_values(&switches, &mut result, &tu::TestLogger::create_new());
        let ex = vec![
            String::from("-a"),
            String::from("-"),
            String::from("d")];
        assert_eq!(ex, result);
    }

    #[test]
    #[allow(non_snake_case)]
    fn include_dashJs_values_test_empty() {
        let switches: Vec<std::string::String> = vec![];
        let mut result: Vec<std::string::String> = Vec::new();
        let ex: Vec<std::string::String> = Vec::new();
        super::include_dashJs_values(&switches, &mut result, &tu::TestLogger::create_new());
        assert_eq!(ex, result);
    }
}


