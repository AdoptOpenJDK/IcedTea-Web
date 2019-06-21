use os_access;
use env;
use std::string::String;
use std::fmt::Write;
use std::str::FromStr;

/*legacy variables*/
const SPLASH_PNG: Option<&'static str> = option_env!("SPLASH_PNG");
const JRE: Option<&'static str> = option_env!("JRE");
const MAIN_CLASS: Option<&'static str> = option_env!("MAIN_CLASS");
const CORE_JAR: Option<&'static str> = option_env!("CORE_JAR");
const COMMON_JAR: Option<&'static str> = option_env!("COMMON_JAR");
const JNLPAPI_JAR: Option<&'static str> = option_env!("JNLPAPI_JAR");
const XMLPARSER_JAR: Option<&'static str> = option_env!("XMLPARSER_JAR");
const CLIENTS_JAR: Option<&'static str> = option_env!("CLIENTS_JAR");
const JNLPSERVER_JAR: Option<&'static str> = option_env!("JNLPSERVER_JAR");
const TAGSOUP_JAR: Option<&'static str> = option_env!("TAGSOUP_JAR");
const RHINO_JAR: Option<&'static str> = option_env!("RHINO_JAR");
const ITW_LIBS: Option<&'static str> = option_env!("ITW_LIBS");
const MODULARJDK_ARGS_LOCATION: Option<&'static str> = option_env!("MODULARJDK_ARGS_LOCATION");
const MSLINKS_JAR: Option<&'static str> = option_env!("MSLINKS_JAR");


pub fn get_jre() -> &'static str {
    JRE.unwrap_or("JRE-dev-unspecified")
}

pub fn get_main() -> &'static str {
    MAIN_CLASS.unwrap_or("MAIN_CLASS-dev-unspecified")
}

pub fn get_splash() -> &'static str {
    SPLASH_PNG.unwrap_or("SPLASH_PNG-dev-unspecified")
}

pub fn get_core() -> &'static str { CORE_JAR.unwrap_or("CORE_JAR-dev-unspecified") }

pub fn get_common() -> &'static str { COMMON_JAR.unwrap_or("COMMON_JAR-dev-unspecified") }

pub fn get_jnlpapi() -> &'static str { JNLPAPI_JAR.unwrap_or("JNLPAPI_JAR-dev-unspecified") }

pub fn get_xmlparser() -> &'static str { XMLPARSER_JAR.unwrap_or("XMLPARSER_JAR-dev-unspecified") }

pub fn get_clientsjar() -> &'static str { CLIENTS_JAR.unwrap_or("CLIENTS_JAR-dev-unspecified") }

pub fn get_jnlpserver() -> &'static str { JNLPSERVER_JAR.unwrap_or("JNLPSERVER_JAR-dev-unspecified") }

pub fn get_itwlibsearch() -> &'static str { ITW_LIBS.unwrap_or("ITW_LIBS-dev-unspecified") }

pub fn get_tagsoup() -> Option<&'static str> { sanitize(TAGSOUP_JAR) }

pub fn get_rhino() -> Option<&'static str> { sanitize(RHINO_JAR) }

pub fn get_mslinks() -> Option<&'static str> { sanitize(MSLINKS_JAR) }

pub fn get_argsfile() -> &'static str {
    MODULARJDK_ARGS_LOCATION.unwrap_or("MODULARJDK_ARGS_LOCATION-dev-unspecified")
}

