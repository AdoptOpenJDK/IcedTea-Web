/* DownloadService.java
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.DownloadService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

public class DownloadServiceRunner {

    URL serverUrl = null;
    URL extensionUrl = null;
    URL NonExistingUrl = null;

    URL urlToExternalResource = null;

    /**
     * Launching jnlp and extension jnlp PARTS
     */
    final String launchPartOne = "one";
    final String launchPartTwo = "two";
    final String extensionPartOne = "extOne";
    final String nonExistingPart = "random";

    /**
     * Parts in Array
     */
    final String[] validLaunchParts = { launchPartOne, launchPartTwo };
    final String[] halfValidLaunchParts = { launchPartOne, nonExistingPart };
    final String[] validExtensionParts = { extensionPartOne };
    final String[] halfValidExtensionParts = { extensionPartOne, nonExistingPart };
    final String[] invalidParts = { nonExistingPart, "random2" };

    private static DownloadService downloadService;
    static {
        try {
            downloadService = (DownloadService) ServiceManager.lookup("javax.jnlp.DownloadService");
        } catch (UnavailableServiceException ex) {
            System.err.println("DownloadService is not available.");
        }
    }

    public DownloadServiceRunner(String urlToServer) throws MalformedURLException, InterruptedException {
        serverUrl = new URL(urlToServer);
        extensionUrl = new URL(urlToServer + "DownloadServiceExtension.jnlp");
        NonExistingUrl = new URL(urlToServer + "NONEXISTINGFILE.JNLP");

        urlToExternalResource = new URL(urlToServer + "EmptySignedJar.jar");

        System.out.println(urlToExternalResource.toString());

    }

    /**
     * Checks the cache status of resources using isPartCached()
     */
    private void checkCache() throws MalformedURLException {
        System.out.println("CHECKCACHE-isPartCached: LaunchPartOne: " + downloadService.isPartCached(launchPartOne));
        System.out.println("CHECKCACHE-isPartCached: LaunchPartTwo: " + downloadService.isPartCached(launchPartTwo));
        System.out.println("CHECKCACHE-isPartCached: NonExistingPart: " + downloadService.isPartCached(nonExistingPart));
    }

    /**
     * Checks the cache status of resources using isPartCached([]) - an array with part names
     */
    private void checkCacheUsingMultipleParts() throws MalformedURLException {
        System.out.println("CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): ValidLaunchParts: " + downloadService.isPartCached(validLaunchParts));
        System.out.println("CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): HalfValidLaunchParts: " + downloadService.isPartCached(halfValidLaunchParts));
        System.out.println("CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): InvalidParts: " + downloadService.isPartCached(invalidParts));
    }

    /**
     * Checks the cache status of extension resources using isExtensionPartCached()
     */
    private void checkExtensionCache() throws MalformedURLException {
        System.out.println("CHECKEXTENSIONCACHE-isExtensionPartCached: ExtensionPartOne: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
        System.out.println("CHECKEXTENSIONCACHE-isExtensionPartCached: NonExistingPart: "
                + downloadService.isExtensionPartCached(extensionUrl, null, nonExistingPart));
        System.out.println("CHECKEXTENSIONCACHE-isExtensionPartCached: NonExistingUrl: "
                + downloadService.isExtensionPartCached(NonExistingUrl, null, extensionPartOne));
    }

    /**
     * Checks the cache status of extension resources using isExtensionPartCached([]) - an array with part names
     */
    private void checkExtensionCacheUsingMultipleParts() throws MalformedURLException {
        System.out.println("CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): ValidExtensionParts: "
                + downloadService.isExtensionPartCached(extensionUrl, null, validExtensionParts));
        System.out.println("CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): HalfValidExtensionParts: "
                + downloadService.isExtensionPartCached(extensionUrl, null, halfValidExtensionParts));
        System.out.println("CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): InvalidParts: "
                + downloadService.isExtensionPartCached(NonExistingUrl, null, invalidParts));
    }

    /**
     * Checks the cache status of external (not mentioned in jnlps) resources using isResourceCached()
     */
    private void checkExternalCache() {
        System.out.println("CHECKEXTERNALCACHE-isResourceCached: UrlToExternalResource: " + downloadService.isResourceCached(urlToExternalResource, null));
        System.out.println("CHECKEXTERNALCACHE-isResourceCached: NonExistingUrl: " + downloadService.isResourceCached(NonExistingUrl, null));
    }

    /**
     * Removes resources from cache using removePart()
     */
    private void removePart() throws IOException {
        System.out.println("REMOVEPART-removePart: LaunchPartOne-BEFORE: " + downloadService.isPartCached(launchPartOne));
        downloadService.removePart(launchPartOne);
        System.out.println("REMOVEPART-removePart: LaunchPartOne-AFTER: " + downloadService.isPartCached(launchPartOne));

        System.out.println("REMOVEPART-removePart: LaunchPartTwo-BEFORE: " + downloadService.isPartCached(launchPartTwo));
        downloadService.removePart(launchPartTwo);
        System.out.println("REMOVEPART-removePart: LaunchPartTwo-AFTER: " + downloadService.isPartCached(launchPartTwo));
    }

    /**
     * Removes extension resources from cache using isExtensionPartCached()
     */
    private void removeExtensionPart() throws IOException {
        System.out.println("REMOVEEXTENSIONPART-removeExtensionPart: ExtensionPartOne-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
        downloadService.removeExtensionPart(extensionUrl, null, extensionPartOne);
        System.out.println("REMOVEEXTENSIONPART-removeExtensionPart: ExtensionPartOne-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
    }

    /**
     * Removes extension resources using part array (all parts exist) from cache using isExtensionPartCached()
     */
    private void removeExtensionUsingValidPartInArray() throws IOException {
        System.out.println("REMOVEEXTENSIONUSINGVALIDPARTINARRAY-removeExtensionPart(Array): ValidExtensionParts-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

        downloadService.removeExtensionPart(extensionUrl, null, validExtensionParts);

        System.out.println("REMOVEEXTENSIONUSINGVALIDPARTINARRAY-removeExtensionPart(Array): ValidExtensionParts-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

    }

    /**
     * Removes extension resources using part array (one part exists, the other one does not) from cache using isExtensionPartCached()
     */
    private void removeExtensionUsingHalfValidPartInArray() throws IOException {
        System.out.println("REMOVEEXTENSIONUSINGHALFVALIDPARTINARRAY-removeExtensionPart(Array): HalfValidExtensionParts-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

        downloadService.removeExtensionPart(extensionUrl, null, halfValidExtensionParts);

        System.out.println("REMOVEEXTENSIONUSINGHALFVALIDPARTINARRAY-removeExtensionPart(Array): HalfValidExtensionParts-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
    }

    /**
     * Removes external (not mentioned in jnlps) resources from cache using removeResource()
     */
    private void removeExternalResource() throws IOException {
        System.out.println("REMOVEEXTERNALPART-removeResource: UrlToExternalResource-BEFORE: " + downloadService.isResourceCached(urlToExternalResource, null));
        downloadService.removeResource(urlToExternalResource, null);
        System.out.println("REMOVEEXTERNALPART-removeResource: UrlToExternalResource-AFTER: " + downloadService.isResourceCached(urlToExternalResource, null));
    }

    /**
     * Loads resources from cache using loadPart()
     */
    private void loadPart() throws IOException {
        System.out.println("LOADPART-loadPart: LaunchPartOne-BEFORE: " + downloadService.isPartCached(launchPartOne));
        downloadService.loadPart(launchPartOne, null);
        System.out.println("LOADPART-loadPart: LaunchPartOne-AFTER: " + downloadService.isPartCached(launchPartOne));

        System.out.println("LOADPART-loadPart: LaunchPartTwo-BEFORE: " + downloadService.isPartCached(launchPartTwo));
        downloadService.loadPart(launchPartTwo, null);
        System.out.println("LOADPART-loadPart: LaunchPartTwo-AFTER: " + downloadService.isPartCached(launchPartTwo));
    }

    /**
     * Load extension resources from cache using loadExtensionPart()
     */
    private void loadExtensionPart() throws IOException {
        System.out.println("LOADEXTENSIONPART-loadExtensionPart: ExtensionPartOne-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
        downloadService.loadExtensionPart(extensionUrl, null, extensionPartOne, null);
        System.out.println("LOADEXTENSIONPART-loadExtensionPart: ExtensionPartOne-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
    }

    /**
     * Loads extension resources using part array (all parts exist) from cache using isExtensionPartCached()
     */
    private void loadExtensionUsingValidPartInArray() throws IOException {
        System.out.println("LOADEXTENSIONUSINGVALIDPARTINARRAY-loadExtensionPart(Array): ValidExtensionParts-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

        downloadService.loadExtensionPart(extensionUrl, null, validExtensionParts, null);

        System.out.println("LOADEXTENSIONUSINGVALIDPARTINARRAY-loadExtensionPart(Array): ValidExtensionParts-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

    }

    /**
     * Loads extension resources using part array (one part exists, the other one does not) from cache using isExtensionPartCached()
     */
    private void loadExtensionUsingHalfValidPartInArray() throws IOException {
        System.out.println("LOADEXTENSIONUSINGHALFVALIDPARTINARRAY-loadExtensionPart(Array): HalfValidExtensionParts-BEFORE: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));

        downloadService.loadExtensionPart(extensionUrl, null, halfValidExtensionParts, null);

        System.out.println("LOADEXTENSIONUSINGHALFVALIDPARTINARRAY-loadExtensionPart(Array): HalfValidExtensionParts-AFTER: "
                + downloadService.isExtensionPartCached(extensionUrl, null, extensionPartOne));
    }

    /**
     * Loads external (not mentioned in jnlps) resources from cache using removeResource()
     */
    private void loadExternalResource() throws IOException {
        System.out.println("LOADEXTERNALRESOURCE-loadResource: UrlToExternalResource-BEFORE: " + downloadService.isResourceCached(urlToExternalResource, null));
        downloadService.loadResource(urlToExternalResource, null, null);
        System.out.println("LOADEXTERNALRESOURCE-loadResource: UrlToExternalResource-AFTER: " + downloadService.isResourceCached(urlToExternalResource, null));
    }

    /**
     * Repeatedly unloads and loads jars
     */
    private void repeatedlyLoadingAndUnloadingJars() throws IOException {
        downloadService.removePart(launchPartOne);
        downloadService.loadPart(launchPartOne, null);

        downloadService.removePart(launchPartOne);
        System.out.println("MULTIPLEMETHODCALLS - removePart: LaunchPartOne: " + downloadService.isPartCached(launchPartOne));

        downloadService.loadPart(launchPartOne, null);
        System.out.println("MULTIPLEMETHODCALLS - loadPart: LaunchPartOne: " + downloadService.isPartCached(launchPartOne));
    }

    /**
     * Repeatedly unloads and loads external jars
     */
    private void repeatedlyLoadingAndUnloadingExternalJars() throws IOException {
        downloadService.removeResource(urlToExternalResource, null);
        downloadService.loadResource(urlToExternalResource, null, null);

        downloadService.removeResource(urlToExternalResource, null);
        System.out.println("MULTIPLEMETHODCALLS - removeResource: UrlToExternalResource: " + downloadService.isResourceCached(urlToExternalResource, null));

        downloadService.loadResource(urlToExternalResource, null, null);
        System.out.println("MULTIPLEMETHODCALLS - loadResource: UrlToExternalResource: " + downloadService.isResourceCached(urlToExternalResource, null));
    }

    /**
     * Loads external jar as preparation for external resource testing
     */
    private void prepareExternalResourceTests() {
        try {
            if (!downloadService.isResourceCached(urlToExternalResource, null))
                downloadService.loadResource(urlToExternalResource, null, null);
        } catch (Exception e) {
            //Continue testing
            // This is okay to ignore as it may be a problem with loadResouce( ), which will be identified within tests
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Running DownloadService..");

        if (args.length < 2) {
            System.out.println("Requires 2 arguments: [server_url] [checkCache | manageJars | manageExternalJars]");
            System.out.println("Exiting..");
            return;
        }

        DownloadServiceRunner ds = new DownloadServiceRunner(args[0]);

        if (args[1].equals("checkCache")) {
            //Cache Resources
            ds.checkCache();
            ds.checkCacheUsingMultipleParts();
            ds.checkExtensionCache();
            ds.checkExtensionCacheUsingMultipleParts();
        }

        if (args[1].equals("manageJnlpJars")) {
            //Remove Resources
            ds.removePart();
            ds.removeExtensionPart();

            //Load Resources
            ds.loadPart();
            ds.loadExtensionPart();

            //Manage using multiple part arrays
            ds.removeExtensionUsingValidPartInArray();
            ds.loadExtensionUsingValidPartInArray();
            ds.removeExtensionUsingHalfValidPartInArray();
            ds.loadExtensionUsingHalfValidPartInArray();

            //Unloads and loads jars repeatedly
            ds.repeatedlyLoadingAndUnloadingJars();

        } else if (args[1].equals("manageExternalJars")) {
            ds.prepareExternalResourceTests();
            ds.checkExternalCache();
            ds.removeExternalResource();
            ds.loadExternalResource();

            //Unloads and loads jars repeatedly
            ds.repeatedlyLoadingAndUnloadingExternalJars();
        }

        System.out.println("Exiting DownloadService..");
    }
}
