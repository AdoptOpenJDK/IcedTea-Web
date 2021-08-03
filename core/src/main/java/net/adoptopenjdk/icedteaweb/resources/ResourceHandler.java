package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;
import net.sourceforge.jnlp.deploymentrules.UrlDeploymentRulesSetUtils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.ERROR;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;
import static net.sourceforge.jnlp.util.UrlUtils.decodeUrlQuietly;
/*
 * Modified class to add DeploymentRuleSet 
 * DJ -03-02-2021
 */
class ResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);

    private final Resource resource;

    ResourceHandler(Resource resource) {
        this.resource = Assert.requireNonNull(resource, "resource");
    }

    Future<Resource> putIntoCache(final Executor downloadExecutor) {
        LOG.debug("Will check and maybe put into cache: {}", resource.getSimpleName());
        validateWithWhitelist();

        // the thread which is processing this resource will set its future onto the resource all other
        // threads will return this future and ensure a resource is only processed by a single thread
        synchronized (resource) {
            final Future<Resource> future = resource.getFutureForDownloaded();
            if(future == null) {
                LOG.debug("Download for {} has not been started until now", resource.getSimpleName());
                final Future<Resource> futureResource = getDownloadStateAndStartUnstartedDownload(downloadExecutor);
                resource.startProcessing(futureResource);
                return futureResource;
            } else {
                LOG.debug("Download for {} has already been started.", resource.getSimpleName());
                return future;
            }
        }
    }

    private Future<Resource> getDownloadStateAndStartUnstartedDownload(final Executor downloadExecutor) {
        LOG.debug("Checking download state of {}", resource.getSimpleName());
        final CompletableFuture<Resource> result = new CompletableFuture<>();
        if (resource.isComplete()) {
            LOG.debug("Resource is already downloaded: {} ", resource.getSimpleName());
            result.complete(resource);
        } else if (isNotCacheable()) {
            LOG.debug("Resource is not cacheable: {}", resource.getSimpleName());
            result.complete(initNoneCacheableResources());
        } else {
            LOG.debug("Download has not been started yet: {}", resource.getSimpleName());
            downloadExecutor.execute(() -> {
                try {
                    result.complete(download());
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
        }
        return result;
    }

    private boolean isNotCacheable() {
        return !CacheUtil.isCacheable(resource.getLocation());
    }

    private Resource initNoneCacheableResources() {
        resource.setStatus(DOWNLOADED);
        if (resource.getLocation().getProtocol().equals(FILE_PROTOCOL)) {
            final File file = new File(decodeUrlQuietly(resource.getLocation()).getPath());
            resource.setSize(file.length());
            resource.setLocalFile(file);
            resource.setTransferred(file.length());
        }
        return resource;
    }

    private Resource download() {
        int triesLeft = 2;
        while (true) {
            try {
                return downloadResource();
            } catch (Exception e) {
                if (--triesLeft < 0) {
                    LOG.debug("Exception while downloading '{}'", resource.getSimpleName(), e);
                    resource.setStatus(ERROR);
                    throw e;
                }
            }
        }
    }

    private Resource downloadResource() {
        LOG.debug("Download of resource {} will start now!", resource.getSimpleName());
        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }

    /**  Original method
    private void validateWithWhitelist() {
        final URL url = resource.getLocation();
        Assert.requireNonNull(url, "url");

        // Validate with whitelist specified in deployment.properties. localhost is considered valid.
        final boolean found = UrlWhiteListUtils.isUrlInApplicationUrlWhitelist(url);
        if (!found) {
            BasicExceptionDialog.show(new SecurityException(Translator.R("SWPInvalidURL") + ": " + url));
            LOG.error("Resource URL not In Whitelist: {}", resource.getLocation());
            JNLPRuntime.exit(-1);
        }
    }
    */
    private void validateWithWhitelist() {
        final URL url = resource.getLocation();
        Assert.requireNonNull(url, "url");
        
        // Validate with whitelist specified in deployment.properties. localhost is considered valid.
        //commented out by DJ -final key word so that URL can be checked against whitelist as well as deploymentRuleset.
        /*final*/ boolean found = UrlWhiteListUtils.isUrlInApplicationUrlWhitelist(url);
        //If not found in  the serverWhitelisting , check in DeploymentRuleSet.jar file.
        LOG.debug("Resource URL not In Whitelist: {} found before calling Deployment rule set", found);
        if (!found) {
        	LOG.debug("----------------------BEGIN DEPLOYMENT RULESET CALL------------------------------------------", found);
        	LOG.debug("Resource URL call inside (!found) before calling found=validateWithDeploymentRuleSet()", found);
        	found=validateWithDeploymentRuleSet() ;
        	LOG.debug("Resource URL call inside (!found) after calling found=validateWithDeploymentRuleSet()", found);
        }
        LOG.debug("Resource URL not In Whitelist: {} found after calling Deployment rule set", found);
        if (!found) {
            BasicExceptionDialog.show(new SecurityException(Translator.R("SWPInvalidURL") + ": " + url));
            LOG.error("Resource URL not In Whitelist: {}", resource.getLocation());
            JNLPRuntime.exit(-1);
        }
    }
    
    /**
     * @author DJ 03-02-2021
     * Validates the resource URL with the deploymentRuleSet jar file
     */
    private boolean validateWithDeploymentRuleSet() {
        final URL url = resource.getLocation();
        Assert.requireNonNull(url, "url");

        // Validate with whitelist specified in DeploymentRuleSet.jar localhost is considered valid.
        final boolean found = UrlDeploymentRulesSetUtils.isUrlInDeploymentRuleSetlist(url);
        return found;
    }

}
