/*
 * Copyright 2012 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

public class JNLPFileFactory {

    /**
     * Create a JNLPFile from a URL.
     *
     * @param location the location of the JNLP file
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile create(final URL location) throws IOException, ParseException {
        return create(location, new ParserSettings());
    }

    /**
     * Create a JNLPFile from a URL checking for updates using the
     * default policy.
     *
     * @param location the location of the JNLP file
     * @param settings the parser settings to use while parsing the file
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile create(final URL location, final ParserSettings settings) throws IOException, ParseException {
        return create(location, null, settings, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version  the version of the JNLP file
     * @param settings the {@link ParserSettings} to use when parsing the {@code location}
     * @param policy   the update policy
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile create(final URL location, final VersionString version, final ParserSettings settings, UpdatePolicy policy) throws IOException, ParseException {
        final String uniqueKey = Calendar.getInstance().getTimeInMillis() + "-" + ((int) (Math.random() * Integer.MAX_VALUE)) + "-" + location;
        return create(location, uniqueKey, version, settings, policy);
    }

    /**
     * Create a JNLPFile from a URL, parent URLm a version and checking for
     * updates using the specified policy.
     *
     * @param location  the location of the JNLP file
     * @param uniqueKey A string that uniquely identifies connected instances
     * @param version   the version of the JNLP file
     * @param settings  the parser settings to use while parsing the file
     * @param policy    the update policy
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile create(final URL location, final String uniqueKey, final VersionString version, final ParserSettings settings, final UpdatePolicy policy) throws IOException, ParseException {
        try (InputStream input = openURL(location, version, policy)) {
            return new JNLPFile(input, location, null, settings, uniqueKey);
        }
    }

    /**
     * Open the jnlp file URL from the cache if there, otherwise
     * download to the cache.
     * Unless file is up-to-date in cache, this method blocks until it is downloaded.
     *
     * @param location of resource to open
     * @param version  of resource
     * @param policy   update policy of resource
     * @return opened stream from given url
     * @throws IOException if something goes wrong
     */
    protected InputStream openURL(final URL location, final VersionString version, final UpdatePolicy policy) throws IOException {
        Assert.requireNonNull(location, "location");
        Assert.requireNonNull(policy, "policy");

        try {
            final ResourceTracker tracker = new ResourceTracker(false, DownloadOptions.NONE, policy); // no prefetch
            tracker.addResource(location, version);
            final File f = tracker.getCacheFile(location);
            return new FileInputStream(f);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

}
