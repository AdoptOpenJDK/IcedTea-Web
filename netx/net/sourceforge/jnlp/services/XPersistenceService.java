// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.services;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.cache.*;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.*;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * The BasicService JNLP service.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
class XPersistenceService implements PersistenceService {

    // todo: recheck delete, etc to make sure security is tight

    protected XPersistenceService() {
    }

    /**
     * Checks whether the application has access to URL area
     * requested.  If the method returns normally then the specified
     * location can be accessed by the current application.
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    protected void checkLocation(URL location) throws MalformedURLException {
        ApplicationInstance app = JNLPRuntime.getApplication();
        if (app == null)
            throw new MalformedURLException("Cannot determine the current application.");

        URL source = app.getJNLPFile().getCodeBase();
        
        if (!source.getHost().equalsIgnoreCase(location.getHost())
                && !ServiceUtil.isSigned(app)) // Allow trusted application to have access to data from a different host
            throw new MalformedURLException(
                    "Untrusted application cannot access data from a different host.");


        // test for above codebase, not perfect but works for now

        String requestPath = location.getFile();
        if (-1 != requestPath.lastIndexOf("/"))
            requestPath = requestPath.substring(0, requestPath.lastIndexOf("/"));
        else
            requestPath = "";

          OutputController.getLogger().log("codebase path: " + source.getFile());
          OutputController.getLogger().log("request path: " + requestPath);

        if (!source.getFile().startsWith(requestPath) 
                && !ServiceUtil.isSigned(app)) // Allow trusted application to have access to data below source URL path
            throw new MalformedURLException(
                    "Cannot access data below source URL path.");
    }

    /**
     * Converts a URL into a file in the persistence store.
     *
     * @return the file
     */
    protected File toCacheFile(URL location) throws MalformedURLException {
        String pcache = JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_USER_PERSISTENCE_CACHE_DIR);
        return CacheUtil.urlToPath(location, pcache);
    }

    /**
     *
     * @return the maximum size of storage that got granted, in bytes
     * @throws MalformedURLException if the application cannot access the location
     */
    public long create(URL location, long maxsize) throws MalformedURLException, IOException {
        checkLocation(location);

        File file = toCacheFile(location);
        FileUtils.createParentDir(file, "Persistence store for "
                    + location.toString());

        if (file.exists())
            throw new IOException("File already exists.");

        FileUtils.createRestrictedFile(file, true);

        return maxsize;
    }

    /**
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    public void delete(URL location) throws MalformedURLException, IOException {
        checkLocation(location);

        FileUtils.deleteWithErrMesg(toCacheFile(location), " tocache");
    }

    /**
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    public FileContents get(URL location) throws MalformedURLException, IOException, FileNotFoundException {
        checkLocation(location);

        File file = toCacheFile(location);
        if (!file.exists()) {
            throw new FileNotFoundException("Persistence store for "
                    + location.toString() + " is not found.");
        }
        FileUtils.createParentDir(file, "Persistence store for "
                    + location.toString());

        return (FileContents) ServiceUtil.createPrivilegedProxy(FileContents.class, new XFileContents(file));
    }

    /**
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    public String[] getNames(URL location) throws MalformedURLException, IOException {
        checkLocation(location);

        File file = toCacheFile(location);
        if (!file.isDirectory())
            return new String[0];

        List<String> result = new ArrayList<String>();

        // check whether this is right: only add files and not directories.
        File entries[] = file.listFiles();
        for (int i = 0; i < entries.length; i++)
            if (entries[i].isFile())
                result.add(entries[i].getName());

        return result.toArray(new String[result.size()]);
    }

    /**
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    public int getTag(URL location) throws MalformedURLException, IOException {
        checkLocation(location);

        // todo: actually implement tags

        if (toCacheFile(location).exists())
            return PersistenceService.CACHED;

        return PersistenceService.CACHED;
    }

    /**
     *
     * @throws MalformedURLException if the application cannot access the location
     */
    public void setTag(URL location, int tag) throws MalformedURLException, IOException {
        checkLocation(location);

        // todo: actually implement tags
    }

}
