package net.adoptopenjdk.icedteaweb.client.parts.downloadindicator;

import javax.jnlp.DownloadServiceListener;
import java.net.URL;

public class DummyDownloadIndicator implements DownloadIndicator {

    @Override
    public DownloadServiceListener getListener(final String downloadName, final URL[] resources) {
        return new DownloadServiceListener() {
            @Override
            public void progress(final URL url, final String s, final long l, final long l1, final int i) {}

            @Override
            public void validating(final URL url, final String s, final long l, final long l1, final int i) {}

            @Override
            public void upgradingArchive(final URL url, final String s, final int i, final int i1) {}

            @Override
            public void downloadFailed(final URL url, final String s) {}
        };
    }

    @Override
    public void disposeListener(final DownloadServiceListener listener) {}
}
