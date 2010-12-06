/* XPrintService.java
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

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.jnlp.*;
import javax.swing.JOptionPane;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

public class XPrintService implements PrintService {

    // If pj is null, then we do not have a printer to use.
    private PrinterJob pj;

    public XPrintService() {
        pj = PrinterJob.getPrinterJob();
    }

    public PageFormat getDefaultPage() {
        if (pj != null)
            return pj.defaultPage();
        else {
            showWarning();
            return new PageFormat(); // might not have default settings.
        }
    }

    public PageFormat showPageFormatDialog(PageFormat page) {
        if (pj != null)
            return pj.pageDialog(page);
        else {
            showWarning();
            return page;
        }

    }

    public boolean print(Pageable document) {
        if (pj != null) {
            pj.setPageable(document);
            if (pj.printDialog()) {
                try {
                    pj.print();
                    return true;
                } catch (PrinterException pe) {
                    System.err.println("Could not print: " + pe);
                    return false;
                }
            }
        } else
            showWarning();

        return false;
    }

    public boolean print(Printable painter) {
        if (pj != null) {
            pj.setPrintable(painter);
            if (pj.printDialog()) {
                try {
                    pj.print();
                    return true;
                } catch (PrinterException pe) {
                    System.err.println("Could not print: " + pe);
                    return false;
                }

            }
        } else
            showWarning();

        return false;
    }

    private void showWarning() {
        JOptionPane.showMessageDialog(null,
                                "Unable to find a default printer.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
        System.err.println("Unable to print: Unable to find default printer.");
    }
}
