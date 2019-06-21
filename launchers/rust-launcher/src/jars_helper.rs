use std;
use env;
use hardcoded_paths;
use hardcoded_paths::ItwLibSearch;
use property_from_files_resolver;
use os_access;
use dirs_paths_helper;
use std::fmt::Write;

//order important!
// TODO verify with EMBEDDED
const LOCAL_PATHS: &'static [&'static str] = &[
    "libs",
    "../libs",
    ".",
    "bin",
    "../bin"];

pub fn resolve_argsfile(logger: &os_access::Os) -> std::path::PathBuf {
    resolve_jar(hardcoded_paths::get_argsfile(), logger)
}


pub fn resolve_splash(logger: &os_access::Os) -> std::path::PathBuf {
    resolve_jar(hardcoded_paths::get_splash(), logger)
}

fn try_jar_in_subdirs(dir: &std::path::PathBuf, name: &std::ffi::OsStr, logger: &os_access::Os) -> Option<std::path::PathBuf> {
    for path in LOCAL_PATHS {
        let mut candidate = std::path::PathBuf::from(dir);
        candidate.push(path);
        candidate.push(name);
        let mut info1 = String::new();
        write!(&mut info1, "itw-rust-debug: trying {}", &dirs_paths_helper::path_to_string(&candidate)).expect("unwrap failed");
        logger.log(&info1);
        if dirs_paths_helper::is_file(&candidate) {
            logger.log(&dirs_paths_helper::path_to_string(&candidate));
            return Some(candidate);
        }
    }
    return None;
}

fn resolve_jar(full_hardcoded_path: &str, logger: &os_access::Os) -> std::path::PathBuf {
    let current_libsearch = hardcoded_paths::get_libsearch(logger);
    let full_path = std::path::PathBuf::from(full_hardcoded_path);
    let name = full_path.file_name().expect("Error obtaining file name form hardcoded jar");
    //ITW_LIBS_DIR always, hopefully it is  not set
    let itw_libs_override = env::var("ITW_HOME");
    match itw_libs_override {
        Ok(result_of_override_var) => {
            let custom_dir = std::path::PathBuf::from(&result_of_override_var);
            if dirs_paths_helper::is_dir(&custom_dir) {
                match try_jar_in_subdirs(&custom_dir, name, logger) {
                    Some(candidate) => {
                        return candidate;
                    }
                    _none => {
                        //nothing found, continuing
                    }
                }
            } else {
                let mut info1 = String::new();
                write!(&mut info1, "custom ITW_HOME provided, but do not exists or is not directory: {}", &(dirs_paths_helper::path_to_string(&custom_dir)));
                logger.important(&info1);
            }
        }
        _error => {
            //good, no messing with paths!
        }
    }
    //first local dir - if allowed
    if current_libsearch == ItwLibSearch::BUNDLED || current_libsearch == ItwLibSearch::EMBEDDED {
        let pgmdir = dirs_paths_helper::current_program_parent();
        let pgmparent: std::path::PathBuf = match pgmdir.parent() {
            Some(s) => {
                s.to_path_buf()
            }
            None => {
                pgmdir.clone()
            }
        };
        match try_jar_in_subdirs(&pgmparent, &name, logger) {
            Some(candidate) => {
                return candidate;
            }
            _none => {
                //nothing found, continuing
            }
        }
    }
    //then installed dirs, if allowd
    if current_libsearch == ItwLibSearch::DISTRIBUTION {
        let candidate = std::path::PathBuf::from(full_hardcoded_path);
        if dirs_paths_helper::is_file(&candidate) {
            logger.log(&dirs_paths_helper::path_to_string(&candidate));
            return candidate;
        }
    }
    //fallback to hardcoded, but warn
    logger.important("Warning!, Fall back in resolve_jar to hardcoded paths: ");
    let result = std::path::PathBuf::from(full_hardcoded_path);
    logger.important(&dirs_paths_helper::path_to_string(&result));
    result
}

fn append_if_exists(value: Option<&'static str>, os: &os_access::Os, vec: &mut Vec<std::path::PathBuf>) {
    match value {
        Some(s) => {
            vec.push(resolve_jar(s, os));
        }
        _none => {}
    }
}

pub static XCP_MODS_DELMITER: &'static str = " ";

fn filter_out_val(val: String, vec: &mut Vec<std::path::PathBuf>) {
    let mut i:i32 = 0;
    while i < (vec.len() as i32) {
        let cpstring=dirs_paths_helper::path_to_string(vec.get(i as usize).expect("string should be there"));
        for value in val.split(XCP_MODS_DELMITER) {
            if !String::from(String::from(value).trim()).is_empty() && cpstring.contains(value) {
                vec.remove(i as usize);
                i = i - 1;
                break;
            }
        }
        i = i + 1;
    }
}

fn filter_out_key(key: &str, os: &os_access::Os, vec: &mut Vec<std::path::PathBuf>) {
    let val = property_from_files_resolver::try_direct_key_from_properties(key, os);
    filter_out_val(val,  vec);
}

fn filter_in_val(val: String, vec: &mut Vec<std::path::PathBuf>) {
    for value in val.split(" ") {
        vec.push(std::path::PathBuf::from(value));
    }
}

fn filter_in_key(key: &str, os: &os_access::Os, vec: &mut Vec<std::path::PathBuf>) {
    let val = property_from_files_resolver::try_direct_key_from_properties(key, os);
    filter_in_val(val, vec)
}

