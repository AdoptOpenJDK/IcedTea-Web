package net.sourceforge.jnlp.cache;

@SuppressWarnings("serial")
public class IllegalResourceDescriptorException extends IllegalArgumentException {
    /**
     * Constructs a <code>IllegalResourceDescriptorException</code> with the
     * specified detail message.
     * @param msg the detail message.
     */
    public IllegalResourceDescriptorException(String msg) {
        super(msg);
    }
}
