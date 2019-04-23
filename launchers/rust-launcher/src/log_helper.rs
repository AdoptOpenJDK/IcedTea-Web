//this module was created as std::io::Write; and std::fmt::Write; hcat be imoted together
//adn still, there are different methods. Notably writeln is only in io version. but format! is only in fmt version
use os_access;
use std::fs::OpenOptions;
use std::io::Write;
use std::time::SystemTime;
use std::time::UNIX_EPOCH;
use std::fs::File;
use property_from_files_resolver;

static mut FIRST: bool = true;

//0 critical
//1 info
//2 debug only
pub fn log_impl(level: i32, os: &os_access::Os, s: &str) {
    if level == 0 {
        if os.advanced_logging().log_to_stdstreams {
            println!("{}", s);
        }
        if os.advanced_logging().log_to_system {
            let mut info2 = String::from("IcedTea-Web nativerustlauncher error. Consult - https://icedtea.classpath.org/wiki/IcedTea-Web\n");
            info2.push_str(s);
            os.system_log(&info2);
        }
    } else if level == 1 {
        if os.advanced_logging().log_to_stdstreams {
            println!("{}", s);
        }
    } else if level == 2 {
        if os.is_verbose() {
            if os.advanced_logging().log_to_stdstreams {
                println!("{}", s);
            }
        }
    }
    if os.advanced_logging().log_to_file {
        unsafe {
            if FIRST {
                FIRST = false;
                std::fs::create_dir_all(os.advanced_logging().log_target_file.parent().expect("hard to imagine log file without parent"));
                let start = SystemTime::now();
                let t = start.duration_since(UNIX_EPOCH).expect("time should be measureable");
                let mut file = File::create(&os.advanced_logging().log_target_file).expect("failed to create file log");
                let allsec = t.as_secs();
                let sec = allsec % 60;
                let min = (allsec / 60) % 60;
                let h = allsec / (60 * 60);
                if let Err(e) = write!(&mut file, "itw-rust-debug: file log started: {}:{}:{}\n", h, min, sec) {
                    println!("Couldn't write to file: {}", e);
                }
                file.sync_all();
            }
        }
        let mut file = OpenOptions::new()
            .write(true)
            .append(true)
            .open(&os.advanced_logging().log_target_file)
            .expect("failed to append to file log");

        if let Err(e) = writeln!(&mut file, "{}", s) {
            println!("Couldn't write to file: {}", e);
        }
        file.sync_all();
    }
}

pub struct AdvancedLogging {
    pub log_to_file: bool,
    pub log_target_file: std::path::PathBuf,
    pub log_to_stdstreams: bool,
    pub log_to_system: bool,
}

impl Default for AdvancedLogging {
    fn default() -> AdvancedLogging {
        AdvancedLogging {
            log_to_file: false,
            log_target_file: std::path::PathBuf::from("undefined"),
            log_to_stdstreams: true,
            log_to_system: true,
        }
    }
}

impl AdvancedLogging {
    pub fn load(os: &os_access::Os) -> AdvancedLogging {
        AdvancedLogging {
            log_to_file: property_from_files_resolver::try_log_to_file_from_properties(os),
            log_to_stdstreams: property_from_files_resolver::try_log_to_streams_from_properties(os),
            log_to_system: property_from_files_resolver::try_log_to_system_from_properties(os),
            log_target_file: property_from_files_resolver::try_logtarget_from_properties(os),
        }
    }
}