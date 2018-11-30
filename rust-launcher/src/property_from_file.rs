use property;
use hardcoded_paths;

use std;
use std::string::String;
use std::fs::File;
use std::fmt::Write;

pub static JRE_PROPERTY_NAME: &'static str = "deployment.jre.dir";
pub static VERBOSE_PROPERTY_NAME: &'static str = "deployment.log";


pub trait Validator {
    fn validate(&self, s: &str) -> bool;
    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String;
}

pub struct JreValidator {}


impl Validator for JreValidator {
    fn validate(&self, s: &str) -> bool {
        verify_jdk_string(&s)
    }

    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String {
        let mut res = String::new();
        write!(&mut res, "Your custom JRE {} read from {} under key {} is not valid.", value, file.clone().expect("jre path should be loaded").display(), key).expect("unwrap failed");
        write!(&mut res, " Trying other config files, then using default ({}, {}, registry or JAVA_HOME) in attempt to start. Please fix this.", hardcoded_paths::get_java(), hardcoded_paths::get_jre()).expect("unwrap failed");
        return res;
    }
}

fn is_file(path: &std::path::PathBuf) -> bool {
    path.metadata().map(|md| md.is_file()).unwrap_or(false)
}


pub fn get_property_from_file(file: Option<std::path::PathBuf>, key: &str) -> Option<String> {
    match file {
        None => None,
        Some(path) => {
            get_property_from_file_direct(path, key)
        }
    }
}

fn get_property_from_file_direct(path: std::path::PathBuf, key: &str) -> Option<String> {
    if !path.exists() {
        None
    } else if !is_file(&path) {
        return None;
    } else {
        let fileresult = File::open(path);
        match fileresult {
            Err(_fe) => None,
            Ok(file) => {
                let result = check_file_for_property(file, key);
                result
            }
        }
    }
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


fn verify_jdk_string(spath: &str) -> bool {
    let mut file = std::path::PathBuf::from(spath);
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

    fn get_jre_from_file(file: Option<std::path::PathBuf>) -> Option<String> {
        super::get_property_from_file(file, super::JRE_PROPERTY_NAME)
    }

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
        let prop = super::check_file_for_property(f.expect("file was not opened"), super::JRE_PROPERTY_NAME);
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }

    #[test]
    fn check_file_for_property_jredir() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let prop = super::check_file_for_property(f.expect("file was not opened"), super::JRE_PROPERTY_NAME);
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
        let prop = get_jre_from_file(Some(path.clone()));
        tu::debuggable_remove_file(&path);
        assert_eq!("/some/jre", prop.expect("property was supposed to be loaded"));
    }

    #[test]
    fn get_jre_from_file_not_found() {
        let path = tu::create_tmp_file();
        let prop = get_jre_from_file(Some(path.clone()));
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }


    #[test]
    fn get_jre_from_file_notexists() {
        let path = tu::create_tmp_file();
        tu::debuggable_remove_file(&path);
        let prop = get_jre_from_file(Some(path));
        assert_eq!(None, prop);
    }

    #[test]
    fn get_jre_from_file_none() {
        let prop = get_jre_from_file(None);
        assert_eq!(None, prop);
    }

    #[test]
    fn verify_jdk_string_verify_jdk_path_jdk_ok() {
        let master_dir = tu::fake_jre(true);
        let vs = super::verify_jdk_string(&master_dir.display().to_string());
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(true, vs);
    }

    #[test]
    fn verify_jdk_string_verify_jdk_path_jdk_bad() {
        let master_dir = tu::fake_jre(false);
        let vs = super::verify_jdk_string(&master_dir.display().to_string());
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(false, vs);
    }
}
