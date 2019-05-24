use property;
use hardcoded_paths;
use dirs_paths_helper as dh;
use os_access;

use std;
use std::string::String;
use std::fs::File;
use std::fmt::Write;

pub static JRE_PROPERTY_NAME: &'static str = "deployment.jre.dir";
pub static VERBOSE_PROPERTY_NAME: &'static str = "deployment.log";

pub static KEY_USER_LOG_DIR: &'static str  = "deployment.user.logdir";  //custom log file; default to xdg_confgi/icedtea-web/log
pub static KEY_ENABLE_LOGGING_TOFILE: &'static str  = "deployment.log.file"; //is loging to file enabled? default false
pub static KEY_ENABLE_LOGGING_TOSTREAMS: &'static str  = "deployment.log.stdstreams";//is logging to stdouts enabled?defoult true
pub static KEY_ENABLE_LOGGING_TOSYSTEMLOG: &'static str  = "deployment.log.system";//is logging to system logs enabled? default true


pub trait Validator {
    fn validate(&self, s: &str, os: &os_access::Os) -> bool;
    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String;
}

pub struct JreValidator {}


impl Validator for JreValidator {
    fn validate(&self, s: &str, os: &os_access::Os) -> bool {
        verify_jdk_string(&s, os)
    }

    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String {
        let mut res = String::new();
        write!(&mut res, "Your custom JRE {} read from {} under key {} is not valid.", value, file.clone().expect("jre path should be loaded").display(), key).expect("unwrap failed");
        write!(&mut res, " Trying other config files, then using default ({}, {}, registry or JAVA_HOME) in attempt to start. Please fix this.", hardcoded_paths::get_jre(), hardcoded_paths::get_jre()).expect("unwrap failed");
        return res;
    }
}

pub struct BoolValidator {}


impl Validator for BoolValidator {
    fn validate(&self, s: &str, _os: &os_access::Os) -> bool {
        verify_bool_string(&s.to_string())
    }

    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String {
        let mut res = String::new();
        write!(&mut res, "the boolean value of {} read from {} under key {} is not valid. Expected true or false (key insensitive)", value, file.clone().expect("jre path should be loaded").display(), key).expect("unwrap failed");
        return res;
    }
}

pub struct NotMandatoryPathValidator {}


impl Validator for NotMandatoryPathValidator {
    fn validate(&self, _s: &str, _os: &os_access::Os) -> bool {
        true
    }

    fn get_fail_message(&self, key: &str, value: &str, file: &Option<std::path::PathBuf>) -> String {
        let mut res = String::new();
        write!(&mut res, "the String value of {} read from {} under key {} is not valid. Expected String", value, file.clone().expect("jre path should be loaded").display(), key).expect("unwrap failed");
        return res;
    }
}

fn verify_bool_string(val: &String) -> bool {
    val.trim().to_lowercase() == "true" || val.trim().to_lowercase() == "false"
}

pub fn str_to_bool(val: &String) -> bool {
    val.trim().to_lowercase() == "true"
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
    } else if !dh::is_file(&path) {
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


fn verify_jdk_string(spath: &str, os: &os_access::Os) -> bool {
    let mut file = std::path::PathBuf::from(spath);
    file.push("bin");
    for suffix in os.get_exec_suffixes() {
        let mut bin_name = String::new();
        write!(&mut bin_name, "java{}", suffix).expect("unwrap failed");
        let full_path = file.join(bin_name);
        if !full_path.exists() {
            continue;
        } else if !dh::is_file(&full_path) {
            continue;
        } else {
            return true
        }
    }
    false
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
    fn verify_bool_string_true() {
        assert_eq!(true, super::verify_bool_string(&String::from("true")));
        assert_eq!(true, super::verify_bool_string(&String::from("True")));
        assert_eq!(true, super::verify_bool_string(&String::from("TRUE")));
        assert_eq!(true, super::verify_bool_string(&String::from("false")));
        assert_eq!(true, super::verify_bool_string(&String::from("FALSE")));
        assert_eq!(true, super::verify_bool_string(&String::from("False")));
    }

    #[test]
    fn verify_bool_string_false() {
        assert_eq!(false, super::verify_bool_string(&String::from("truee")));
        assert_eq!(false, super::verify_bool_string(&String::from("WHATEVER")));
    }

    #[test]
    fn str_to_bool_true() {
        assert_eq!(true, super::str_to_bool(&String::from("true")));
        assert_eq!(true, super::str_to_bool(&String::from("True")));
        assert_eq!(true, super::str_to_bool(&String::from("TRUE")));
    }

    #[test]
    fn str_to_bool_false() {
        assert_eq!(false, super::str_to_bool(&String::from("truee")));
        assert_eq!(false, super::str_to_bool(&String::from("WHATEVER")));
        assert_eq!(false, super::str_to_bool(&String::from("false")));
        assert_eq!(false, super::str_to_bool(&String::from("FALSE")));
        assert_eq!(false, super::str_to_bool(&String::from("False")));
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
        let os = tu::TestLogger::create_new();
        let vs = super::verify_jdk_string(&master_dir.display().to_string(), &os);
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(true, vs);
    }

    #[test]
    fn verify_jdk_string_verify_jdk_path_jdk_bad() {
        let master_dir = tu::fake_jre(false);
        let os = tu::TestLogger::create_new();
        let vs = super::verify_jdk_string(&master_dir.display().to_string(), &os);
        tu::debuggable_remove_dir(&master_dir);
        assert_eq!(false, vs);
    }
}
