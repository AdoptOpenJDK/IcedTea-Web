package javax.jnlp;

public interface FileContents {

    public java.lang.String getName() throws java.io.IOException;

    public java.io.InputStream getInputStream() throws java.io.IOException;

    public java.io.OutputStream getOutputStream(boolean overwrite) throws java.io.IOException;

    public long getLength() throws java.io.IOException;

    public boolean canRead() throws java.io.IOException;

    public boolean canWrite() throws java.io.IOException;

    public JNLPRandomAccessFile getRandomAccessFile(java.lang.String mode) throws java.io.IOException;

    public long getMaxLength() throws java.io.IOException;

    public long setMaxLength(long maxlength) throws java.io.IOException;

}
