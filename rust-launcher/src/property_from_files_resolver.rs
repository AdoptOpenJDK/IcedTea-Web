use property_from_file;
use os_access;
use dirs_paths_helper;

use std;
use std::string::String;
use std::fmt::Write;

pub fn try_jdk_from_properties(logger: &os_access::Os) -> Option<String> {
    //obviously search in jre dir is missing, when we search for jre
    let array: [Option<std::path::PathBuf>; 4] = [
        dirs_paths_helper::get_itw_config_file(logger),
        dirs_paths_helper::get_itw_legacy_config_file(logger),
        dirs_paths_helper::get_itw_legacy_global_config_file(logger),
        dirs_paths_helper::get_itw_global_config_file(logger)
    ];
    try_key_from_properties_files(logger, &array, property_from_file::JRE_PROPERTY_NAME, &property_from_file::JreValidator {})
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
                if validator.validate(&value) {
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
    use std::cell::RefCell;
    use utils::tests_utils as tu;
    use property_from_file;
    //if you wont to investigate files used for testing
    // use cargo test -- --nocapture to see  files which needs delete
    static DELETE_TEST_FILES: bool = true;

    pub struct TestLogger {
        vec: RefCell<Vec<String>>,
    }

    impl TestLogger {
        fn get_log(&self) -> String {
            let joined = self.vec.borrow_mut().join("; ");
            joined
        }
    }

    impl os_access::Os for TestLogger {
        fn log(&self, s: &str) {
            let ss = String::from(s);
            self.vec.borrow_mut().push(ss);
        }

        fn info(&self, s: &str) {
            let ss = String::from(s);
            self.vec.borrow_mut().push(ss);
        }

        fn get_registry_jdk(&self) -> Option<std::path::PathBuf> {
            None
        }

        fn spawn_java_process(&self, jre_dir: &std::path::PathBuf, args: &Vec<String>) -> std::process::Child {
            panic!("not implemented");
        }

        fn get_system_config_javadir(&self) -> Option<std::path::PathBuf> {
            panic!("not implemented");
        }

        fn get_user_config_dir(&self) -> Option<std::path::PathBuf> {
            panic!("not implemented");
        }

        fn get_legacy_system_config_javadir(&self) -> Option<std::path::PathBuf> {
            panic!("not implemented");
        }

        fn get_legacy_user_config_dir(&self) -> Option<std::path::PathBuf> {
            panic!("not implemented");
        }
    }

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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
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
        let os = TestLogger { vec: RefCell::new(Vec::new()) };
        let r = try_jdk_from_properties_files(&os, &array);
        println!("{}", &os.get_log());
        clean_fake_files(&array);
        assert_ne!(None, r);
        assert_ne!(true, os.get_log().contains("is not valid"));
        assert_eq!(master_dir1.display().to_string(), r.expect("also this r should be full"));
    }
}


