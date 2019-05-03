// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.xmlparser;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;

class XMLSanitizer {

    /**
     * Reads an xml file and removes the comments, leaving only relevant
     * xml code.
     *
     * @param in  The reader of the containing the xml.
     * @return  A new reader for the sanitized xml
     */
    static Reader sanitizeXml(final Reader in) {
        try {
            final PipedWriter pw = new PipedWriter();
            final PipedReader pr = new PipedReader(pw);
            new Thread(() -> {
                try {
                    sanitizeInput(in, pw);
                } finally {
                    try {
                        pw.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }).start();
            return pr;
        } catch (IOException e) {
            throw new RuntimeException("failed to setup piped reader/writer", e);
        }
    }

    private static void sanitizeInput(final Reader in, final Writer out) {
        try {
            final char[] buffer = new char[4];
            int charInBuffer = 0;
            boolean inComment = false;

            while (true) {
                // fill the buffer
                for (; charInBuffer < 4; charInBuffer++) {
                    final int ch = in.read();
                    if (ch == -1) {
                        // end of input stream
                        // write buffered content and flush
                        if (!inComment) {
                            for (int i = 0; i < charInBuffer; i++) {
                                out.write(buffer[i]);
                            }
                        }
                        out.flush();
                        return;
                    }
                    buffer[charInBuffer] = (char) ch;
                }

                // at this point the buffer is always full
                if (inComment) {
                    if (buffer[0] == '-' && buffer[1] == '-' && buffer[2] == '>') {
                        // end of comment
                        // prepare buffer for refilling
                        buffer[0] = buffer[3];
                        charInBuffer = 1;
                        inComment = false;
                    } else {
                        // shift buffer content one to the left
                        // and drop buffer[0]
                        buffer[0] = buffer[1];
                        buffer[1] = buffer[2];
                        buffer[2] = buffer[3];
                        charInBuffer--;
                    }
                } else {
                    if (buffer[0] == '<' && buffer[1] == '!' && buffer[2] == '-' && buffer[3] == '-') {
                        // start of comment
                        // prepare buffer for refilling
                        charInBuffer = 0;
                        inComment = true;
                    } else {
                        // shift buffer content one to the left
                        // and write out buffer[0]
                        out.write(buffer[0]);
                        out.flush();
                        buffer[0] = buffer[1];
                        buffer[1] = buffer[2];
                        buffer[2] = buffer[3];
                        charInBuffer--;
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error in XML", e);
        }
    }
}
