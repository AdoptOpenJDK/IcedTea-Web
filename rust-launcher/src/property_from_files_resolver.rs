use property_from_file;
use os_access;
use dirs_paths_helper;
use::log_helper;
use std::time::SystemTime;
use std::time::UNIX_EPOCH;

use std;
use std::string::String;
use std::fmt::Write;


fn get_basic_array(logger: &os_access::Os) -> [Option<std::path::PathBuf>; 4] {
    //obviously search in jre dir is missing, when we search for jre
    let array: [Option<std::path::PathBuf>; 4] = [
        dirs_paths_helper::get_itw_config_file(logger),
        dirs_paths_helper::get_itw_legacy_config_file(logger),
        dirs_paths_helper::get_itw_legacy_global_config_file(logger),
        dirs_paths_helper::get_itw_global_config_file(logger)
    ];
    array
}

pub fn try_jdk_from_properties(logger: &os_access::Os) -> Option<String> {
    try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::JRE_PROPERTY_NAME, &property_from_file::JreValidator {})
}

pub fn try_main_verbose_from_properties(logger: &os_access::Os) -> bool {
    let str_bool = try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::VERBOSE_PROPERTY_NAME, &property_from_file::BoolValidator {});
    match str_bool {
        Some(val) => {
            property_from_file::str_to_bool(&val)
        }
        None => {
            false
        }
    }
}

pub fn try_log_to_file_from_properties(logger: &os_access::Os) -> bool {
    let str_bool = try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::KEY_ENABLE_LOGGING_TOFILE, &property_from_file::BoolValidator {});
    match str_bool {
        Some(val) => {
            property_from_file::str_to_bool(&val)
        }
        None => {
            log_helper::AdvancedLogging::default().log_to_file
        }
    }
}

pub fn try_log_to_streams_from_properties(logger: &os_access::Os) -> bool {
    let str_bool = try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::KEY_ENABLE_LOGGING_TOSTREAMS, &property_from_file::BoolValidator {});
    match str_bool {
        Some(val) => {
            property_from_file::str_to_bool(&val)
        }
        None => {
            log_helper::AdvancedLogging::default().log_to_stdstreams
        }
    }
}

pub fn try_log_to_system_from_properties(logger: &os_access::Os) -> bool {
    let str_bool = try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::KEY_ENABLE_LOGGING_TOSYSTEMLOG, &property_from_file::BoolValidator {});
    match str_bool {
        Some(val) => {
            property_from_file::str_to_bool(&val)
        }
        None => {
            log_helper::AdvancedLogging::default().log_to_system
        }
    }
}

//this method is not in log_helpers because of io::write and  fmt::write issue
pub fn logfile_name() -> String {
    let start = SystemTime::now();
    let t = start.duration_since(UNIX_EPOCH).expect("time should be measureable");
    let m = t.as_secs();
    //itw-javantx-2019-02-16_20:56:08.882.log
    let mut future_name = String::new();
    write!(&mut future_name, "itw-nativerustlauncher-{}.log", m).expect("unwrap failed");
    future_name
}

pub fn try_logtarget_from_properties(logger: &os_access::Os) ->  std::path::PathBuf {
    let str_candidate = try_key_from_properties_files(logger, &get_basic_array(logger), property_from_file::KEY_USER_LOG_DIR, &property_from_file::NotMandatoryPathValidator {});
    match str_candidate {
        Some(val) => {
            let mut  future_file=std::path::PathBuf::from(val);
            future_file.push(logfile_name());
            future_file
        }
        None => {
            let mut cfgdir_candidate = logger.get_user_config_dir();
            match cfgdir_candidate {
                Some(mut cfgdir) => {
                    cfgdir.push("log");
                    cfgdir.push(logfile_name());
                    cfgdir
                }
                None => {
                    std::path::PathBuf::from("unloadable")
                }
            }
        }
    }
}

pub fn try_direct_key_from_properties(key: &str, logger: &os_access::Os) ->  String {
    let str_candidate = try_key_from_properties_files(logger, &get_basic_array(logger), key, &property_from_file::NotMandatoryPathValidator {});
    match str_candidate {
        Some(val) => {
            val
        }
        None => {
            return String::from("")
        }
    }
}


