/* 
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
package net.adoptopenjdk.icedteaweb.client.parts.browser;

import java.net.Authenticator;
import java.net.ProxySelector;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import net.adoptopenjdk.icedteaweb.client.console.JavaConsole;
import net.sourceforge.jnlp.browser.BrowserAwareProxySelector;
import net.sourceforge.jnlp.security.JNLPAuthenticator;

import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;

public class LinkingBrowser extends JTabbedPane {

    private final HtmlBrowserPanel browser;
    private final LinksPanel linksPanel = new LinksPanel();

    public LinkingBrowser(final URL url) {
        this(url, false);
    }

    public LinkingBrowser(final String url) {
        this(url, false);
    }

    public LinkingBrowser(final URL url, boolean socket) {
        super();
        browser = new HtmlBrowserPanel(url, socket);
        createGui(browser);
    }

    public LinkingBrowser(final String url, boolean socket) {
        super();
        browser = new HtmlBrowserPanel(url, socket);
        createGui(browser);
    }

    private void createGui(HtmlBrowserPanel lBrowser) {
        linksPanel.addressChanged(lBrowser.getUrl());
        linksPanel.parseAndAdd(lBrowser.getCurrentSource());
        linksPanel.setInternalBrowser(lBrowser);
        lBrowser.addDocumentChangedListener(linksPanel);
        this.addTab("html", lBrowser); //jeditorpane
        this.addTab("links", new JScrollPane(linksPanel)); // set of found links - copyToClipboard, execute in browser, show...

    }

    public static void showStandAloneWindow(String url, boolean socket) {
        if (JavaConsole.canShowOnStartup(true)) {
            JavaConsole.getConsole().showConsoleLater();
        }
        // plug in a custom authenticator and proxy selector
        Authenticator.setDefault(new JNLPAuthenticator());
        BrowserAwareProxySelector proxySelector = new BrowserAwareProxySelector(getConfiguration());
        proxySelector.initialize();
        ProxySelector.setDefault(proxySelector);
        createFrame(url, socket, JFrame.EXIT_ON_CLOSE);
    }

    public static void createFrame(String url, boolean socket, int action) {
        HtmlBrowserPanel.warn();
        JFrame f = new JFrame();
        f.add(new LinkingBrowser(url, socket));
        f.pack();
        f.setDefaultCloseOperation(action);
        f.setVisible(true);
    }

    public HtmlBrowserPanel getBrowser() {
        return browser;
    }

    public LinksPanel getLinksPanel() {
        return linksPanel;
    }

}
