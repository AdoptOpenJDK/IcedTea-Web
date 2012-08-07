/* SecurityDialogMessage.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp.security;

import java.security.cert.X509Certificate;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.DialogType;

/**
 * Represents a message to the security framework to show a specific security
 * dialog
 */
final class SecurityDialogMessage {

    /*
     * These fields contain information need to display the correct dialog type
     */

    public DialogType dialogType;
    public AccessType accessType;
    public JNLPFile file;
    public CertVerifier certVerifier;
    public X509Certificate certificate;
    public Object[] extras;

    /*
     * Volatile because this is shared between threads and we dont want threads
     * to use a cached value of this.
     */
    public volatile Object userResponse;

    /*
     * These two fields are used to block/unblock the application or the applet.
     * If either of them is not null, call release() or dispose() on it to allow
     * the application/applet to continue.
     */

    public Semaphore lock;
    public JDialog toDispose;

}
