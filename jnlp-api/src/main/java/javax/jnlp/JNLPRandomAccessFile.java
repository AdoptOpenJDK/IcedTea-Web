package javax.jnlp;

public interface JNLPRandomAccessFile extends java.io.DataInput, java.io.DataOutput {

    public void close() throws java.io.IOException;

    public long length() throws java.io.IOException;

    public long getFilePointer() throws java.io.IOException;

    public int read() throws java.io.IOException;

    public int read(byte[] b, int off, int len) throws java.io.IOException;

    public int read(byte[] b) throws java.io.IOException;

    public void readFully(byte[] b) throws java.io.IOException;

    public void readFully(byte[] b, int off, int len) throws java.io.IOException;

    public int skipBytes(int n) throws java.io.IOException;

    public boolean readBoolean() throws java.io.IOException;

    public byte readByte() throws java.io.IOException;

    public int readUnsignedByte() throws java.io.IOException;

    public short readShort() throws java.io.IOException;

    public int readUnsignedShort() throws java.io.IOException;

    public char readChar() throws java.io.IOException;

    public int readInt() throws java.io.IOException;

    public long readLong() throws java.io.IOException;

    public float readFloat() throws java.io.IOException;

    public double readDouble() throws java.io.IOException;

    public java.lang.String readLine() throws java.io.IOException;

    public java.lang.String readUTF() throws java.io.IOException;

    public void seek(long pos) throws java.io.IOException;

    public void setLength(long newLength) throws java.io.IOException;

    public void write(int b) throws java.io.IOException;

    public void write(byte[] b) throws java.io.IOException;

    public void write(byte[] b, int off, int len) throws java.io.IOException;

    public void writeBoolean(boolean v) throws java.io.IOException;

    public void writeByte(int v) throws java.io.IOException;

    public void writeShort(int v) throws java.io.IOException;

    public void writeChar(int v) throws java.io.IOException;

    public void writeInt(int v) throws java.io.IOException;

    public void writeLong(long v) throws java.io.IOException;

    public void writeFloat(float v) throws java.io.IOException;

    public void writeDouble(double v) throws java.io.IOException;

    public void writeBytes(java.lang.String s) throws java.io.IOException;

    public void writeChars(java.lang.String s) throws java.io.IOException;

    public void writeUTF(java.lang.String str) throws java.io.IOException;

}
