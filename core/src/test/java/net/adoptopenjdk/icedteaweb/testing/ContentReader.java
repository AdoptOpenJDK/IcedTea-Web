/* ContentReader.java
Copyright (C) 2011,2012 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.testing;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class to read content of stdout/stderr of process, and to cooperate with its
 * running/terminated/finished statuses.
 */
class ContentReader implements Runnable {

    private final StringBuilder sb = new StringBuilder();
    private final InputStream is;
    private final List<ContentReaderListener> listeners = new ArrayList<>(1);

    public String getContent() {
        return sb.toString();
    }

    public ContentReader(final InputStream is, final List<ContentReaderListener> l) {
        this.is = is;
        if (l != null) {
            this.listeners.addAll(l);
        }
    }


    @Override
    public void run() {
        try {
            final Reader br = new InputStreamReader(is, UTF_8);
            StringBuilder line = new StringBuilder();
            while (true) {
                int s = br.read();
                if (s < 0) {
                    if (line.length() > 0 && listeners != null) {
                        for (final ContentReaderListener listener : listeners) {
                            if (listener != null) {
                                listener.lineRead(line.toString());
                            }
                        }
                    }
                    break;
                }
                char ch = (char) s;
                sb.append(ch);
                line.append(ch);
                if (ch == '\n') {
                    if (listeners != null) {
                        for (final ContentReaderListener listener : listeners) {
                            if (listener != null) {
                                listener.lineRead(line.toString());
                            }
                        }
                    }
                    line = new StringBuilder();
                }
                if (listeners != null) {
                    for (final ContentReaderListener listener : listeners) {
                        if (listener != null) {
                            listener.charRead(ch);
                        }
                    }
                }
            }
        } catch (final NullPointerException ex) {
            ex.printStackTrace();
        }
        //do not want to bother output with terminations
        //mostly compaling when assassin kill the process about StreamClosed
        catch (final Exception ex) {
            // logException(ex);
            //ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (final Exception ex) {
                // ex.printStackTrace();
            }
        }
    }
}
