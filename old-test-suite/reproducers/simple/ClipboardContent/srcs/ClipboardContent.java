/* ClipboardContent.java
Copyright (C) 2012 Red Hat, Inc.

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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class ClipboardContent extends JPanel {

    private static final String contentC = "COPY#$REPRODUCER";
    private static final String contentP = "PASTE#$REPRODUCER";

    private static class LocalFrame extends JFrame {

        JTextField t;

        public LocalFrame(String str) {
            super();
            t = new JTextField(str);
            this.add(t);
            this.setSize(100, 100);
            this.pack();
            t.selectAll();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        public void run() throws InterruptedException {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setVisible(true);

                }
            });
            while (!this.isVisible()) {
                Thread.sleep(100);
            }
        }

        public JTextField getT() {
            return t;
        }
    }

    public void putToClipboard1(String str) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(str);
        clipboard.setContents(strSel, null);
        printFlavors();
    }

    public void putToClipboard2(final String str) throws InterruptedException, NoSuchMethodException, IllegalAccessException, UnsupportedFlavorException, IllegalArgumentException, InvocationTargetException, IOException {
        final LocalFrame lf = new LocalFrame(str);
        lf.run();
        ((JTextComponent) (lf.getT())).copy();
        printFlavors();
        lf.dispose();
    }

    public String pasteFromClipboard2() throws InterruptedException, NoSuchMethodException, IllegalAccessException, UnsupportedFlavorException, IllegalArgumentException, InvocationTargetException, IOException {
        final LocalFrame lf = new LocalFrame("xxx");
        lf.run();
        ((JTextComponent) (lf.getT())).paste();
        printFlavors();
        String s = lf.getT().getText();
        lf.dispose();
        return s;
    }

    private void printFlavors() {
//just for debugging
//        Toolkit toolkit = Toolkit.getDefaultToolkit();
//        Clipboard clipboard = toolkit.getSystemClipboard();
//        Transferable clipData = clipboard.getContents(clipboard);
//        DataFlavor[] cd = clipData.getTransferDataFlavors();
//        for (DataFlavor dataFlavor : cd) {
//            System.out.println(dataFlavor.getMimeType());
//        }
    }

    public String pasteFromClipboard1() throws UnsupportedFlavorException, IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        Transferable clipData = clipboard.getContents(clipboard);
        printFlavors();
        String s = (String) (clipData.getTransferData(
                DataFlavor.stringFlavor));
        return s;
    }

    public static void main(String[] args) throws Exception {
        ClipboardContent cl = new ClipboardContent();
        if (args.length == 0) {
            throw new IllegalArgumentException("at least copy1|2 or paste1|2 must be as argument (+mandatory number giving use timeout in seconds before termination)");
        } else if (args.length == 1) {
            cl.proceed(args[0]);
        } else {
            cl.proceed(args[0], args[1]);
        }

    }

    public void proceed(String arg) throws Exception {
        proceed(arg, 0);
    }

    public void proceed(String arg, String keepAliveFor) throws Exception {
        proceed(arg, Long.valueOf(keepAliveFor));
    }

    public void proceed(String arg, long timeOut) throws Exception {
        if (arg.equals("copy1")) {
            System.out.println(this.getClass().getName() + " copying1 to clipboard " + contentC);
            putToClipboard1(contentC);
            System.out.println(this.getClass().getName() + " copied1 to clipboard " + pasteFromClipboard1());
        } else if (arg.equals("paste1")) {
            System.out.println(this.getClass().getName() + " pasting1 from clipboard ");
            String nwContent = pasteFromClipboard1();
            System.out.println(this.getClass().getName() + " pasted1 from clipboard " + nwContent);
        } else if (arg.equals("copy2")) {
            System.out.println(this.getClass().getName() + " copying2 to clipboard " + contentC);
            putToClipboard2(contentC);
            System.out.println(this.getClass().getName() + " copied2 to clipboard " + pasteFromClipboard2());
        } else if (arg.equals("paste2")) {
            System.out.println(this.getClass().getName() + " pasting2 from clipboard ");
            String nwContent = pasteFromClipboard2();
            System.out.println(this.getClass().getName() + " pasted2 from clipboard " + nwContent);
        } else {
            throw new IllegalArgumentException("supported copy1|2 paste1|2");
        }
        long start = System.nanoTime();
        while (TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) < timeOut) {
            Thread.sleep(500);
        }
    }
}
