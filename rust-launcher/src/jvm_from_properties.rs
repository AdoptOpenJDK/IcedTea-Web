use property;

use std;
use std::env;
use std::string::String;
use std::fs::File;

static ICEDTEA_WEB: &'static str = "icedtea-web";
pub static DEPLOYMENT_PROPERTIES: &'static str = "deployment.properties";
pub static PROPERTY_NAME: &'static str = "deployment.jre.dir";

fn is_file(path: &std::path::PathBuf) -> bool {
    let mdr = path.metadata();
    match mdr {
        Ok(md) => md.is_file(),
        Err(_e) => false
    }
}

fn get_home() -> Option<std::path::PathBuf> {
    match env::home_dir() {
        Some(p) => Some(p),
        None => None
    }
}

fn get_config_dir() -> Option<std::path::PathBuf> {
    match env::var("XDG_CONFIG_HOME") {
        Ok(war) => {
            Some(std::path::PathBuf::from(war))
        }
        Err(_e) => {
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

pub fn get_itw_config_dir() -> Option<std::path::PathBuf> {
    match get_config_dir() {
        Some(mut p) => {
            p.push(ICEDTEA_WEB);
            Some(p)
        }
        None => None
    }
}


pub fn get_itw_legacy_config_dir() -> Option<std::path::PathBuf> {
    match get_home() {
        Some(mut p) => {
            p.push(".icedtea");
            Some(p)
        }
        None => None
    }
}


pub fn get_itw_config_file() -> Option<std::path::PathBuf> {
    match get_itw_config_dir() {
        Some(mut p) => {
            p.push(DEPLOYMENT_PROPERTIES);
            Some(p)
        }
        None => None
    }
}

pub fn get_itw_legacy_config_file() -> Option<std::path::PathBuf> {
    match get_itw_legacy_config_dir() {
        Some(mut p) => {
            p.push(DEPLOYMENT_PROPERTIES);
            Some(p)
        }
        None => None
    }
}


pub fn get_itw_legacy_global_config_file() -> Option<std::path::PathBuf> {
    let mut path = std::path::PathBuf::from("/etc/.java/.deploy");
    path.push(DEPLOYMENT_PROPERTIES);
    Some(path)
}

pub fn get_itw_global_config_file() -> Option<std::path::PathBuf> {
    let mut path = std::path::PathBuf::from("/etc/.java/deployment");
    path.push(DEPLOYMENT_PROPERTIES);
    Some(path)
}


pub fn check_file_for_property_jredir(file: File) -> Option<String> {
    check_file_for_property(file, PROPERTY_NAME)
}

fn check_file_for_property(file: File, key: &str) -> Option<String> {
    let p = property::Property::load(file, key);
    match p {
        None => { None }
        Some(property) => {
            Some(property.value)
        }
    }
}


pub fn get_jre_from_file(file: Option<std::path::PathBuf>) -> Option<String> {
    match file {
        None => None,
        Some(path) => {
            get_jre_from_file_direct(path)
        }
    }
}

fn get_jre_from_file_direct(path: std::path::PathBuf) -> Option<String> {
    if !path.exists() {
        None
    } else if !is_file(&path) {
        return None;
    } else {
        let fileresult = File::open(path);
        match fileresult {
            Err(_fe) => None,
            Ok(file) => {
                let result = check_file_for_property_jredir(file);
                result
            }
        }
    }
}

pub fn verify_jdk_string(file: &String) -> bool {
    verify_jdk_path(&std::path::PathBuf::from(file))
}

fn verify_jdk_path(ffile: &std::path::PathBuf) -> bool {
    let mut file = ffile.clone();
    file.push("bin");
    file.push("java");
    if !file.exists() {
        false
    } else if !is_file(&file) {
        false
    } else {
        true
    }
}

/*tests*/
#[cfg(test)]
mod tests {
    use std;
    use std::fs::File;
    use utils::tests_utils as tu;


    #[test]
    fn is_not_file_() {
        let r = super::is_file(&std::path::PathBuf::from("/definitely/not/existing/file"));
        assert_eq!(false, r);
    }

    #[test]
    fn is_file_() {
        let dir = tu::create_tmp_file();
        let r = super::is_file(&dir);
        tu::debuggable_remove_file(&dir);
        assert_eq!(true, r);
    }

    #[test]
    fn check_file_for_property_jredir_not_found() {
        let path = tu::create_tmp_file();
        let f = File::open(&path);
        let prop = super::check_file_for_property_jredir(f.expect("file was not opened"));
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }

    #[test]
    fn check_file_for_property_jredir() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let prop = super::check_file_for_property_jredir(f.expect("file was not opened"));
        tu::debuggable_remove_file(&path);
        assert_eq!("/some/jre", prop.expect("property was supposed to be loaded"));
    }


    #[test]
    fn check_file_for_property_not_found() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let k = "not_existing_key";
        let prop = super::check_file_for_property(f.expect("file was not opened"), k);
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }

    #[test]
    fn check_file_for_property_item_exists() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let k = "key2";
        let prop = super::check_file_for_property(f.expect("file was not opened"), k);
        tu::debuggable_remove_file(&path);
        assert_eq!("val2", prop.expect("property was supposed to be loaded"));
    }

    #[test]
    fn get_jre_from_file_exists() {
        let path = tu::create_tmp_propfile_with_content();
        let prop = super::get_jre_from_file(Some(path.clone()));
        tu::debuggable_remove_file(&path);
        assert_eq!("/some/jre", prop.expect("property was supposed to be loaded"));
    }

    #[test]
    fn get_jre_from_file_not_found() {
        let path = tu::create_tmp_file();
        let prop = super::get_jre_from_file(Some(path.clone()));
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }


    #[test]
    fn get_jre_from_file_notexists() {
        let path = tu::create_tmp_file();
        tu::debuggable_remove_file(&path);
        let prop = super::get_jre_from_file(Some(path));
        assert_eq!(None, prop);
    }

    #[test]
    fn get_jre_from_file_none() {
        let prop = super::get_jre_from_file(None);
        assert_eq!(None, prop);
    }

    #[test]
    fn verify_jdk_string_verify_jdk_path_jdk_ok() {
        let master_dir = tu::fake_jre(true);
        let vs = super::verify_jdk_string(&master_dir.display().to_string());
        let vp = super::verify_jdk_path(&master_dir);
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(true, vs);
        assert_eq!(true, vp);
    }

    #[test]
    fn verify_jdk_string_verify_jdk_path_jdk_bad() {
        let master_dir = tu::fake_jre(false);
        let vs = super::verify_jdk_string(&master_dir.display().to_string());
        let vp = super::verify_jdk_path(&master_dir);
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(false, vs);
        assert_eq!(false, vp);
    }

    #[test]
    fn check_config_files_paths() {
        let p1 = super::get_itw_config_dir();
        let p2 = super::get_itw_legacy_config_dir();
        let p3 = super::get_itw_config_file();
        let p4 = super::get_itw_legacy_config_file();
        let p5 = super::get_itw_legacy_global_config_file();
        let p6 = super::get_itw_global_config_file();
        assert_ne!(None, p1);
        assert_ne!(None, p2);
        assert_ne!(None, p3);
        assert_ne!(None, p4);
        assert_ne!(None, p5);
        assert_ne!(None, p6);
        println!("{}", p1.clone().expect("unwrap failed").display());
        println!("{}", p2.clone().expect("unwrap failed").display());
        println!("{}", p3.clone().expect("unwrap failed").display());
        println!("{}", p4.clone().expect("unwrap failed").display());
        println!("{}", p5.clone().expect("unwrap failed").display());
        println!("{}", p6.clone().expect("unwrap failed").display());
        assert_eq!(true, p1.clone().expect("unwrap failed").display().to_string().contains("icedtea-web"));
        assert_eq!(true, p2.clone().expect("unwrap failed").display().to_string().contains(".icedtea"));
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
