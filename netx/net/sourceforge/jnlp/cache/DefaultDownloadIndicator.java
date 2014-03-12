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
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.ScreenFinder;

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
    private static final Object frameMutex = new Object();

    /** shared constraint */
    static GridBagConstraints vertical;
    static GridBagConstraints verticalNoClean;
    static GridBagConstraints verticalIndent;
    static {
        vertical = new GridBagConstraints();
        vertical.gridwidth = GridBagConstraints.REMAINDER;
        vertical.weightx = 1.0;
        vertical.fill = GridBagConstraints.HORIZONTAL;
        vertical.anchor = GridBagConstraints.WEST;

        verticalNoClean = new GridBagConstraints();
        verticalNoClean.weightx = 1.0;

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

        synchronized (frameMutex) {
            if (frame == null) {
                frame=createDownloadIndicatorFrame(true);
            }

            if (resources != null) {
                for (URL url : resources) {
                    result.addProgressPanel(url, null);
        
                }
            }

            frame.getContentPane().add(result, vertical);
            frame.pack();
            placeFrameToLowerRight();
            result.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    placeFrameToLowerRight();
                }
            });

            frame.setVisible(true);

            return result;
        }
    }

     public static JFrame createDownloadIndicatorFrame(boolean undecorated) throws HeadlessException {
        JFrame f = new JFrame(downloading + "...");
        f.setUndecorated(undecorated);
        f.setIconImages(ImageResources.INSTANCE.getApplicationImages());
        f.getContentPane().setLayout(new GridBagLayout());
        return f;
    }

    /**
     * This places indicator to lower right corner of active monitor.
     */
    private static void placeFrameToLowerRight() throws HeadlessException {
       Rectangle bounds = ScreenFinder.getCurrentScreenSizeWithoutBounds();
        frame.setLocation(bounds.width+bounds.x - frame.getWidth(),
                bounds.height+bounds.y - frame.getHeight());
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
                synchronized(frameMutex) {
                    frame.getContentPane().remove((DownloadPanel) listener);
                    frame.pack();

                    if (frame.getContentPane().getComponentCount() == 0) {
                        frame.setVisible(false);
                        frame.dispose();
                        frame = null;
                    }
                }
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
        private final DownloadPanel self;

        private static enum States{
            ONE_JAR, COLLAPSED, DETAILED;
         }
        
        private static final String DETAILS=R("ButShowDetails");
        private static final String HIDE_DETAILS=R("ButHideDetails");   
        /** the download name */
        private String downloadName;
        /** Downloading part: */
        private JLabel header = new JLabel();
        /** Show/hide detailsButton button: */
        private final JButton detailsButton;
        private static final URL magnifyGlassUrl = ClassLoader.getSystemResource("net/sourceforge/jnlp/resources/showDownloadDetails.png");
        private static final URL redCrossUrl = ClassLoader.getSystemResource("net/sourceforge/jnlp/resources/hideDownloadDetails.png");
        private static final Icon magnifyGlassIcon = new ImageIcon(magnifyGlassUrl);
        private static final Icon redCrossIcon = new ImageIcon(redCrossUrl);
        /** used  instead of detailsButton button in case of one jar*/
        private JLabel delimiter = new JLabel("");  
        /** all already created progress bars*/
        private List<ProgressPanel> progressPanels = new ArrayList<ProgressPanel>();
        private States state=States.ONE_JAR;
        private ProgressPanel mainProgressPanel;
        
        /** list of URLs being downloaded */
        private List<URL> urls = new ArrayList<URL>();

        /** list of ProgressPanels */
        private List<ProgressPanel> panels = new ArrayList<ProgressPanel>();

        /**
         * Create a new download panel for with the specified download
         * name.
         */
        protected DownloadPanel(String downloadName) {
            self = this;
            setLayout(new GridBagLayout());
            this.downloadName = downloadName;
            this.add(header, verticalNoClean);
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            this.add(delimiter, vertical);
            detailsButton = new JButton(magnifyGlassIcon);
            int w = magnifyGlassIcon.getIconWidth();
            int h = magnifyGlassIcon.getIconHeight();
            detailsButton.setPreferredSize(new Dimension(w + 2, h + 2));
            detailsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (state == States.DETAILED) {
                        state = States.COLLAPSED;
                        detailsButton.setToolTipText(DETAILS);
                        detailsButton.setIcon(magnifyGlassIcon);
                        for (ProgressPanel progressPanel : progressPanels) {
                            remove(progressPanel);
                        }
                        add(mainProgressPanel, verticalIndent);
                        recreateFrame(true);
                    } else {
                        state = States.DETAILED;
                        detailsButton.setToolTipText(HIDE_DETAILS);
                        detailsButton.setIcon(redCrossIcon);
                        remove(mainProgressPanel);
                        for (ProgressPanel progressPanel : progressPanels) {
                            add(progressPanel, verticalIndent);
                        }
                        recreateFrame(false);
                    }
                }

                public void recreateFrame(boolean undecorated) throws HeadlessException {
                    JFrame oldFrame = frame;
                    frame = createDownloadIndicatorFrame(undecorated);
                    frame.getContentPane().add(self, vertical);
                    synchronized (frameMutex) {
                        frame.pack();
                        placeFrameToLowerRight();
                    }
                    frame.setVisible(true);
                    oldFrame.dispose();
                }
            });
            setOverallPercent(0);
        }

        /**
         * Add a ProgressPanel for a URL.
         */
        protected void addProgressPanel(URL url, String version) {
            if (!urls.contains(url)) {
                ProgressPanel panel = new ProgressPanel(url, version);
                if (state != States.COLLAPSED) {
                    add(panel, verticalIndent);
                }
                progressPanels.add(panel);
                urls.add(url);
                panels.add(panel); 
                //download indicator does not know about added jars
                //When only one jar is added to downlaod queue then its progress is 
                //shown, and there is no show detail button.
                //When second one is added, then it already knows that there will
                //be two or more jars, so it swap to collapsed state in count of two.
                //no later, no sooner
                if (panels.size() == 2){
                    remove(panels.get(0));
                    remove(panels.get(1));
                    remove(delimiter);
                    add(detailsButton,vertical);
                    mainProgressPanel=new ProgressPanel();
                    add(mainProgressPanel, verticalIndent);
                    state=States.COLLAPSED;
                }
                synchronized (frameMutex) {
                    frame.pack();
                    placeFrameToLowerRight();
                }

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
         * should be called via invokeLater
         */
        public void setOverallPercent(int percent) {
            // don't get whole string from resource and sub in
            // values because it'll be doing a MessageFormat for
            // each update.
            header.setText(downloading + " " + downloadName + ": " + percent + "% " + complete + ".");
            Container c = header.getParent();
            //we need to adapt both panels and also frame to new length of header text
            while (c != null) {
                c.invalidate();
                c.validate();
                if (c instanceof  Window){
                    ((Window) c).pack();
                }
                c=c.getParent();
            }
            if (mainProgressPanel != null) {
                mainProgressPanel.setProgress(percent, 100);
                mainProgressPanel.repaint();
            }
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
        private Dimension size = new Dimension(80, 15);

        ProgressPanel() {
            bar.setMinimumSize(size);
            bar.setPreferredSize(size);
            bar.setOpaque(false);

            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            styleGridBagConstraints(gbc);
            add(bar, gbc);
        }
        
        ProgressPanel(URL url, String version) {
            this(" " + url.getHost() + "/" + url.getFile(),version);
        }
        ProgressPanel(String caption, String version) {
            JLabel location = new JLabel(caption);

            bar.setMinimumSize(size);
            bar.setPreferredSize(size);
            bar.setOpaque(false);

            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            add(bar, gbc);
            
            styleGridBagConstraints(gbc);
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

        private void styleGridBagConstraints(GridBagConstraints gbc) {
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.anchor = GridBagConstraints.WEST;
        }
    };

}
