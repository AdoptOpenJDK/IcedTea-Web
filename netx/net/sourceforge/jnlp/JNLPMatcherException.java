package net.sourceforge.jnlp;

public class JNLPMatcherException extends Exception
{
    private static final long serialVersionUID = 1L;

    public JNLPMatcherException(String message)
    {
        super(message);
    }
    
    public JNLPMatcherException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
