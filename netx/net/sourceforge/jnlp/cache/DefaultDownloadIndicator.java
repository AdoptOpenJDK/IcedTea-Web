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

package net.sourceforge.jnlp.cache;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.jnlp.*;

import net.sourceforge.jnlp.runtime.*;

/**
 * Show the progress of downloads.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.3 $
 */
public class DefaultDownloadIndicator implements DownloadIndicator {

    // todo: rewrite this to cut down on size/complexity; smarter
    // panels (JList, renderer) understand resources instead of
    // nested panels and grid-bag mess.

    // todo: fix bug where user closes download box and it
    // never(?) reappears.

    // todo: UI for user to cancel/restart download

    // todo: this should be synchronized at some point but conflicts
    // aren't very likely.

    private static String downloading = R("CDownloading");
    private static String complete = R("CComplete");

    /** time to wait after completing but before window closes */
    private static final int CLOSE_DELAY = 750;

    /** the display window */
    private static JFrame frame;

    /** shared constraint */
    static GridBagConstraints vertical;
    static GridBagConstraints verticalIndent;
    static {
        vertical = new GridBagConstraints();
        vertical.gridwidth = GridBagConstraints.REMAINDER;
        vertical.weightx = 1.0;
        vertical.fill = GridBagConstraints.HORIZONTAL;
        vertical.anchor = GridBagConstraints.WEST;

        verticalIndent = (GridBagConstraints) vertical.clone();
        verticalIndent.insets = new Insets(0, 10, 3, 0);
    }

    /**
     * Return the update rate.
     */
    public int getUpdateRate() {
        return 150; //ms
    }

    /**
     * Return the initial delay before obtaining a listener.
     */
    public int getInitialDelay() {
        return 300; //ms
    }

    /**
     * Return a download service listener that displays the progress
     * in a shared download info window.
     *
     * @param app the downloading application, or null if N/A
     * @param downloadName name identifying the download to the user
     * @param resources initial urls to display (not required)
     */
    public DownloadServiceListener getListener(ApplicationInstance app, String downloadName, URL resources[]) {
        DownloadPanel result = new DownloadPanel(downloadName);

        if (frame == null) {
            frame = new JFrame(downloading + "...");
            frame.getContentPane().setLayout(new GridBagLayout());
        }

        if (resources != null)
            for (int i = 0; i < resources.length; i++)
                result.addProgressPanel(resources[i], null);

        frame.getContentPane().add(result, vertical);
        frame.pack();

        if (!frame.isVisible()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
            Dimension screen = new Dimension(screenSize.width - insets.left,
                    screenSize.height - insets.top);
            frame.setLocation(screen.width - frame.getWidth(),
                              screen.height - frame.getHeight());
        }

        frame.setVisible(true);

        return result;
    }

    /**
     * Remove a download service listener that was obtained by
     * calling the getDownloadListener method from the shared
     * download info window.
     */
    public void disposeListener(final DownloadServiceListener listener) {
        if (!(listener instanceof DownloadPanel))
            return;

        ActionListener hider = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (frame.getContentPane().getComponentCount() == 1)
                    frame.setVisible(false);

                frame.getContentPane().remove((DownloadPanel) listener);
                frame.pack();
            }
        };

        Timer timer = new Timer(CLOSE_DELAY, hider);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Groups the url progress in a panel.
     */
    static class DownloadPanel extends JPanel implements DownloadServiceListener {

        /** the download name */
        private String downloadName;

        /** Downloading part: */
        private JLabel header = new JLabel();

        /** list of URLs being downloaded */
        private List<URL> urls = new ArrayList<URL>();

        /** list of ProgressPanels */
        private List<ProgressPanel> panels = new ArrayList<ProgressPanel>();

        /**
         * Create a new download panel for with the specified download
         * name.
         */
        protected DownloadPanel(String downloadName) {
            setLayout(new GridBagLayout());

            this.downloadName = downloadName;
            this.add(header, vertical);
            header.setFont(header.getFont().deriveFont(Font.BOLD));

            setOverallPercent(0);
        }

        /**
         * Add a ProgressPanel for a URL.
         */
        protected void addProgressPanel(URL url, String version) {
            if (!urls.contains(url)) {
                ProgressPanel panel = new ProgressPanel(url, version);

                add(panel, verticalIndent);
                frame.pack();

                urls.add(url);
                panels.add(panel);
            }
        }

        /**
         * Update the download progress of a url.
         */
        protected void update(final URL url, final String version,
                              final long readSoFar, final long total,
                              final int overallPercent) {
            Runnable r = new Runnable() {
                public void run() {
                    if (!urls.contains(url))
                        addProgressPanel(url, version);

                    setOverallPercent(overallPercent);

                    ProgressPanel panel = panels.get(urls.indexOf(url));
                    panel.setProgress(readSoFar, total);
                    panel.repaint();
                }
            };
            SwingUtilities.invokeLater(r);
        }

        /**
         * Sets the overall percent completed.
         */
        public void setOverallPercent(int percent) {
            // don't get whole string from resource and sub in
            // values because it'll be doing a MessageFormat for
            // each update.
            header.setText(downloading + " " + downloadName + ": " + percent + "% " + complete + ".");
        }

        /**
         * Called when a download failed.
         */
        public void downloadFailed(URL url, String version) {
            update(url, version, -1, -1, -1);
        }

        /**
         * Called when a download has progressed.
         */
        public void progress(URL url, String version, long readSoFar, long total, int overallPercent) {
            update(url, version, readSoFar, total, overallPercent);
        }

        /**
         * Called when an archive is patched.
         */
        public void upgradingArchive(URL url, String version, int patchPercent, int overallPercent) {
            update(url, version, patchPercent, 100, overallPercent);
        }

        /**
         * Called when a download is being validated.
         */
        public void validating(URL url, String version, long entry, long total, int overallPercent) {
            update(url, version, entry, total, overallPercent);
        }

    };

    /**
     * A progress bar with the URL next to it.
     */
    static class ProgressPanel extends JPanel {
        private JPanel bar = new JPanel();

        private long total;
        private long readSoFar;

        ProgressPanel(URL url, String version) {
            JLabel location = new JLabel(" " + url.getHost() + "/" + url.getFile());

            bar.setMinimumSize(new Dimension(80, 15));
            bar.setPreferredSize(new Dimension(80, 15));
            bar.setOpaque(false);

            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            add(bar, gbc);

            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.anchor = GridBagConstraints.WEST;
            add(location, gbc);
        }

        public void setProgress(long readSoFar, long total) {
            this.readSoFar = readSoFar;
            this.total = total;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int x = bar.getX();
            int y = bar.getY();
            int h = bar.getHeight();
            int w = bar.getWidth();

            if (readSoFar <= 0 || total <= 0) {
                // make barber pole
            } else {
                double progress = (double) readSoFar / (double) total;
                int divide = (int) (w * progress);

                g.setColor(Color.white);
                g.fillRect(x, y, w, h);
                g.setColor(Color.blue);
                g.fillRect(x + 1, y + 1, divide - 1, h - 1);
            }
        }
    };

}
