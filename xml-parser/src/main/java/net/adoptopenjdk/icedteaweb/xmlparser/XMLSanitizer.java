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

import java.io.Reader;

class XMLSanitizer {

    /**
     * Reads an xml file and removes the comments, leaving only relevant
     * xml code.
     *
     * @param in The reader of the containing the xml.
     * @return A new reader for the sanitized xml
     */
    static String sanitizeXml(final Reader in) {
        try {
            final StringBuilder result = new StringBuilder();
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
                                result.append(buffer[i]);
                            }
                        }
                        return result.toString().trim();
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
                    // jnlp files from a MFSys25  contain '<?-- JViewerVersion 3.30a -->' therefore we must treat ! and ? equally
                    if (buffer[0] == '<' && (buffer[1] == '!' || buffer[1] == '?') && buffer[2] == '-' && buffer[3] == '-') {
                        // start of comment
                        // prepare buffer for refilling
                        charInBuffer = 0;
                        inComment = true;
                    } else {
                        // shift buffer content one to the left
                        // and write out buffer[0]
                        result.append(buffer[0]);
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
