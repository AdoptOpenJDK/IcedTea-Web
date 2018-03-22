/*legacy variables*/
const PROGRAM_NAME: Option<&'static str> = option_env!("PROGRAM_NAME");
const LAUNCHER_BOOTCLASSPATH: Option<&'static str> = option_env!("LAUNCHER_BOOTCLASSPATH");
const JAVAWS_SPLASH_LOCATION: Option<&'static str> = option_env!("JAVAWS_SPLASH_LOCATION");
const JAVA: Option<&'static str> = option_env!("JAVA");
const JRE: Option<&'static str> = option_env!("JRE");
const MAIN_CLASS: Option<&'static str> = option_env!("MAIN_CLASS");
const BIN_LOCATION: Option<&'static str> = option_env!("BIN_LOCATION");
const NETX_JAR: Option<&'static str> = option_env!("NETX_JAR");
const PLUGIN_JAR: Option<&'static str> = option_env!("PLUGIN_JAR");
const JSOBJECT_JAR: Option<&'static str> = option_env!("JSOBJECT_JAR");


pub fn get_jre() -> &'static str {
    return JRE.unwrap_or("JRE-dev-unspecified")
}

pub fn get_java() -> &'static str {
    return JAVA.unwrap_or("JAVA-dev-unspecified")
}

pub fn get_main() -> &'static str {
    return MAIN_CLASS.unwrap_or("MAIN_CLASS-dev-unspecified")
}

pub fn get_name() -> &'static str {
    return PROGRAM_NAME.unwrap_or("PROGRAM_NAME-dev-unspecified")
}

pub fn get_bin() -> &'static str {
    return BIN_LOCATION.unwrap_or("BIN_LOCATION-dev-unspecified")
}



/*new variables*/

/*tests*/
#[cfg(test)]
mod tests {

    #[test]
    fn variables_non_default() {
        assert_ne!(String::from(super::get_jre()).trim(), String::from("JRE-dev-unspecified"));
        assert_ne!(String::from(super::get_java()).trim(), String::from("JAVA-dev-unspecified"));
        assert_ne!(String::from(super::get_main()).trim(), String::from("MAIN_CLASS-dev-unspecified"));
        assert_ne!(String::from(super::get_name()).trim(), String::from("PROGRAM_NAME-dev-unspecified"));
        assert_ne!(String::from(super::get_bin()).trim(), String::from("BIN_LOCATION-dev-unspecified"));
    }

    #[test]
    fn variables_non_empty() {
        assert_ne!(String::from(super::get_jre()).trim(), String::from(""));
        assert_ne!(String::from(super::get_java()).trim(), String::from(""));
        assert_ne!(String::from(super::get_main()).trim(), String::from(""));
        assert_ne!(String::from(super::get_name()).trim(), String::from(""));
        assert_ne!(String::from(super::get_bin()).trim(), String::from(""));
    }
}
