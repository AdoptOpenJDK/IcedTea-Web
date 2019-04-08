use std::fmt;
use std::io::{BufReader, BufRead};
use std::fs::File;

pub struct Property {
    pub key: String,
    pub value: String
}

impl fmt::Debug for Property {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Property {{ k: {}, v: {} }}", self.key, self.value)
    }
}

impl PartialEq for Property {
    fn eq(&self, other: &Property) -> bool {
        self.key == other.key && self.value == other.value
    }
}

impl Property {
    pub fn load(file: File, key: &str) -> Option<Property> {
        let r = check_file_for_property(file, key);
        match r {
            None => { None }
            Some(value) => {
                Some(Property { key: key.to_string(), value: value })
            }
        }
    }
}

/*
 *checked_split is not safe. If you are splitting out of bounds, you get thread panic
 */
fn checked_split(s: String, i: usize) -> Property {
    let key = &s[..i];
    let val = &s[(i + 1)..];
    Property { key: String::from(key.trim()), value: String::from(val.trim()) }
}

fn split_property(string: &String) -> Option<Property> {
    let trimmed = string.trim().to_string();
    if trimmed.starts_with("#") {
        None
    } else {
        let eq_char = match trimmed.find("=") {
            Some(i) => i,
            None => usize::max_value()
        };
        let doubledot_char = match trimmed.find(":") {
            Some(i) => i,
            None => usize::max_value()
        };
        if eq_char == doubledot_char && doubledot_char == usize::max_value() {
            None
        } else if eq_char <= doubledot_char {
            Some(checked_split(trimmed, eq_char))
        } else {
            Some(checked_split(trimmed, doubledot_char))
        }
    }
}

//error[E0658]: use of unstable library feature 'str_escape': return type may change to be an iterator (see issue #27791)
//
//                             let a = kvv.value.escape_unicode();
//For more information about this error, try `rustc --explain E0658`.
//https://icedtea.classpath.org/bugzilla/show_bug.cgi?id=3697
fn escape_unicode(src: String) -> String {
    src
}

fn check_file_for_property(file: File, key: &str) -> Option<String> {
    let bf = BufReader::new(file);
    for lineresult in bf.lines() {
        match lineresult {
            Err(_le) => {
                return None;
            }
            Ok(line) => {
                let kv = split_property(&line);
                match kv {
                    None => {}
                    Some(kvv) => {
                        if kvv.key.eq(key) {
                            return Some(escape_unicode(kvv.value.replace(r"\:", ":")));
                        }
                    }
                }
            }
        }
    }
    None
}

/*tests*/
#[cfg(test)]
mod tests {
    use utils::tests_utils as tu;
    use std::fs::File;

    #[test]
    fn check_property() {
        let p1 = super::Property { key: String::from("k1"), value: String::from("v1") };
        let p2 = super::Property { key: String::from("k1"), value: String::from("v1") };
        let p3 = super::Property { key: String::from("k2"), value: String::from("v1") };
        let p4 = super::Property { key: String::from("k1"), value: String::from("v2") };
        let p5 = super::Property { key: String::from("k2"), value: String::from("v2") };
        assert_eq!(p1, p2);
        assert_ne!(p1, p3);
        assert_ne!(p1, p4);
        assert_ne!(p1, p5);
    }

    #[test]
    fn checked_split() {
        let p1 = super::checked_split("aXb".to_string(), 1);
        assert_eq!("a".to_string(), p1.key);
        assert_eq!("b".to_string(), p1.value);
        let p2 = super::checked_split("aXb".to_string(), 0);
        assert_eq!("".to_string(), p2.key);
        assert_eq!("Xb".to_string(), p2.value);
        let p3 = super::checked_split("aXb".to_string(), 2);
        assert_eq!("aX".to_string(), p3.key);
        assert_eq!("".to_string(), p3.value);
    }

    #[test]
    fn split_property_nodelimiter() {
        let p = super::split_property(&"aXb".to_string());
        assert_eq!(None, p);
    }

    #[test]
    fn split_property_colon_delimiter() {
        let p = super::split_property(&"a:b".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p.key);
        assert_eq!("b".to_string(), p.value);
    }

    #[test]
    fn split_property_equals_delimiter() {
        let p = super::split_property(&"a=b".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p.key);
        assert_eq!("b".to_string(), p.value);
    }

    #[test]
    fn split_property_mixed_delimiter() {
        let p1 = super::split_property(&"a=:b".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p1.key);
        assert_eq!(":b".to_string(), p1.value);
        let p2 = super::split_property(&"a:=b".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p2.key);
        assert_eq!("=b".to_string(), p2.value);
    }

    #[test]
    fn split_property_trimming() {
        let p1 = super::split_property(&" a =  ".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p1.key);
        assert_eq!("".to_string(), p1.value);
        let p2 = super::split_property(&"a : b".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p2.key);
        assert_eq!("b".to_string(), p2.value);
        let p3 = super::split_property(&" a :b ".to_string()).expect("should be some!");
        assert_eq!("a".to_string(), p3.key);
        assert_eq!("b".to_string(), p3.value)
    }

    #[test]
    fn split_property_reals() {
        let p1 = super::split_property(&"java.property = some:terrible:jdk".to_string()).expect("should be some!");
        assert_eq!("java.property".to_string(), p1.key);
        assert_eq!("some:terrible:jdk".to_string(), p1.value);
    }

    #[test]
    fn check_load_not_found() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let k = "not_existing_key";
        let prop = super::Property::load(f.expect("file was not opened"), k);
        tu::debuggable_remove_file(&path);
        assert_eq!(None, prop);
    }

    #[test]
    fn check_load_item_exists() {
        let path = tu::create_tmp_propfile_with_content();
        let f = File::open(&path);
        let k = "key2";
        let prop = super::Property::load(f.expect("file was not opened"), k);
        tu::debuggable_remove_file(&path);
        assert_eq!("val2", prop.expect("property was supposed to be loaded").value);
    }
}
