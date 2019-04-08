use os_access;

use std;
use std::io;
use std::env;

#[cfg(windows)] extern crate dunce;


pub static ICEDTEA_WEB: &'static str = "icedtea-web";
pub static DEPLOYMENT_PROPERTIES: &'static str = "deployment.properties";



pub fn get_xdg_config_dir(os: &os_access::Os) -> Option<std::path::PathBuf> {
    match env::var("XDG_CONFIG_HOME") {
        Ok(war) => {
            Some(std::path::PathBuf::from(war))
        }
        Err(_) => {
            match os.get_home() {
                Some(mut p) => {
                    p.push(".config");
                    Some(p)
                }
                None => None
            }
        }
    }
}

pub fn append_deployment_file(dir: Option<std::path::PathBuf>) -> Option<std::path::PathBuf> {
    match dir {
        Some(mut p) => {
            p.push(DEPLOYMENT_PROPERTIES);
            Some(p)
        }
        None => None
    }
}


pub fn get_itw_config_file(os: &os_access::Os) -> Option<std::path::PathBuf> {
    append_deployment_file(os.get_user_config_dir())
}

pub fn get_itw_legacy_config_file(os: &os_access::Os) -> Option<std::path::PathBuf> {
    append_deployment_file(os.get_legacy_user_config_dir())
}


pub fn get_itw_legacy_global_config_file(os: &os_access::Os) -> Option<std::path::PathBuf> {
    append_deployment_file(os.get_legacy_system_config_javadir())
}

pub fn get_itw_global_config_file(os: &os_access::Os) -> Option<std::path::PathBuf> {
    append_deployment_file(os.get_system_config_javadir())
}

pub fn is_file(path: &std::path::PathBuf) -> bool {
    path.metadata().map(|md| md.is_file()).unwrap_or(false)
}

pub fn is_dir(path: &std::path::PathBuf) -> bool {
    path.metadata().map(|md| md.is_dir()).unwrap_or(false)
}

pub fn path_to_string(path: &std::path::PathBuf) -> String {
    path.to_str().expect("unwrap of os string failed").to_string()
}

pub fn current_program() -> std::path::PathBuf {
    env::current_exe().expect("unwrap of pgm path failed")
}

pub fn current_program_parent() -> std::path::PathBuf {
    std::path::PathBuf::from(current_program().parent().expect("getting of pgm dir failed"))
}

pub fn current_program_name() -> String {
    String::from(current_program().file_name().expect("unwrap of pgm name failed").to_str().expect("unwrap of pgm name failed"))
}

#[cfg(not(windows))]
pub fn canonicalize(full_path: &std::path::PathBuf) -> Result<std::path::PathBuf, io::Error> {
    full_path.canonicalize()
}


#[cfg(windows)]
pub fn canonicalize(full_path: &std::path::PathBuf) -> Result<std::path::PathBuf, io::Error> {
    dunce::canonicalize(&full_path)
}


/*tests*/
#[cfg(test)]
mod tests {
    use std;
    use std::fs;
    use os_access;
    use utils::tests_utils as tu;

    #[cfg(not(windows))]
    fn get_os() -> os_access::Linux {
        os_access::Linux::new(false, false)
    }

    #[cfg(windows)]
    fn get_os() -> os_access::Windows {
        os_access::Windows::new(false, false, true)
    }


    #[test]
    fn check_config_files_paths() {
        let os = get_os();
        let p3 = super::get_itw_config_file(&os);
        assert_ne!(None, p3);
        println!("{}", p3.clone().expect("unwrap failed").display());
        assert_eq!(true, p3.clone().expect("unwrap failed").display().to_string().contains("icedtea-web"));
        assert_eq!(true, p3.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
    }

    #[test]
    #[cfg(not(windows))]
    fn check_config_files_paths_global() {
        let os = os_access::Linux::new(false, false);
        let p6 = super::get_itw_global_config_file(&os);
        assert_ne!(None, p6);
        println!("{}", p6.clone().expect("unwrap failed").display());
        assert_eq!(true, p6.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
    }

    #[test]
    #[cfg(not(windows))]
    fn check_legacy_config_files_paths() {
        let os = os_access::Linux::new(false, false);
        let p4 = super::get_itw_legacy_config_file(&os);
        let p5 = super::get_itw_legacy_global_config_file(&os);
        assert_ne!(None, p4);
        assert_ne!(None, p5);
        println!("{}", p4.clone().expect("unwrap failed").display());
        println!("{}", p5.clone().expect("unwrap failed").display());
        assert_eq!(true, p4.clone().expect("unwrap failed").display().to_string().contains(".icedtea"));
        assert_eq!(true, p4.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("etc"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".java"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".deploy"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("etc"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".java"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("deployment"));
    }

    #[test]
    fn is_not_file() {
        let r = super::is_file(&std::path::PathBuf::from("/definitely/not/existing/file"));
        assert_eq!(false, r);
    }

    #[test]
    fn is_not_file_is_dir() {
        let dir = tu::create_tmp_file();
        tu::debuggable_remove_file(&dir);
        let _cd = fs::create_dir(&dir); //silenting compiler worning
        let r = super::is_file(&dir);
        tu::debuggable_remove_dir(&dir);
        assert_eq!(false, r);
    }

    #[test]
    fn is_file() {
        let file = tu::create_tmp_file();
        let r = super::is_file(&file);
        tu::debuggable_remove_file(&file);
        assert_eq!(true, r);
    }

    #[test]
    fn is_not_dir() {
        let r = super::is_dir(&std::path::PathBuf::from("/definitely/not/existing/file"));
        assert_eq!(false, r);
    }

    #[test]
    fn is_dir() {
        let dir = tu::create_tmp_file();
        tu::debuggable_remove_file(&dir);
        let _cd = fs::create_dir(&dir); //silenting compiler worning
        let r = super::is_dir(&dir);
        tu::debuggable_remove_dir(&dir);
        assert_eq!(true, r);
    }

    #[test]
    fn is_not_dir_is_file() {
        let file = tu::create_tmp_file();
        let r = super::is_dir(&file);
        tu::debuggable_remove_file(&file);
        assert_eq!(false, r);
    }
}
