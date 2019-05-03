package jnlp.sample.servlet;

import org.w3c.dom.Document;

/**
 * Hook for JNLP file request
 */
public interface JnlpFileHandlerHook {

    /**
     * Identity implementation
     */
    public static final JnlpFileHandlerHook IDENTITY = new JnlpFileHandlerHook() {
	@Override
	public void preCommit(DownloadRequest dreq, Document document) {
	}
    };

    /**
     * Invoked before the HTTP response of a JNLP file is committed.
     * 
     * @param dreq
     *            original request
     * @param document
     *            JNLP document to be committed
     */
    void preCommit(DownloadRequest dreq, Document document);
}
