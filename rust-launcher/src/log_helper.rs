use os_access;
use std::fs::OpenOptions;
use std::io::Write;
use std::time::SystemTime;
use std::time::UNIX_EPOCH;
use std::fs::File;

static mut first: bool = true;

//0 critical
//1 info
//2 debug only
pub fn log_impl(level: i32, os: &os_access::Os, s: &str) {
    if level == 0 {} else if level == 1 {
        println!("{}", s);
    } else if level == 2 {
        if os.is_verbose() {
            println!("{}", s);
        }
    }
    unsafe {
        if first {
            //mkdir
            //createfile
            //rust itw log initiate dor so
            first = false;
            let start = SystemTime::now();
            let t = start.duration_since(UNIX_EPOCH).expect("time should be measureable");
            let mut file = File::create("my-file").expect("failed to create file log");
            if let Err(e) = write!(&mut file, "itw-rust-debug: file log started: {}\n", t.as_secs()) {
                println!("Couldn't write to file: {}", e);
            }
            file.sync_all();
        }
    }
    let mut file = OpenOptions::new()
        .write(true)
        .append(true)
        .open("my-file")
        .expect("failed to append to file log");

    if let Err(e) = writeln!(&mut file, "{}", s) {
        println!("Couldn't write to file: {}", e);
    }
    file.sync_all();
}