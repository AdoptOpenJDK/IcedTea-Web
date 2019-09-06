package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.JNLPFile;

public enum ForkingStrategy {

    IF_JNLP_REQUIRES {
        public boolean needsToFork(JNLPFile file) {
            return file.needsNewVM();
        }
    },

    NEVER {
        public boolean needsToFork(JNLPFile file) {
            return false;
        }
    },

    ALWAYS {
        public boolean needsToFork(JNLPFile file) {
            return true;
        }

        @Override
        public boolean mayRunManagedApplication() {
            return false;
        }
    },

    ;

    public abstract boolean needsToFork(JNLPFile file);

    public boolean mayRunManagedApplication() {
        return true;
    }

}
