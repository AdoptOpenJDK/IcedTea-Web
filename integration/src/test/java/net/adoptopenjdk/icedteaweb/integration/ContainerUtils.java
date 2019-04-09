package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.Assert;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.net.MalformedURLException;
import java.net.URL;

public class ContainerUtils {

    public static final String WWW_FOLDER = "/var/www";
    public static final String NGINX_COMMAND = "nginx";
    public static final int PORT_80 = 80;
    public static final String IMAGE_NAME = "kyma/docker-nginx";
    public static final String URL_PREFIX = "http://localhost:";
    public static final String SLASH = "/";

    public static GenericContainer create(final String localPath) {
        Assert.requireNonBlank(localPath, "localPath");
        return create(MountableFile.forClasspathResource(localPath));
    }

    public static GenericContainer create(final MountableFile localWebFolder) {
        Assert.requireNonNull(localWebFolder, "localWebFolder");
        return new GenericContainer<>(IMAGE_NAME)
                .withCopyFileToContainer(localWebFolder, WWW_FOLDER)
                .withCommand(NGINX_COMMAND)
                .withExposedPorts(PORT_80);
    }

    public static URL getEndpointOnContainer(final GenericContainer container, final String path) {
        Assert.requireNonNull(container, "container");
        try {
            return new URL(URL_PREFIX + container.getFirstMappedPort() + SLASH + path);
        } catch (final MalformedURLException exception) {
            throw new RuntimeException("URL for container endpoint is malformed!", exception);
        }
    }
}
