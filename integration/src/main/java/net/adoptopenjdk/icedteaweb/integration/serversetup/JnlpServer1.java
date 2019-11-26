package net.adoptopenjdk.icedteaweb.integration.serversetup;

import java.io.IOException;

/**
 * ...
 */
public interface JnlpServer1 {
    JnlpServer2 servingJnlp(String jnlpFileName) throws IOException;
}