fn try_key_from_properties_files(logger: &os_access::Os, array: &[Option<std::path::PathBuf>], key: &str, validator: &property_from_file::Validator) -> Option<String> {
    for file in array {
        let mut info1 = String::new();
        write!(&mut info1, "itw-rust-debug: checking {} in: {}", key, file.clone().unwrap_or(std::path::PathBuf::from("None")).display()).expect("unwrap failed");
        logger.log(&info1);
        match property_from_file::get_property_from_file(file.clone(), &key) {
            Some(value) => {
                let mut info2 = String::new();
                write!(&mut info2, "itw-rust-debug: located {} in file {}", value, file.clone().expect("file should be already verified").display()).expect("unwrap failed");
                logger.log(&info2);
                if validator.validate(&value, logger) {
                    return Some(value);
                } else {
                    //the only output out of verbose mode
                    let res = validator.get_fail_message(&key, &value, file);
                    logger.info(&res);
                }
            }
            None => {
                logger.log("itw-rust-debug: property not located or file inaccessible");
            }
        }
    }
    None
}

/*tests*/
/*To print the diagnostic output use `cargo test -- --nocapture
`*/
#[cfg(test)]
mod tests {
    use std;
    use os_access;
    use utils::tests_utils as tu;
    use property_from_file;
    //if you wont to investigate files used for testing
    // use cargo test -- --nocapture to see  files which needs delete
    static DELETE_TEST_FILES: bool = true;

    fn try_jdk_from_properties_files(logger: &os_access::Os, array: &[Option<std::path::PathBuf>]) -> Option<String> {
        super::try_key_from_properties_files(logger, &array, property_from_file::JRE_PROPERTY_NAME, &property_from_file::JreValidator {})
    }

    #[test]
    fn try_jdk_from_properties_files_4nothing() {
        let array: [Option<std::path::PathBuf>; 4] = [
            None,
            None,
            None,
            None
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        assert_eq!(None, r);
    }

    #[test]
    fn try_jdk_from_properties_files_4nonexisting() {
        let array: [Option<std::path::PathBuf>; 4] = [
            Some(std::path::PathBuf::from("Nonexisting file 1")),
            Some(std::path::PathBuf::from("Nonexisting file 2")),
            Some(std::path::PathBuf::from("Nonexisting file 3")),
            Some(std::path::PathBuf::from("Nonexisting file 4")),
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        assert_eq!(None, r);
    }

    fn clean_fake_files(array: &[Option<std::path::PathBuf>]) {
        for jdk in array {
            match jdk.clone() {
                Some(path) => {
                    if DELETE_TEST_FILES {
                        tu::debuggable_remove_file(&path);
                    } else {
                        println!("file {} intentionally not deleted!", path.display());
                    }
                }
                None => {}
            }
        }
    }

    #[test]
    fn try_jdk_from_properties_files_4empty() {
        let array: [Option<std::path::PathBuf>; 4] = [
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        clean_fake_files(&array);
        assert_eq!(None, r);
    }

    #[test]
    fn try_jdk_from_properties_files_invalid_jdk() {
        let array: [Option<std::path::PathBuf>; 4] = [
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre1"))),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre2"))),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre3"))),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre4"))),
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        clean_fake_files(&array);
        assert_eq!(None, r);
        assert_eq!(true, os.get_log().contains("is not valid"));
        assert_eq!(true, os.get_log().contains("non/existing/jre1"));
        assert_eq!(true, os.get_log().contains("non/existing/jre2"));
        assert_eq!(true, os.get_log().contains("non/existing/jre3"));
        assert_eq!(true, os.get_log().contains("non/existing/jre4"));
    }

    #[test]
    fn try_jdk_from_properties_files_none_and_valid() {
        let master_dir = tu::fake_jre(true);
        let array: [Option<std::path::PathBuf>; 4] = [
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content(&master_dir.display().to_string()))),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre3"))),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content("non/existing/jre4"))),
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        clean_fake_files(&array);
        assert_ne!(None, r);
        assert_ne!(true, os.get_log().contains("is not valid"));
        assert_ne!(true, os.get_log().contains("non/existing/jre3"));
        assert_ne!(true, os.get_log().contains("non/existing/jre4"));
        assert_eq!(master_dir.display().to_string(), r.expect("r should be full"));
    }

    #[test]
    fn try_jdk_from_properties_files_none_and_more_valid() {
        let master_dir1 = tu::fake_jre(true);
        let master_dir2 = tu::fake_jre(true);
        let array: [Option<std::path::PathBuf>; 4] = [
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content(&master_dir1.display().to_string()))),
            Some(std::path::PathBuf::from(tu::create_tmp_file())),
            Some(std::path::PathBuf::from(tu::create_tmp_propfile_with_custom_jre_content(&master_dir2.display().to_string()))),
        ];
        let os = tu::TestLogger::create_new();
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        clean_fake_files(&array);
        assert_ne!(None, r);
        assert_ne!(true, os.get_log().contains("is not valid"));
        assert_eq!(master_dir1.display().to_string(), r.expect("also this r should be full"));
    }
}


