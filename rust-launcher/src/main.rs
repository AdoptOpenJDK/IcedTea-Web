mod hardcoded_paths;

fn main() {
    println!("{}",hardcoded_paths::get_jre());
    println!("{}",hardcoded_paths::get_java());
    println!("{}",hardcoded_paths::get_main());
    println!("{}",hardcoded_paths::get_name());
    println!("{}",hardcoded_paths::get_bin());
}
