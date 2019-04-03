/* VariableX509TrustManagerJDK7.java
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

package net.sourceforge.jnlp.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;

public class VariableX509TrustManagerJDK7 extends X509ExtendedTrustManager {

    private VariableX509TrustManager vX509TM = VariableX509TrustManager.getInstance();

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        vX509TM.checkTrustClient(chain, authType, null /* hostname*/);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        vX509TM.checkTrustServer(chain, authType, null /* hostname*/, null /* socket */, null /* engine */);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return vX509TM.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkTrustClient(chain, authType, socket, null);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkTrustServer(chain, authType, socket, null);

    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkTrustClient(chain, authType, null, engine);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkTrustServer(chain, authType, null, engine);
    }

    /**
     * Check if the server is trusted
     *
     * @param chain The cert chain
     * @param authType The auth type algorithm
     * @param socket the SSLSocket, may be null
     * @param engine the SSLEngine, may be null
     */
    private void checkTrustServer(X509Certificate[] chain,
                             String authType, Socket socket,
                             SSLEngine engine) throws CertificateException {

        String hostName = null;

        if (socket != null) {
            hostName = ((SSLSocket) socket).getHandshakeSession().getPeerHost();
        } else if (engine != null) {
            hostName = engine.getHandshakeSession().getPeerHost();
        }

        vX509TM.checkTrustServer(chain, authType, hostName, (SSLSocket) socket, engine);
    }

    /**
     * Check if the client is trusted
     *
     * @param chain The cert chain
     * @param authType The auth type algorithm
     * @param socket the SSLSocket, if provided
     * @param engine the SSLEngine, if provided
     */
    private void checkTrustClient(X509Certificate[] chain,
                             String authType, Socket socket,
                             SSLEngine engine) throws CertificateException {

        String hostName = null;

        if (socket != null) {
            hostName = ((SSLSocket) socket).getHandshakeSession().getPeerHost();
        } else if (engine != null) {
            hostName = engine.getHandshakeSession().getPeerHost();
        }

        vX509TM.checkTrustClient(chain, authType, hostName);
    }
}
