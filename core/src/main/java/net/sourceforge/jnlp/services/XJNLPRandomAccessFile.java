/* XJNLPRandomAccessFile.java
   Copyright (C) 2008 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */
package net.sourceforge.jnlp.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.jnlp.JNLPRandomAccessFile;

public class XJNLPRandomAccessFile implements JNLPRandomAccessFile {

    private RandomAccessFile raf;

    public XJNLPRandomAccessFile(File file, String mode) throws IOException {
        raf = new RandomAccessFile(file, mode);

    }

    public void close() throws IOException {
        raf.close();
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    public long length() throws IOException {
        return raf.length();
    }

    public int read() throws IOException {
        return raf.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return raf.read(b);
    }

    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    public byte readByte() throws IOException {
        return raf.readByte();
    }

    public char readChar() throws IOException {
        return raf.readChar();
    }

    public double readDouble() throws IOException {
        return raf.readDouble();
    }

    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    public void readFully(byte[] b) throws IOException {
        raf.readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        raf.readFully(b, off, len);
    }

    public int readInt() throws IOException {
        return raf.readInt();
    }

    public String readLine() throws IOException {
        return raf.readLine();
    }

    public long readLong() throws IOException {
        return raf.readLong();
    }

    public short readShort() throws IOException {
        return raf.readShort();
    }

    public String readUTF() throws IOException {
        return raf.readUTF();
    }

    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
        return raf.readUnsignedShort();
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    public void setLength(long newLength) throws IOException {
        raf.setLength(newLength);
    }

    public int skipBytes(int n) throws IOException {
        return raf.skipBytes(n);
    }

    public void write(int b) throws IOException {
        raf.write(b);

    }

    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOException {
        raf.writeBoolean(v);
    }

    public void writeByte(int v) throws IOException {
        raf.writeByte(v);
    }

    public void writeBytes(String s) throws IOException {
        raf.writeBytes(s);
    }

    public void writeChar(int v) throws IOException {
        raf.writeChar(v);
    }

    public void writeChars(String s) throws IOException {
        raf.writeChars(s);
    }

    public void writeDouble(double v) throws IOException {
        raf.writeDouble(v);
    }

    public void writeFloat(float v) throws IOException {
        raf.writeFloat(v);
    }

    public void writeInt(int v) throws IOException {
        raf.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        raf.writeLong(v);
    }

    public void writeShort(int v) throws IOException {
        raf.writeShort(v);
    }

    public void writeUTF(String str) throws IOException {
        raf.writeUTF(str);
    }

}
