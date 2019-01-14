use std;
use env;
use dirs_paths_helper;
use os_access;
use std::fmt::Write;
use hardcoded_paths;
use property_from_files_resolver;

pub fn find_jre(os: &os_access::Os) -> std::path::PathBuf {
    match property_from_files_resolver::try_jdk_from_properties(os) {
        Some(path) => {
            os.log("itw-rust-debug: found and using");
            return std::path::PathBuf::from(path);
        }
        None => {
            os.log("itw-rust-debug: nothing");
            os.log("itw-rust-debug: trying jdk JAVA_HOME");
            match env::var("JAVA_HOME") {
                Ok(war) => {
                    os.log("itw-rust-debug: found and using");
                    return std::path::PathBuf::from(war);
                }
                Err(_e) => {
                    os.log("itw-rust-debug: nothing");
                    os.log("itw-rust-debug: trying jdk from registry");
                    match os.get_registry_jdk() {
                        Some(path) => {
                            os.log("itw-rust-debug: found and using");
                            return path;
                        }
                        None => {
                            os.log("itw-rust-debug: nothing");
                            os.log("itw-rust-debug: trying jdk from path");
                            match get_jdk_from_path_conditionally(os) {
                                Some(path) => {
                                    os.log("itw-rust-debug: found and using");
                                    return path;
                                }
                                None => {
                                    os.log("itw-rust-debug: nothing");
                                    os.log("itw-rust-debug: failing down to hardcoded");
                                    return std::path::PathBuf::from(hardcoded_paths::get_jre());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fn get_jdk_from_path_conditionally(os: &os_access::Os) -> Option<std::path::PathBuf> {
    let libsearch = hardcoded_paths::get_libsearch(os);
    if libsearch == hardcoded_paths::ItwLibSearch::DISTRIBUTION {
        os.log("itw-rust-debug: skipping jdk from path, your build is distribution");
        None
    } else {
        if libsearch == hardcoded_paths::ItwLibSearch::BOTH {
            os.info("your build is done as BOTH distribution and bundled, jdk from PATH may be not what you want!");
        }
        get_jdk_from_path(os)
    }
}

fn get_jdk_from_path(os: &os_access::Os) -> Option<std::path::PathBuf> {
    env::var_os("PATH").and_then(|paths| {
        env::split_paths(&paths).filter_map(|dir| {
            for suffix in os.get_exec_suffixes() {
                let mut bin_name = String::new();
                write!(&mut bin_name, "java{}", suffix).expect("unwrap failed");
                let full_path = dir.join(bin_name);
                let mut info1 = String::new();
                write!(&mut info1, "itw-rust-debug: trying {}", full_path.to_str().expect("unwrap failed")).expect("unwrap failed");
                os.log(&info1);
                if dirs_paths_helper::is_file(&full_path) {
                    let can = match full_path.canonicalize() {
                        Ok(resolved) => {
                            //.../bin/java
                            resolved
                        }
                        _error => {
                            full_path.clone()
                        }
                    };
                    //.../bin/java -> bin
                    let jre_bin_dir: std::path::PathBuf = std::path::PathBuf::from(&can.parent().expect("file should always have parent"));
                    let jre_dir: std::path::PathBuf;
                    //will panic if the file was /java - not fixing
                    if jre_bin_dir.file_name().expect("java's parent should have name") == "bin" {
                        jre_dir = std::path::PathBuf::from(jre_bin_dir.parent().expect("java's  bin dir should have parent"))
                    } else {
                        os.info("Error: JRE from path seems to not have bin dir");
                        jre_dir = match jre_bin_dir.parent() {
                            Some(p) => {
                                //.../bin/ -> ...
                                std::path::PathBuf::from(p)
                            }
                            None => {
                                //??
                                jre_bin_dir.clone()
                            }
                        }
                    }
                    let mut info2 = String::new();
                    write!(&mut info2, "itw-rust-debug: found {} resolving as {}", full_path.to_str().expect("unwrap failed"), can.to_str().expect("unwrap failed")).expect("unwrap failed");
                    os.log(&info2);
                    //returning owner of /bin/java as needed by find_jre
                    return Some(jre_dir);
                }
            }
            None
        }).next()
    })
}


#[cfg(test)]
pub mod tests_utils {
    use std;
    use std::fs::File;
    use std::time::{SystemTime, UNIX_EPOCH};
    use std::fs::OpenOptions;
    use std::fmt::Write as fmt_write;
    use std::io::Write;
    use std::sync::atomic::{AtomicUsize, Ordering, ATOMIC_USIZE_INIT};
    use property_from_file;
    use os_access;
    use std::cell::RefCell;
    use dirs_paths_helper;

    pub struct TestLogger {
        vec: RefCell<Vec<String>>,
    }

    impl TestLogger {
        pub fn create_new() -> TestLogger {
            TestLogger { vec: RefCell::new(Vec::new()) }
        }

        pub fn get_log(&self) -> String {
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

        fn spawn_java_process(&self, _jre_dir: &std::path::PathBuf, _args: &Vec<String>) -> std::process::Child {
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

        fn get_home(&self) -> Option<std::path::PathBuf> {
            panic!("not implemented");
        }
        fn get_classpath_separator(&self) -> char { ':' }

        fn get_exec_suffixes(&self) -> &'static [&'static str] {
            panic!("not implemented");
        }
    }


    // rand is in separate crate, so using atomic increment instead
    static TMP_COUNTER: AtomicUsize = ATOMIC_USIZE_INIT;
    // use cargo test -- --nocapture to see  files which needs delete
    static CLEAN_TMP_FILES: bool = true;

    pub fn debuggable_remove_file(file: &std::path::PathBuf) {
        if CLEAN_TMP_FILES {
            std::fs::remove_file(file).expect("remove of tmp failed");
        } else {
            println!("file {} intentionally not deleted!", file.display());
        }
    }


    pub fn debuggable_remove_dir(dir: &std::path::PathBuf) {
        if CLEAN_TMP_FILES {
            std::fs::remove_dir_all(dir).expect("remove of tmp dir failed");
        } else {
            println!("directory {} intentionally not deleted!", dir.display());
        }
    }

    pub fn create_tmp_name() -> String {
        // base name on time
        let now = SystemTime::now();
        let since_the_epoch = now.duration_since(UNIX_EPOCH).expect("Time went backwards");
        let in_ms = since_the_epoch.as_secs() * 1000 + since_the_epoch.subsec_nanos() as u64 / 1_000_000;
        //each [test] run via cargo test run (by default) in its own thread, so two files of same name may be handled over time
        //thus adding also atomic counter
        let id = TMP_COUNTER.fetch_add(1, Ordering::AcqRel);
        //create nice name
        let mut owned_string: String = "itw-".to_owned();
        owned_string.push_str(&in_ms.to_string());
        owned_string.push_str("-");
        owned_string.push_str(&id.to_string());
        owned_string.push_str("-rusttmp");
        owned_string
    }

    pub fn create_scratch_dir() -> std::path::PathBuf {
        let mut project_dir = dirs_paths_helper::current_program_parent();
        while !project_dir.to_str().expect("Cannot get path name").ends_with("rust-launcher") {
            project_dir = project_dir.parent().expect("Cannot get parent").to_path_buf();
        }
        let mut scratch_dir = std::path::PathBuf::new();
        scratch_dir.push(project_dir);
        scratch_dir.push("target");
        scratch_dir.push("scratch");
        if !scratch_dir.exists() {
            std::fs::create_dir_all(scratch_dir.as_path()).expect("Cannot create scratch dir");
        }
        scratch_dir
    }

    const CUSTOM_TMP_DIR: Option<&'static str> = option_env!("ITW_TMP_REPLACEMENT");

    pub fn prepare_tmp_dir() -> std::path::PathBuf {
        if CUSTOM_TMP_DIR.is_some() {
            let dir = std::path::PathBuf::from(CUSTOM_TMP_DIR.expect("is_some failed for CUSTOM_TMP_DIR"));
            assert_eq!(true, dir.exists());
            dir
        } else {
            create_scratch_dir()
        }
    }

    pub fn create_tmp_file() -> std::path::PathBuf {
        //let mut dir = CUSTOM_TMP_DIR.unwrap_or(env::temp_dir());
        let mut dir = prepare_tmp_dir();
        let s = create_tmp_name();
        dir.push(s);
        let f = File::create(&dir).expect("File created");
        f.sync_all().expect("data were written");
        dir
    }

    pub fn create_tmp_propfile_with_content() -> std::path::PathBuf {
        create_tmp_propfile_with_custom_jre_content("/some/jre")
    }

    pub fn create_tmp_propfile_with_custom_jre_content(jre_path: &str) -> std::path::PathBuf {
        let dir = create_tmp_file();
        let mut f = OpenOptions::new()
            .write(true)
            .open(&dir).expect("just created file failed to open");
        let mut res = String::new();
        write!(&mut res, "{}", "key1=val1\n").expect("unwrap failed");
        write!(&mut res, "{}", "key2=val2\n").expect("unwrap failed");
        write!(&mut res, "{}", property_from_file::JRE_PROPERTY_NAME).expect("unwrap failed");
        write!(&mut res, "{}", "=").expect("unwrap failed");
        write!(&mut res, "{}", jre_path).expect("unwrap failed");
        write!(&mut res, "{}", "\n").expect("unwrap failed");
        write!(&mut res, "{}", "key2=val3\n").expect("unwrap failed");
        write!(&mut res, "{}", "key4=val4\n").expect("unwrap failed");
        write!(&mut res, "{}", ", ").expect("unwrap failed");
        f.write_all(res.as_bytes()).expect("writing of tmp file failed");
        f.sync_all().expect("data were written");
        dir
    }

    pub fn fake_jre(valid: bool) -> std::path::PathBuf {
        let mut fake_jre = create_tmp_file();
        debuggable_remove_file(&fake_jre);
        let master_dir = fake_jre.clone();
        std::fs::create_dir(&fake_jre).expect("dir creation failed");
        fake_jre.push("bin");
        std::fs::create_dir(&fake_jre).expect("dir creation failed");
        fake_jre.push("java");
        if valid {
            File::create(&fake_jre).expect("File created");
        }
        master_dir
    }
}
