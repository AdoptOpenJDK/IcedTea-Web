package net.sourceforge.jnlp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JDialog;

import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ImageResources;

public class JNLPSplashScreen extends JDialog {

    String applicationTitle;
    String applicationVendor;

    ResourceTracker resourceTracker;

    URL splashImageUrl;
    Image splashImage;

    public JNLPSplashScreen(ResourceTracker resourceTracker,
            String applicationTitle, String applicationVendor) {

        setIconImages(ImageResources.INSTANCE.getApplicationImages());

        // If the JNLP file does not contain any icon images, the splash image
        // will consist of the application's title and vendor, as taken from the
        // JNLP file.

        this.resourceTracker = resourceTracker;
        this.applicationTitle = applicationTitle;
        this.applicationVendor = applicationVendor;

    }

    public void setSplashImageURL(URL url) {
        splashImageUrl = url;
        splashImage = null;
        try {
            splashImage = ImageIO.read(resourceTracker
                    .getCacheFile(splashImageUrl));
            if (splashImage == null) {
                if (JNLPRuntime.isDebug()) {
                    System.err.println("Error loading splash image: " + url);
                }
                return;
            }
        } catch (IOException e) {
            if (JNLPRuntime.isDebug()) {
                System.err.println("Error loading splash image: " + url);
            }
            splashImage = null;
            return;
        } catch (IllegalArgumentException argumentException) {
            if (JNLPRuntime.isDebug()) {
                System.err.println("Error loading splash image: " + url);
            }
            splashImage = null;
            return;
        }

        correctSize();
    }

    public boolean isSplashScreenValid() {
        return (splashImage != null);
    }

    private void correctSize() {

        Insets insets = getInsets();
        int minimumWidth = splashImage.getWidth(null) + insets.left
                + insets.right;
        int minimumHeight = splashImage.getHeight(null) + insets.top
                + insets.bottom;
        setMinimumSize(new Dimension(minimumWidth, minimumHeight));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - minimumWidth) / 2,
                (screenSize.height - minimumHeight) / 2);
    }

    @Override
    public void paint(Graphics g) {
        if (splashImage == null) {
            return;
        }

        correctSize();
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(splashImage, getInsets().left, getInsets().top, null);

    }
}
