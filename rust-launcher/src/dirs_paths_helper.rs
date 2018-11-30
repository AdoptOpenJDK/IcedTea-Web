use os_access;

use std;
use std::env;

pub static ICEDTEA_WEB: &'static str = "icedtea-web";
pub static DEPLOYMENT_PROPERTIES: &'static str = "deployment.properties";

pub fn get_home() -> Option<std::path::PathBuf> {
    match env::home_dir() {
        Some(p) => Some(p),
        None => None
    }
}

pub fn get_xdg_config_dir() -> Option<std::path::PathBuf> {
    match env::var("XDG_CONFIG_HOME") {
        Ok(war) => {
            Some(std::path::PathBuf::from(war))
        }
        Err(_) => {
            match get_home() {
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


/*tests*/
#[cfg(test)]
mod tests {
    use os_access;

    #[test]
    fn check_config_files_paths() {
        let os = os_access::Linux::new(false);
        let p3 = super::get_itw_config_file(&os);
        let p4 = super::get_itw_legacy_config_file(&os);
        let p5 = super::get_itw_legacy_global_config_file(&os);
        let p6 = super::get_itw_global_config_file(&os);
        assert_ne!(None, p3);
        assert_ne!(None, p4);
        assert_ne!(None, p5);
        assert_ne!(None, p6);
        println!("{}", p3.clone().expect("unwrap failed").display());
        println!("{}", p4.clone().expect("unwrap failed").display());
        println!("{}", p5.clone().expect("unwrap failed").display());
        println!("{}", p6.clone().expect("unwrap failed").display());
        assert_eq!(true, p3.clone().expect("unwrap failed").display().to_string().contains("icedtea-web"));
        assert_eq!(true, p3.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
        assert_eq!(true, p4.clone().expect("unwrap failed").display().to_string().contains(".icedtea"));
        assert_eq!(true, p4.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("etc"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".java"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".deploy"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("etc"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains(".java"));
        assert_eq!(true, p5.clone().expect("unwrap failed").display().to_string().contains("deployment"));
        assert_eq!(true, p6.clone().expect("unwrap failed").display().to_string().ends_with("deployment.properties"));
    }
}
