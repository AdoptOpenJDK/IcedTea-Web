use std;
use dirs_paths_helper;

pub trait Os {
    //logging "api" can change
    fn log(&self, s: &str);
    fn info(&self, s: &str);
    fn get_registry_jdk(&self) -> Option<std::path::PathBuf>;
    // next to system and home cfg dir, there is also by-jre config dir, but that do not need to be handled os-specific way
    //https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/jcp/properties.html
    fn get_system_config_javadir(&self) -> Option<std::path::PathBuf>;
    fn get_user_config_dir(&self) -> Option<std::path::PathBuf>;
    //is valid  only on linux, otherwise returns get_system_config_javadir
    fn get_legacy_system_config_javadir(&self) -> Option<std::path::PathBuf>;
    //is valid  only on linux, otherwise returns get_user_config_dir
    fn get_legacy_user_config_dir(&self) -> Option<std::path::PathBuf>;
    fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child;
}

pub struct Linux {
    verbose: bool,
}

impl Linux {
    pub fn new(debug: bool) -> Linux {
        Linux { verbose: debug }
    }
}

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
        match dirs_paths_helper::get_xdg_config_dir() {
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
        match dirs_paths_helper::get_home() {
            Some(mut p) => {
                p.push(".icedtea");
                Some(p)
            }
            None => None
        }
    }
    
    fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child {
        let mut bin_java = jre_dir.clone();
        bin_java.push("bin");
        bin_java.push("java");
        let mut cmd = std::process::Command::new(&bin_java);
        for ar in args.into_iter() {
            cmd.arg(ar);
        }
        cmd.stdin(std::process::Stdio::inherit());
        cmd.stdout(std::process::Stdio::inherit());
        cmd.stderr(std::process::Stdio::inherit());
        let res = cmd.spawn();
        match res {
            Ok(child) => child,
            Err(_) => panic!("Error spawning JVM process, \
                 java executable: [{}], arguments: [{:?}]", bin_java.into_os_string().to_str().expect("path should unwrap"), args)
        }
    }
}