//TODO what to do with rt.jar, nashorn and javafx.jar with jdk11 and up?
fn get_bootcp_members(jre_path: &std::path::PathBuf, os: &os_access::Os) -> Vec<std::path::PathBuf> {
    let mut cp_parts = Vec::new();
    cp_parts.push(resolve_jar(hardcoded_paths::get_core(), os));
    cp_parts.push(resolve_jar(hardcoded_paths::get_common(), os));
    cp_parts.push(resolve_jar(hardcoded_paths::get_jnlpapi(), os));
    cp_parts.push(resolve_jar(hardcoded_paths::get_xmlparser(), os));
    cp_parts.push(resolve_jar(hardcoded_paths::get_clientsjar(), os));
    cp_parts.push(resolve_jar(hardcoded_paths::get_jnlpserver(), os));
    append_if_exists(hardcoded_paths::get_rhino(), os, &mut cp_parts);
    append_if_exists(hardcoded_paths::get_tagsoup(), os, &mut cp_parts);
    append_if_exists(hardcoded_paths::get_mslinks(), os, &mut cp_parts);
    let mut nashorn_jar = jre_path.clone();
    nashorn_jar.push("lib");
    nashorn_jar.push("ext");
    nashorn_jar.push("nashorn.jar");
    cp_parts.push(nashorn_jar);
    filter_out_key("deployment.launcher.rust.bootcp.remove", os, &mut cp_parts, );
    filter_in_key("deployment.launcher.rust.bootcp.add", os, &mut cp_parts);
    cp_parts
}

//can this be buggy? Shouldnt jfxrt.jar be in boot classapth? Copied from shell launchers...
//see eg: http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2018-November/040492.html
fn get_cp_members(jre_path: &std::path::PathBuf, os: &os_access::Os) -> Vec<std::path::PathBuf> {
    let mut cp_parts = Vec::new();
    let mut rt_jar = jre_path.clone();
    rt_jar.push("lib");
    rt_jar.push("rt.jar");
    cp_parts.push(rt_jar);
    let mut jfxrt_jar = jre_path.clone();
    jfxrt_jar.push("lib");
    jfxrt_jar.push("ext");
    jfxrt_jar.push("jfxrt.jar");
    cp_parts.push(jfxrt_jar);
    filter_out_key("deployment.launcher.rust.cp.remove", os, &mut cp_parts, );
    filter_in_key("deployment.launcher.rust.cp.add", os, &mut cp_parts);
    cp_parts
}

fn compose_class_path(members: Vec<std::path::PathBuf>, os: &os_access::Os) -> String {
    let mut result = String::new();
    for (i, mb) in members.iter().enumerate()  {
        result.push_str(&dirs_paths_helper::path_to_string(&mb));
        if i < members.len() - 1 {
            result.push(os.get_classpath_separator());
        }
    }
    result
}

pub fn get_classpath(jre_path: &std::path::PathBuf, os: &os_access::Os) -> String {
    compose_class_path(get_cp_members(jre_path, os), os)
}

pub fn get_bootclasspath(jre_path: &std::path::PathBuf, os: &os_access::Os) -> String {
    let mut result = String::from("-Xbootclasspath/a:");
    result.push_str(&compose_class_path(get_bootcp_members(jre_path, os), os));
    result
}

/*tests*/
#[cfg(test)]
mod tests {
    use utils::tests_utils as tu;

    #[test]
    fn compose_class_path_test_empty() {
        assert_eq!("", super::compose_class_path(vec![], &tu::TestLogger::create_new()));
    }

    #[test]
    fn compose_class_path_test_two() {
        assert_eq!("a:b", super::compose_class_path(vec![std::path::PathBuf::from("a"), std::path::PathBuf::from("b")], &tu::TestLogger::create_new()));
    }

    #[test]
    fn compose_class_path_test_one() {
        assert_eq!("a", super::compose_class_path(vec![std::path::PathBuf::from("a")], &tu::TestLogger::create_new()));
    }


    #[test]
    fn compose_class_path_test_three() {
        assert_eq!("a/b:/a/b/:c:c:a/b", super::compose_class_path(vec![
            std::path::PathBuf::from("a/b"),
            std::path::PathBuf::from("/a/b/"),
            std::path::PathBuf::from("c"),
            std::path::PathBuf::from("c"),
            std::path::PathBuf::from("a/b"),
        ], &tu::TestLogger::create_new()));
    }

    #[test]
    fn filter_out_val_test1() {
        let mut vec = vec![std::path::PathBuf::from("a"), std::path::PathBuf::from("b"), std::path::PathBuf::from("c")];
        super::filter_out_val(String::from("a c"), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("b")], vec);
        super::filter_out_val(String::from(""), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("b")], vec);
        super::filter_out_val(String::from("   "), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("b")], vec);
        super::filter_out_val(String::from("b"), &mut vec);
        let mut empty: Vec<std::path::PathBuf> = Vec::new();
        assert_eq!(empty, vec);

    }

    #[test]
    fn filter_out_val_test2() {
        let mut vec = vec![std::path::PathBuf::from("a"), std::path::PathBuf::from("b"), std::path::PathBuf::from("c")];
        super::filter_out_val(String::from("b"), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("a"), std::path::PathBuf::from("c")], vec);
        super::filter_out_val(String::from("a c"), &mut vec);
        let mut empty: Vec<std::path::PathBuf> = Vec::new();
        assert_eq!(empty, vec);

    }

    #[test]
    fn filter_in_val_test1() {
        let mut vec = vec![std::path::PathBuf::from("a")];
        super::filter_in_val(String::from("b"), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("a"), std::path::PathBuf::from("b")], vec);

    }

    #[test]
    fn filter_in_val_test2() {
        let mut vec = vec![];
        super::filter_in_val(String::from("b"), &mut vec);
        assert_eq!(vec![std::path::PathBuf::from("b")], vec);

    }
}
