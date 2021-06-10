/* JNLPAuthenticator
   Copyright (C) 2008  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */

package net.sourceforge.jnlp.security;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.adoptopenjdk.icedteaweb.resources.DaemonThreadPoolProvider.globalFixedThreadPool;

public class JNLPAuthenticator extends Authenticator {

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        // No security check is required here, because the only way to set
        // parameters for which auth info is needed
        // (Authenticator:requestPasswordAuthentication()), has a security check

        String type = this.getRequestorType() == RequestorType.PROXY ? "proxy" : "web";

        String host = getRequestingHost();
        int port = getRequestingPort();
        String prompt = getRequestingPrompt();

        final CacheKey key = new CacheKey(host, port, prompt, type);
        final PasswordAuthentication cached = ShortTermCache.getCached(key);
        if (cached != null) {
            return cached;
        }

        NamePassword response = SecurityDialogs.showAuthenticationPrompt(host, port, prompt, type);
        if (response == null) {
            return null;
        } else {
            final PasswordAuthentication result = new PasswordAuthentication(response.getName(), response.getPassword());
            ShortTermCache.putIntoCacheForFiveSeconds(key, result);
            return result;
        }
    }

    private static class ShortTermCache {

        private static final Map<CacheKey, PasswordAuthentication> store = new HashMap<>();

        static PasswordAuthentication getCached(final CacheKey key) {
            return store.get(key);
        }

        static void putIntoCacheForFiveSeconds(final CacheKey key, final PasswordAuthentication value) {
            store.put(key, value);

            globalFixedThreadPool().submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {
                    // do nothing
                }
                store.remove(key);
            });
        }
    }

    private static final class CacheKey {
        private final String host;
        private final int port;
        private final String prompt;
        private final String type;

        private CacheKey(final String host, final int port, final String prompt, final String type) {
            this.host = host;
            this.port = port;
            this.prompt = prompt;
            this.type = type;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return port == cacheKey.port &&
                    Objects.equals(host, cacheKey.host) &&
                    Objects.equals(prompt, cacheKey.prompt) &&
                    Objects.equals(type, cacheKey.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port, prompt, type);
        }
    }

}
