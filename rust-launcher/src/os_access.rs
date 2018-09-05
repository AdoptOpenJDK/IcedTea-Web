use std;

pub trait Os {
    //logging "api" can change
    fn log(&self, s: &str);
    fn info(&self, s: &str);
    fn get_registry_jdk(&self) -> Option<std::path::PathBuf>;
}

pub struct Linux {
    verbose: bool,
}

impl Linux {
    pub fn new(debug: bool) -> Linux {
        Linux { verbose: debug }
    }
}

impl Os for Linux {
    fn log(&self, s: &str) {
        if self.verbose {
            println!("{}", s);
        }
    }

    fn info(&self, s: &str) {
        println!("{}", s);
    }

    fn get_registry_jdk(&self) -> Option<std::path::PathBuf> {
        None
    }
}
