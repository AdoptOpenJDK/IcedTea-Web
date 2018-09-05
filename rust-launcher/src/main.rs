mod hardcoded_paths;
mod jvm_from_properties;
mod os_access;
mod jvm_from_properties_resolver;
mod utils;
mod property;

use std::string::String;
use std::fmt::Write;
use os_access::Os;
use std::env;


fn main() {
    //TODO verbose will be populated by -verbose in arguments and augmented by deployment properties
    let os = os_access::Linux::new(true);
    let java_dir: std::path::PathBuf;
    let mut info1 = String::new();
    write!(&mut info1, "{}", "itw-rust-debug: trying jdk over properties (").expect("unwrap failed");
    write!(&mut info1, "{}", jvm_from_properties::PROPERTY_NAME).expect("unwrap failed");
    write!(&mut info1, "{}", ")").expect("unwrap failed");
    os.log(&info1);
    match jvm_from_properties_resolver::try_jdk_from_properties(&os) {
        Some(path) => {
            java_dir = std::path::PathBuf::from(path);
            os.log("itw-rust-debug: found and using");
        }
        None => {
            os.log("itw-rust-debug: nothing");
            os.log("itw-rust-debug: trying jdk JAVA_HOME");
            match env::var("JAVA_HOME") {
                Ok(war) => {
                    java_dir = std::path::PathBuf::from(war);
                    os.log("itw-rust-debug: found and using");
                }
                Err(_e) => {
                    os.log("itw-rust-debug: nothing");
                    os.log("itw-rust-debug: trying jdk from registry");
                    match os.get_registry_jdk() {
                        Some(path) => {
                            java_dir = path;
                            os.log("itw-rust-debug: found and using");
                        }
                        None => {
                            os.log("itw-rust-debug: nothing");
                            os.log("itw-rust-debug: failing down to hardcoded");
                            java_dir = std::path::PathBuf::from(hardcoded_paths::get_jre());
                        }
                    }
                }
            }
        }
    }
    let mut info2 = String::new();
    write!(&mut info2, "{}", "itw-rust-debug: selected jre: ").expect("unwrap failed");
    write!(&mut info2, "{}", java_dir.display()).expect("unwrap failed");
    os.log(&info2);
}
