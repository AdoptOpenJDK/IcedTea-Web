/* RhinoBasedPacEvaluator.java
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

package net.sourceforge.jnlp.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketPermission;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import net.sourceforge.jnlp.util.TimedHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Represents a Proxy Auto Config file. This object can be used to evaluate the
 * proxy file to find the proxy for a given url.
 *
 * @see http://en.wikipedia.org/wiki/Proxy_auto-config#The_PAC_file
 */
public class RhinoBasedPacEvaluator implements PacEvaluator {

    private final String pacHelperFunctionContents;
    private final String pacContents;
    private final URL pacUrl;
    private final TimedHashMap<String, String> cache;

    /**
     * Initialize a new object by using the PAC file located at the given URL.
     *
     * @param pacUrl the url of the PAC file to use
     */
    public RhinoBasedPacEvaluator(URL pacUrl) {
        if (JNLPRuntime.isDebug()) {
            System.err.println("Using the Rhino based PAC evaluator for url " + pacUrl);
        }
        pacHelperFunctionContents = getHelperFunctionContents();
        this.pacUrl = pacUrl;
        pacContents = getPacContents(pacUrl);
        cache = new TimedHashMap<String, String>();
    }

    /**
     * Get the proxies for accessing a given URL. The result is obtained by
     * evaluating the PAC file with the given url (and the host) as input.
     *
     * This method performs caching of the result.
     *
     * @param url the url for which a proxy is desired
     * @return a list of proxies in a string like
     * <pre>"PROXY foo.example.com:8080; PROXY bar.example.com:8080; DIRECT"</pre>
     *
     * @see #getProxiesWithoutCaching(URL)
     */
    public String getProxies(URL url) {
        String cachedResult = getFromCache(url);
        if (cachedResult != null) {
            return cachedResult;
        }

        String result = getProxiesWithoutCaching(url);
        addToCache(url, result);
        return result;
    }

    /**
     * Get the proxies for accessing a given URL. The result is obtained by
     * evaluating the PAC file with the given url (and the host) as input.
     *
     * @param url the url for which a proxy is desired
     * @return a list of proxies in a string like
     * <pre>"PROXY example.com:3128; DIRECT"</pre>
     *
     * @see #getProxies(URL)
     */
    private String getProxiesWithoutCaching(URL url) {
        if (pacHelperFunctionContents == null) {
            System.err.println("Error loading pac functions");
            return "DIRECT";
        }

        EvaluatePacAction evaluatePacAction = new EvaluatePacAction(pacContents, pacUrl.toString(),
                pacHelperFunctionContents, url);
        Permissions p = new Permissions();
        p.add(new RuntimePermission("accessClassInPackage.org.mozilla.javascript"));
        p.add(new SocketPermission("*", "resolve"));
        ProtectionDomain pd = new ProtectionDomain(null, p);
        AccessControlContext context = new AccessControlContext(new ProtectionDomain[] { pd });

        return AccessController.doPrivileged(evaluatePacAction, context);
    }

    /**
     * Returns the contents of file at pacUrl as a String.
     */
    private String getPacContents(URL pacUrl) {
        StringBuilder contents = null;
        try {
            String line = null;
            contents = new StringBuilder();
            BufferedReader pacReader = new BufferedReader(new InputStreamReader(pacUrl.openStream()));
            try {
                while ((line = pacReader.readLine()) != null) {
                    // System.out.println(line);
                    contents = contents.append(line).append("\n");
                }
            } finally {
                pacReader.close();
            }
        } catch (IOException e) {
            contents = null;
        }

        return (contents != null) ? contents.toString() : null;
    }

    /**
     * Returns the pac helper functions as a String. The functions are read
     * from net/sourceforge/jnlp/resources/pac-funcs.js
     */
    private String getHelperFunctionContents() {
        StringBuilder contents = null;
        try {
            String line;
            ClassLoader cl = this.getClass().getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            InputStream in = cl.getResourceAsStream("net/sourceforge/jnlp/runtime/pac-funcs.js");
            BufferedReader pacFuncsReader = new BufferedReader(new InputStreamReader(in));
            try {
                contents = new StringBuilder();
                while ((line = pacFuncsReader.readLine()) != null) {
                    // System.out.println(line);
                    contents = contents.append(line).append("\n");
                }
            } finally {
                pacFuncsReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            contents = null;
        }

        return (contents != null) ? contents.toString() : null;
    }

    /**
     * Gets an entry from the cache
     */
    private String getFromCache(URL url) {
        String lookupString = url.getProtocol() + "://" + url.getHost();
        String result = cache.get(lookupString);
        return result;
    }

    /**
     * Adds an entry to the cache
     */
    private void addToCache(URL url, String proxyResult) {
        String lookupString = url.getAuthority() + "://" + url.getHost();
        cache.put(lookupString, proxyResult);
    }

    /**
     * Helper classs to run remote javascript code (specified by the user as
     * PAC URL) inside a sandbox.
     */
    private static class EvaluatePacAction implements PrivilegedAction<String> {

        private String pacContents;
        private String pacUrl;
        private String pacFuncsContents;
        private URL url;

        public EvaluatePacAction(String pacContents, String pacUrl, String pacFuncsContents, URL url) {
            this.pacContents = pacContents;
            this.pacUrl = pacUrl;
            this.pacFuncsContents = pacFuncsContents;
            this.url = url;
        }

        public String run() {
            Context cx = Context.enter();
            try {
                /*
                 * TODO defense in depth.
                 *
                 * This is already running within a sandbox, but we can (and we
                 * should) lock it down further. Look into ClassShutter.
                 */
                Scriptable scope = cx.initStandardObjects();
                // any optimization level greater than -1 will trigger code generation
                // and this block will then need classloader permissions
                cx.setOptimizationLevel(-1);
                Object result = null;
                result = cx.evaluateString(scope, pacFuncsContents, "internal", 1, null);
                result = cx.evaluateString(scope, pacContents, pacUrl, 1, null);

                Object functionObj = scope.get("FindProxyForURL", scope);
                if (!(functionObj instanceof Function)) {
                    System.err.println("FindProxyForURL not found");
                    return null;
                } else {
                    Function findProxyFunction = (Function) functionObj;

                    Object[] args = { url.toString(), url.getHost() };
                    result = findProxyFunction.call(cx, scope, scope, args);
                    return (String) result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "DIRECT";
            } finally {
                Context.exit();
            }
        }
    }

}
