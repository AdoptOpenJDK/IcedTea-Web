mod hardcoded_paths;
mod property_from_file;
mod os_access;
mod dirs_paths_helper;
mod property_from_files_resolver;
mod utils;
mod property;
mod jars_helper;

use std::string::String;
use std::fmt::Write;
use os_access::Os;
use std::env;

fn is_debug_on() -> bool {
    for s in env::args() {
        //this can go wrong with case like -jnlp file-verbose or -html file-verbose
        //but it is really unlikely case as those are ususally .jnlp or .html suffixed
        if s.ends_with("-verbose") {
            return true;
        }
    }
    let os = os_access::Linux::new(false);
    return property_from_files_resolver::try_main_verbose_from_properties(&os);
}

fn main() {
    //TODO verbose will be populated by also from deployment properties
    let os = os_access::Linux::new(is_debug_on());
    os.log(&dirs_paths_helper::path_to_string(&dirs_paths_helper::current_program()));
    let mut info1 = String::new();
    write!(&mut info1, "itw-rust-debug: trying jdk over properties ({})", property_from_file::JRE_PROPERTY_NAME).expect("unwrap failed");
    os.log(&info1);
    let java_dir = utils::find_jre(&os);
    let mut info2 = String::new();
    write!(&mut info2, "selected jre: {}", java_dir.display()).expect("unwrap failed");
    os.info(&info2);

    let a = env::args();
    let s = a.skip(1);
    let c: std::vec::Vec<String> = s.collect();

    let mut child = os.spawn_java_process(&java_dir, &compose_arguments(&java_dir, &c,  &os));
    let ecode = child.wait().expect("failed to wait on child");
    let code = ecode.code().expect("code should be always here");
    std::process::exit(code)
}

fn compose_arguments(java_dir: &std::path::PathBuf, original_args: &std::vec::Vec<String>, os: &os_access::Os) -> Vec<String> {
    let hard_bootcp = hardcoded_paths::get_bootcp();
    let bootcp = jars_helper::get_bootclasspath(&java_dir, os);
    let cp = jars_helper::get_classpath(&java_dir, os);
    let current_name = dirs_paths_helper::current_program_name();
    let current_bin = dirs_paths_helper::current_program();
    let mut info2 = String::new();
    write!(&mut info2, "itw-rust-debug: exemplar boot classpath: {}", hard_bootcp).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: used boot classpath: {}", bootcp).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: used classpath: {}", cp).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: expected name: {}", hardcoded_paths::get_name()).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: current name: {}", current_name).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: installed bin: {}", hardcoded_paths::get_bin()).expect("unwrap failed");
    os.log(&info2);
    info2 = String::new();
    write!(&mut info2, "itw-rust-debug: current bin: {}", &dirs_paths_helper::path_to_string(&current_bin)).expect("unwrap failed");
    os.log(&info2);

    let splash = jars_helper::resolve_splash(os);
    let mut bin_name = String::from("-Dicedtea-web.bin.name=");
    let mut bin_location = String::from("-Dicedtea-web.bin.location=");
    //no metter what ITW_LIBS are saying, imho using current pgm is always correct comapred to hardcoded values
    bin_name.push_str(&current_name);
    bin_location.push_str(&dirs_paths_helper::path_to_string(&current_bin));

    let mut all_args = std::vec::Vec::new();

    include_dashJs_values(&original_args, &mut all_args, os);

    all_args.push(bootcp);
    all_args.push(String::from("-classpath"));
    all_args.push(cp);
    all_args.push(bin_name);
    all_args.push(bin_location);
    all_args.push(hardcoded_paths::get_main().to_string());

    include_not_dashJs(&original_args, &mut all_args);

    all_args
}

#[allow(non_snake_case)]
fn include_not_dashJs(srcs: &Vec<std::string::String>, target: &mut Vec<std::string::String>) {
    for f in srcs.iter() {
        if !f.to_string().starts_with("-J") {
            target.push(f.to_string());
        }
    }
}

#[allow(non_snake_case)]
fn include_dashJs_values(srcs: &Vec<std::string::String>, target: &mut Vec<std::string::String>, os: &os_access::Os) {
    for f in srcs.iter() {
        if f.to_string().starts_with("-J") {
            let s = String::from(f.to_string().get(2..).expect("-J should be substring-able by 2"));
            if s.is_empty() {
                os.info("Warning, empty -J switch")
            } else {
                target.push(s);
            }
        }
    }
}

#[cfg(test)]
pub mod tests_main {
    use utils::tests_utils as tu;


    #[test]
    fn compose_arguments_test() {
        // this test just ensures -Js are first, and all others are last, and something get betwen them
        let switches = vec![
            String::from("-a"),
            String::from("-J-b")];
        let result = super::compose_arguments(&std::path::PathBuf::from("/some/jre"),&switches, &tu::TestLogger::create_new());
        assert_eq!(result.len()>3, true);
        assert_eq!(result.get(0).expect("first item should exists"), &String::from("-b"));
        assert_eq!(result.get(result.len()-1).expect("last item should exists"), &String::from("-a"));

    }

    #[test]
    #[allow(non_snake_case)]
    fn include_not_dashJs_test() {
        let switches = vec![
            String::from("-J-a"),
            String::from("-b"),
            String::from("--Jc"),
            String::from("d")];
        let mut result = Vec::new();
        super::include_not_dashJs(&switches, &mut result);
        let ex = vec![
            String::from("-b"),
            String::from("--Jc"),
            String::from("d")];
        assert_eq!(ex, result);
    }

    #[test]
    #[allow(non_snake_case)]
    fn include_not_dashJs_test_empty() {
        let switches: Vec<std::string::String> = vec![];
        let mut result: Vec<std::string::String> = Vec::new();
        let ex: Vec<std::string::String> = Vec::new();
        super::include_not_dashJs(&switches, &mut result);
        assert_eq!(ex, result);
    }


    #[test]
    #[allow(non_snake_case)]
    fn include_dashJs_valuess_test() {
        let switches = vec![
            String::from("-J-a"),
            String::from("-b"),
            String::from("--Jc"),
            String::from("-Jd")];
        let mut result = Vec::new();
        super::include_dashJs_values(&switches, &mut result, &tu::TestLogger::create_new());
        let ex = vec![
            String::from("-a"),
            String::from("d")];
        assert_eq!(ex, result);
    }

    #[test]
    #[allow(non_snake_case)]
    fn include_dashJs_values_test_empty() {
        let switches: Vec<std::string::String> = vec![];
        let mut result: Vec<std::string::String> = Vec::new();
        let ex: Vec<std::string::String> = Vec::new();
        super::include_dashJs_values(&switches, &mut result, &tu::TestLogger::create_new());
        assert_eq!(ex, result);
    }
}


