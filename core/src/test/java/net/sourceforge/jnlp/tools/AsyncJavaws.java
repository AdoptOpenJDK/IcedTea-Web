/* AsyncJavaws.java
 Copyright (C)  2012 Red Hat, Inc.

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
package net.sourceforge.jnlp.tools;

import java.util.List;
import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;

/**
 * You can see ClipboardContext reproducers as examples
 *
 */
public class AsyncJavaws implements Runnable {

    private final boolean headless;
    private final String url;
    private ProcessResult result;
    private ContentReaderListener contentReaderListener;
    private ContentReaderListener errorReaderListener;
    private final List<String> argList;
    private final ServerAccess server;

    public AsyncJavaws(ServerAccess server, String url, List<String> argList, boolean headless, ContentReaderListener contentReaderListener, ContentReaderListener errorReaderListener) {
        this.url = url;
        this.headless = headless;
        this.contentReaderListener = contentReaderListener;
        this.errorReaderListener = errorReaderListener;
        this.argList = argList;
        this.server = server;
        Assert.assertNotNull(server);
    }

    @Override
    public void run() {
        try {
            if (headless) {
                result = server.executeJavawsHeadless(argList, url, contentReaderListener, errorReaderListener, null);
            } else {
                result = server.executeJavaws(argList, url, contentReaderListener, errorReaderListener);
            }
        } catch (Exception ex) {
            if (result == null) {
                result = new ProcessResult("", ex.getMessage(), null, true, 1, ex);
            }
            throw new RuntimeException(ex);
        }
    }

    public ProcessResult getResult() {
        return result;
    }
}