//unluckily, option_env can go wild and retunr Some("") isntead of None. Fixing here.
fn sanitize(candidate: Option<&'static str>)  -> Option<&'static str> {
    match candidate {
        Some(s) => {
                if !String::from(String::from(s).trim()).is_empty() {
                    return candidate;
                } else {
                    return None;
                }
            }
        _none => {
            return None;
            }
        }
}


#[derive(PartialEq)]
pub enum ItwLibSearch {
    BUNDLED,
    DISTRIBUTION,
    EMBEDDED //like BUNDLED, but with affect on jre path
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ParseItwLibSearch { _priv: () }

impl FromStr for ItwLibSearch {

    type Err = ParseItwLibSearch;

    fn from_str(sstr: &str) -> Result<ItwLibSearch, ParseItwLibSearch> {
        if sstr == "EMBEDDED" {
            return Ok(ItwLibSearch::EMBEDDED);
        }
        if sstr == "BUNDLED" {
            return Ok(ItwLibSearch::BUNDLED);
        }
        if sstr == "DISTRIBUTION" {
            return Ok(ItwLibSearch::DISTRIBUTION);
        }
        return Err(ParseItwLibSearch { _priv: () })
    }
}

impl std::fmt::Display for ItwLibSearch {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        match self {
            ItwLibSearch::BUNDLED => write!(f, "BUNDLED"),
            ItwLibSearch::DISTRIBUTION => write!(f, "DISTRIBUTION"),
            ItwLibSearch::EMBEDDED => write!(f, "EMBEDDED"),
        }
    }
}

pub fn get_libsearch(logger: &os_access::Os) -> ItwLibSearch {
    let itw_libs_override = env::var("ITW_LIBS");
    match itw_libs_override {
        Ok(result_of_override_var) => match ItwLibSearch::from_str(&result_of_override_var) {
            Ok(result_of_override_to_enum) => {
                return result_of_override_to_enum;
            }
            _err => {
                let mut info = String::new();
                write!(&mut info, "ITW-LIBS provided, but have invalid value of {}. Use BUNDLED, DISTRIBUTION or EMBEDDED", result_of_override_var);
                logger.important(&info);
            }
        }
        _error => {
            //no op, continuing via get_itwlibsearch
        }
    }
    match ItwLibSearch::from_str(get_itwlibsearch()) {
        Ok(v) => {
            return v
        }
        _err=> {
            panic!("itw-lib search out of range");
        }
    }
}


/*new variables*/

/*tests*/
#[cfg(test)]
mod tests {
    use std::str::FromStr;

    #[test]
    fn variables_non_default() {
        assert_ne!(String::from(super::get_jre()).trim(), String::from("JRE-dev-unspecified"));
        assert_ne!(String::from(super::get_main()).trim(), String::from("MAIN_CLASS-dev-unspecified"));
        assert_ne!(String::from(super::get_splash()).trim(), String::from("SPLASH_PNG-dev-unspecified"));
        assert_ne!(String::from(super::get_core()).trim(), String::from("CORE_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_common()).trim(), String::from("COMMON_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_xmlparser()).trim(), String::from("XMLPARSER_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_clientsjar()).trim(), String::from("CLIENTS_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_jnlpserver()).trim(), String::from("JNLPSERVER_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_jnlpapi()).trim(), String::from("JNLPAPI_JAR-dev-unspecified"));
        assert_ne!(String::from(super::get_itwlibsearch()).trim(), String::from("ITW_LIBS-dev-unspecified"));
        assert_ne!(String::from(super::get_argsfile()).trim(), String::from("MODULARJDK_ARGS_LOCATION-dev-unspecified"));
    }

    #[test]
    fn variables_non_empty() {
        assert_ne!(String::from(super::get_jre()).trim(), String::from(""));
        assert_ne!(String::from(super::get_main()).trim(), String::from(""));
        assert_ne!(String::from(super::get_splash()).trim(), String::from(""));
        assert_ne!(String::from(super::get_core()).trim(), String::from(""));
        assert_ne!(String::from(super::get_common()).trim(), String::from(""));
        assert_ne!(String::from(super::get_xmlparser()).trim(), String::from(""));
        assert_ne!(String::from(super::get_clientsjar()).trim(), String::from(""));
        assert_ne!(String::from(super::get_jnlpserver()).trim(), String::from(""));
        assert_ne!(String::from(super::get_jnlpapi()).trim(), String::from(""));
        assert_ne!(String::from(super::get_itwlibsearch()).trim(), String::from(""));
        assert_ne!(String::from(super::get_argsfile()).trim(), String::from(""));
    }

    #[test]
    fn get_itwlibsearch_in_enumeration() {
        assert_eq!(super::get_itwlibsearch() == "EMBEDDED" || super::get_itwlibsearch() == "BUNDLED" || super::get_itwlibsearch() == "DISTRIBUTION", true);
    }

    #[test]
    fn itw_libsearch_to_enum_test() {
        assert!(super::ItwLibSearch::from_str("BUNDLED") == Ok(super::ItwLibSearch::BUNDLED));
        assert!(super::ItwLibSearch::from_str("EMBEDDED") == Ok(super::ItwLibSearch::EMBEDDED));
        assert!(super::ItwLibSearch::from_str("DISTRIBUTION") == Ok(super::ItwLibSearch::DISTRIBUTION));
        assert!(super::ItwLibSearch::from_str("") == Err(super::ParseItwLibSearch { _priv: () }));
    }
}
