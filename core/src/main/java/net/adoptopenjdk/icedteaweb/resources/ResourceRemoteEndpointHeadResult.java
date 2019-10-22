package net.adoptopenjdk.icedteaweb.resources;

public class ResourceRemoteEndpointHeadResult {

    public static ResourceRemoteEndpointHeadResult fail(final Exception e) {
        return null;
    }

    public boolean isSucessfull() {
        return false;
    }

    public ResourceRemoteEndpoint getEndpoint() {
        return null;
    }
}
