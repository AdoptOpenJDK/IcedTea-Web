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

package net.sourceforge.jnlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class to read content of stdout/stderr of process, and to cooperate with its running/terminated/finished statuses.
 */
class ContentReader implements Runnable {

    StringBuilder sb = new StringBuilder();
    private final InputStream is;
    private boolean done;
    ContentReaderListener listener;

    public String getContent() {
        return sb.toString();
    }

    public ContentReader(InputStream is) throws IOException {
        this.is = is;
    }

    public ContentReader(InputStream is, ContentReaderListener l) throws IOException {
        this.is = is;
        this.listener = l;
    }

    public void setListener(ContentReaderListener listener) {
        this.listener = listener;
    }

    public ContentReaderListener getListener() {
        return listener;
    }

    /**
     * Blocks until the copy is complete, or until the thread is interrupted
     */
    public synchronized void waitUntilDone() throws InterruptedException {
        boolean interrupted = false;
        // poll interrupted flag, while waiting for copy to complete
        while (!(interrupted = Thread.interrupted()) && !done) {
            wait(1000);
        }
        if (interrupted) {
            ServerAccess.logNoReprint("Stream copier: throwing InterruptedException");
            //throw new InterruptedException();
        }
    }

    @Override
    public void run() {
        try {
            Reader br = new InputStreamReader(is, "UTF-8");
            StringBuilder line = new StringBuilder();
            while (true) {
                int s = br.read();
                if (s < 0) {
                    if (line.length() > 0 && listener != null) {
                        listener.lineReaded(line.toString());
                    }
                    break;
                }
                char ch = (char) s;
                sb.append(ch);
                line.append(ch);
                if (ch == '\n') {
                    if (listener != null) {
                        listener.lineReaded(line.toString());
                    }
                    line = new StringBuilder();
                }
                if (listener != null) {
                    listener.charReaded(ch);
                }
            }
            //do not want to bother output with terminations
            //mostly compaling when assassin kill the process about StreamClosed
            //do not want to bother output with terminations
            //mostly compaling when assassin kill the process about StreamClosed
        } catch (Exception ex) {
            // logException(ex);
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
                // ex.printStackTrace();
            } finally {
                done = true;
            }
        }
    }
}
