/* BasePainter.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012;

import java.awt.BasicStroke;
import net.sourceforge.jnlp.splashscreen.impls.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.splashscreen.SplashUtils.SplashReason;
import net.sourceforge.jnlp.splashscreen.parts.BasicComponentSplashScreen;
import net.sourceforge.jnlp.splashscreen.parts.InfoItem;
import net.sourceforge.jnlp.splashscreen.parts.InformationElement;
import net.sourceforge.jnlp.splashscreen.parts.extensions.ExtensionManager;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.ScreenFinder;

public class BasePainter implements Observer {

    protected final BasicComponentSplashScreen master;
    //animations
    //waterLevel of water (0-100%)
    private int waterLevel = 0;
    //waving of water and position of shhadowed WEB
    private int animationsPosition = 0;
    private int greyTextIncrment = 15; //how quickly is greyed web moving
    //colors
    protected static final Color TEA_LIVE_COLOR = new Color(205, 1, 3);
    protected static final Color BACKGROUND_LIVE_COLOR = ExtensionManager.getExtension().getBackground();
    protected static final Color TEA_LEAFS_STALKS_LIVE_COLOR = Color.black;
    protected static final Color PLUGIN_LIVE_COLOR = ExtensionManager.getExtension().getPluginTextColor();
    public static final Color WATER_LIVE_COLOR = new Color(80, 131, 160);
    protected static final Color PLAIN_TEXT_LIVE_COLOR = ExtensionManager.getExtension().getTextColor();
    protected Color teaColor;
    protected Color backgroundColor;
    protected Color teaLeafsStalksColor;
    protected Color pluginColor;
    protected Color waterColor;
    protected Color plainTextColor;
    //BufferedImage tmpBackround; //testingBackground for fitting
    protected BufferedImage prerenderedStuff;
    private Font teaFont;
    private Font icedFont;
    private Font webFont;
    private Font pluginFont;
    private Font plainTextsFont;
    private Font alternativeTextFont;
    //those spaces are meaningful for centering the text.. thats why alternative;)
    private static final String alternativeICED = "Iced    ";
    private static final String alternativeWeb = "Web  ";
    private static final String alternativeTtea = "    Tea";
    private static final String alternativePlugin = "plugin ";
    private static final String ICED = "Iced";
    private static final String web = "web";
    private static final String tea = "Tea";
    private static final String plugin = "plugin  ";
    //inidivdual sizes, all converging to ZERO!!
    /**
     * Experimentaly meassured best top position for painted parts of vectros
     */
    private final int WEB_TOP_ALIGMENT = 324;
    /**
     * Experimentaly meassured best left position for painted parts of vectors
     */
    private final int WEB_LEFT_ALIGMENT = 84;
    //enabling
    protected boolean showNiceTexts = true;
    private boolean showLeaf = true;
    private boolean showInfo = true;
    protected TextWithWaterLevel twl;
    protected TextWithWaterLevel oldTwl;
    protected boolean canWave = true;
    private Point aboutOfset = new Point();
    
    private final static float dash1[] = {10.0f};
    private final static BasicStroke dashed =
        new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, dash1, 0.0f);

    protected void paintNiceTexts(Graphics2D g2d) {
        //the only animated stuff
        oldTwl = twl;
        twl = new TextWithWaterLevel(ICED, icedFont);
        if (oldTwl != null && !canWave) {
            twl.setCachedPolygon(oldTwl.getCachedPolygon());
        }
        twl.setPercentageOfWater(waterLevel);
        twl.setBgColor(backgroundColor);
        twl.setWaterColor(waterColor);
        twl.cutTo(g2d, scaleX(42), scaleY(278));
        MovingText mt = new MovingText(web, webFont);
        mt.setPercentageOfWater(animationsPosition);
        mt.cutTo(g2d, scaleX(WEB_LEFT_ALIGMENT), scaleY(WEB_TOP_ALIGMENT));
    }

    protected void paintPlainTexts(Graphics2D g2d) {
        g2d.setFont(alternativeTextFont);
        g2d.setColor(waterColor);
        drawTextAroundCenter(g2d, -0.6d, alternativeICED);
        g2d.setColor(teaColor);
        drawTextAroundCenter(g2d, -0.6d, alternativeTtea);
        g2d.setColor(pluginColor);
        String s = getAlternativeProductName();
        int sub = animationsPosition / greyTextIncrment;
        sub = sub % s.length();
        if (!master.isAnimationRunning()) {
            sub = s.length();
        }
        drawTextAroundCenter(g2d, 0.3d, s.substring(0, sub));
    }
    //enabling end

    private int scaleAvarage(double origValue) {
        return (int) (avarageRatio() * origValue);
    }

    private int scaleMax(double origValue) {
        return (int) (maxRatio() * origValue);
    }

    private int scaleMin(double origValue) {
        return (int) (minRatio() * origValue);
    }

    private double avarageRatio() {
        return (getRatioX() + getRatioY()) / 2d;
    }

    private double minRatio() {
        return Math.min(getRatioX(), getRatioY());
    }

    private double maxRatio() {
        return Math.max(getRatioX(), getRatioY());
    }

    private int scaleY(double origValue) {
        return (int) scaleY(master.getSplashHeight(), origValue);
    }

    private int scaleX(double origValue) {
        return (int) scaleX(master.getSplashWidth(), origValue);
    }

    private double getRatioY() {
        return getRatio(DefaultSplashScreen2012.ORIGINAL_H, master.getSplashHeight());
    }

    private double getRatioX() {
        return getRatio(DefaultSplashScreen2012.ORIGINAL_W, master.getSplashWidth());
    }

    private static double scaleY(double currentSize, double origValue) {
        return scale(DefaultSplashScreen2012.ORIGINAL_H, currentSize, origValue);
    }

    private static double scaleX(double currentSize, double origValue) {
        return scale(DefaultSplashScreen2012.ORIGINAL_W, currentSize, origValue);
    }

    private static double getRatioY(double currentSize) {
        return getRatio(DefaultSplashScreen2012.ORIGINAL_H, currentSize);
    }

    private static double getRatioX(double currentSize) {
        return getRatio(DefaultSplashScreen2012.ORIGINAL_W, currentSize);
    }

    public static double scale(double origSize, double currentSize, double origValue) {
        return getRatio(origSize, currentSize) * origValue;
    }

    public static double getRatio(double origSize, double currentSize) {
        return (currentSize / origSize);
    }
    //size is considered from 0-origsize as 0-1.

    //scaling end
    public BasePainter(BasicComponentSplashScreen master) {
        this(master, false);
    }

    public BasePainter(BasicComponentSplashScreen master, boolean startAnimation) {
        //to have this in inner classes
        this.master = master;
        setColors();
        adjustForSize(master.getSplashWidth(), master.getSplashHeight());
        ExtensionManager.getExtension().adjustForSize(master.getSplashWidth(), master.getSplashHeight());
        if (startAnimation) {
            startAnimationThreads();
        }

    }

    public void increaseAnimationPosition() {
        ExtensionManager.getExtension().animate();
        animationsPosition += greyTextIncrment;
    }

    protected void ensurePrerenderedStuff() {
        if (this.prerenderedStuff == null) {
            this.prerenderedStuff = prerenderStill();
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        ensurePrerenderedStuff();
        if (prerenderedStuff != null) {
            g2d.drawImage(prerenderedStuff, 0, 0, null);
        }

        if (showNiceTexts) {
            ExtensionManager.getExtension().paint(g, this);
            paintNiceTexts(g2d);
        } else {
            paintPlainTexts(g2d);
        }



    }

    public final void adjustForSize(int width, int height) {
        prepareFonts(width, height);
        //enablings depends on fonts
        setEnablings(width, height, master.getVersion(), master.getInformationElement(), (Graphics2D) (master.getGraphics()));
        prerenderedStuff = prerenderStill();
        ExtensionManager.getExtension().adjustForSize(width, height);
    }

    private void setEnablings(int w, int h, String version, InformationElement ic, Graphics2D g2d) {
        showLeaf = true;
        if (w > 0 && h > 0) {
            //leaf stretch much better to wide then to high
            if (h / w > 2 || w / h > 6) {
                showLeaf = false;
            }
        }
        showInfo = true;
        if (version != null && g2d != null && ic != null && ic.getHeader() != null && ic.getHeader().size() > 0) {
            String s = ic.getHeader().get(0);
            FontMetrics fm = g2d.getFontMetrics(plainTextsFont);
            int versionLength = fm.stringWidth(version);
            int firsDescLineLengthg = fm.stringWidth(s);
            if (firsDescLineLengthg > w - versionLength - 10) {
                showInfo = false;
            }
        }
        if (Math.min(h, w) < ScreenFinder.getCurrentScreenSizeWithoutBounds().getHeight() / 10) {
            showNiceTexts = false;
        } else {
            showNiceTexts = true;
        }
    }

    public final void startAnimationThreads() {
        Thread tt = getMovingTextThread();
        tt.start();
        Thread t = getWaterLevelThread();
        t.start();
    }

    private void prepareFonts(int w, int h) {
        master.setSplashHeight(h);
        master.setSplashWidth(w);
        Map<TextAttribute, Object> teaFontAttributes = new HashMap<TextAttribute, Object>();
        teaFontAttributes.put(TextAttribute.SIZE, new Integer(scaleMin(84)));
        teaFontAttributes.put(TextAttribute.WIDTH, new Double((0.95)));
        teaFontAttributes.put(TextAttribute.FAMILY, "Serif");
        teaFont = new Font(teaFontAttributes);
        Map<TextAttribute, Object> icedFontAttributes = new HashMap<TextAttribute, Object>();
        icedFontAttributes.put(TextAttribute.SIZE, new Integer(scaleMin(82)));
        icedFontAttributes.put(TextAttribute.WIDTH, new Double((0.80)));
        icedFontAttributes.put(TextAttribute.FAMILY, "Serif");
        icedFont = new Font(icedFontAttributes);
        Map<TextAttribute, Object> webFontAttributes = new HashMap<TextAttribute, Object>();
        webFontAttributes.put(TextAttribute.SIZE, new Integer(scaleMin(41)));
        webFontAttributes.put(TextAttribute.WIDTH, new Double((2)));
        webFontAttributes.put(TextAttribute.FAMILY, "Serif");
        webFont = new Font(webFontAttributes);
        Map<TextAttribute, Object> pluginFontAttributes = new HashMap<TextAttribute, Object>();
        pluginFontAttributes.put(TextAttribute.SIZE, new Integer(scaleMin(32)));
        pluginFontAttributes.put(TextAttribute.WEIGHT, new Double((5d)));
        pluginFontAttributes.put(TextAttribute.WIDTH, new Double((0.9)));
        pluginFontAttributes.put(TextAttribute.FAMILY, "Serif");
        pluginFont = new Font(pluginFontAttributes);
        Map<TextAttribute, Object> plainFontAttributes = new HashMap<TextAttribute, Object>();
        plainFontAttributes.put(TextAttribute.SIZE, new Integer(12));
        plainFontAttributes.put(TextAttribute.FAMILY, "Monospaced");
        plainTextsFont = new Font(plainFontAttributes);
        Map<TextAttribute, Object> alternativeTextFontAttributes = new HashMap<TextAttribute, Object>();
        alternativeTextFontAttributes.put(TextAttribute.SIZE, Math.min(w, h) / 5);
        alternativeTextFontAttributes.put(TextAttribute.WIDTH, new Double((0.7)));
        alternativeTextFontAttributes.put(TextAttribute.FAMILY, "Monospaced");
        alternativeTextFont = new Font(alternativeTextFontAttributes);

    }

    private void setColors() {

        teaColor = TEA_LIVE_COLOR;
        backgroundColor = BACKGROUND_LIVE_COLOR;
        teaLeafsStalksColor = TEA_LEAFS_STALKS_LIVE_COLOR;
        pluginColor = PLUGIN_LIVE_COLOR;
        waterColor = WATER_LIVE_COLOR;
        plainTextColor = PLAIN_TEXT_LIVE_COLOR;

    }

    protected BufferedImage prerenderStill() {
        if (master.getSplashWidth() <= 0 || master.getSplashHeight() <= 0) {
            return null;
        }
        BufferedImage bi = new BufferedImage(master.getSplashWidth(), master.getSplashHeight(), BufferedImage.TYPE_INT_ARGB);
        paintStillTo(bi.createGraphics(), master.getInformationElement(), master.getVersion());
        return bi;
    }

    protected void paintStillTo(Graphics2D g2d, InformationElement ic, String version) {
        RenderingHints r = g2d.getRenderingHints();
        drawBase(g2d, ic, version);
        g2d.setRenderingHints(r);
    }

    protected void drawTextAroundCenter(Graphics2D g2d, double heightOffset, String msg) {

        int y = (master.getSplashHeight() / 2) + (g2d.getFontMetrics().getHeight() / 2 + (int) (heightOffset * g2d.getFontMetrics().getHeight()));
        int x = (master.getSplashWidth() / 2) - (g2d.getFontMetrics().stringWidth(msg) / 2);
        g2d.drawString(msg, x, y);
    }

    private Thread getMovingTextThread() {
        Thread tt = new Thread(new MovingTextRunner(this));
        //tt.setDaemon(true);
        return tt;
    }

    static String stripCommitFromVersion(String version) {
        if (version.contains("pre+")) {
            return version;
        }
        int i = version.indexOf("+");
        if (i < 0) {
            return version;
        }
        return version.substring(0, version.indexOf("+"));
    }

    private final class MovingTextRunner extends Observable implements Runnable {

        private static final int MAX_ANIMATION_VALUE = 10000;
        private static final int ANIMATION_RESTART_VALUE = 1;
        private static final long MOOVING_TEXT_DELAY = 150;

        public MovingTextRunner(Observer o) {
            this.addObserver(o);
        }

        @Override
        public void run() {
            while (master.isAnimationRunning()) {
                try {
                    animationsPosition += greyTextIncrment;
                    if (animationsPosition > MAX_ANIMATION_VALUE) {
                        animationsPosition = ANIMATION_RESTART_VALUE;
                    }
                    this.setChanged();
                    this.notifyObservers();
                    Thread.sleep(MOOVING_TEXT_DELAY);
                } catch (Exception e) {
                    OutputController.getLogger().log(e);
                }
            }
        }
    };

    private Thread getWaterLevelThread() {
        Thread t = new Thread(new WaterLevelThread(this));
        //t.setDaemon(true);
        return t;
    }

    private final class WaterLevelThread extends Observable implements Runnable {

        private static final int MAX_WATERLEVEL_VALUE = 120;
        private static final int WATER_LEVEL_INCREMENT = 2;

        private WaterLevelThread(BasePainter o) {
            this.addObserver(o);
        }

        @Override
        public void run() {
            while (master.isAnimationRunning()) {
                if (waterLevel > MAX_WATERLEVEL_VALUE) {
                    break;
                }
                try {
                    waterLevel += WATER_LEVEL_INCREMENT;
                    this.setChanged();
                    this.notifyObservers();
                    //it is risinfg slower and slower
                    Thread.sleep((waterLevel / 4) * 30);
                } catch (Exception e) {
                    OutputController.getLogger().log(e);
                }
            }
        }
    };

    private String getAlternativeProductName() {
        if (SplashReason.JAVAWS.equals(master.getSplashReason())) {
            return alternativeWeb;
        } else if (SplashReason.APPLET.equals(master.getSplashReason())) {
            return alternativeWeb + alternativePlugin;
        } else {
            return "....";
        }
    }

    protected FontMetrics drawBase(Graphics2D g2d, InformationElement ic, String version) {
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, master.getSplashWidth() + 5, master.getSplashHeight() + 5);
        if (showNiceTexts) {
            //g2d.drawImage(tmpBackround, 0, 0, null);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(teaFont);
            g2d.setColor(teaColor);
            g2d.drawString(tea, scaleX(42) + g2d.getFontMetrics(icedFont).stringWidth(ICED), scaleY(278));
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            if (showLeaf) {
                g2d.fillPolygon(SplinesDefs.getMainLeafCurve(getRatioX(), getRatioY()));
            }
            if (showLeaf) {
                g2d.fillPolygon(SplinesDefs.getSecondLeafCurve(getRatioX(), getRatioY()));
            }
            g2d.setColor(teaLeafsStalksColor);
            if (showLeaf) {
                g2d.fillPolygon(SplinesDefs.getMainLeafStalkCurve(getRatioX(), getRatioY()));
            }
            if (showLeaf) {
                g2d.fillPolygon(SplinesDefs.getSecondLeafStalkCurve(getRatioX(), getRatioY()));
            }
            g2d.setFont(pluginFont);
            g2d.setColor(pluginColor);
            if (SplashReason.APPLET.equals(master.getSplashReason())) {
                if (showLeaf) {
                    g2d.drawString(plugin, scaleX(404), scaleY(145));
                } else {
                    FontMetrics wfm = g2d.getFontMetrics(webFont);
                    g2d.drawString(plugin, wfm.stringWidth(web) + scaleX(WEB_LEFT_ALIGMENT) + 10, scaleY(WEB_TOP_ALIGMENT));
                }
            }
            g2d.setFont(plainTextsFont);
            g2d.setColor(plainTextColor);
            FontMetrics fm = g2d.getFontMetrics();
            if (ic != null) {
                InfoItem des = ic.getBestMatchingDescriptionForSplash();
                List<String> head = ic.getHeader();
                if (head != null && showInfo) {
                    for (int i = 0; i < head.size(); i++) {
                        String string = head.get(i);
                        g2d.drawString(string, 5, (i + 1) * fm.getHeight());
                    }
                }
                if (des != null && des.getValue() != null) {
                    g2d.drawString(des.getValue(), 5, master.getSplashHeight() - fm.getHeight());
                }
            }
        }
        g2d.setFont(plainTextsFont);
        g2d.setColor(plainTextColor);
        FontMetrics fm = g2d.getFontMetrics();
        if (version != null) {
            String aboutPrefix = Translator.R("AboutDialogueTabAbout") + ": ";
            int aboutPrefixWidth = fm.stringWidth(aboutPrefix);
            String niceVersion = stripCommitFromVersion(version);
            int y = master.getSplashWidth() - fm.stringWidth(niceVersion + " ");
            if (y < 0) {
                y = 0;
            }
            if (y > aboutPrefixWidth) {
                niceVersion = aboutPrefix + niceVersion;
                y -= aboutPrefixWidth;
            }
            aboutOfset = new Point(y, fm.getHeight());
            Stroke backup = g2d.getStroke();
            g2d.setStroke(dashed);
            g2d.drawRect(aboutOfset.x-1,1, master.getSplashWidth()-aboutOfset.x-1, aboutOfset.y+1);
            g2d.setStroke(backup);
            g2d.drawString(niceVersion, y, fm.getHeight());
        }
        return fm;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int level) {
        this.waterLevel = level;
    }

    public int getAnimationsPosition() {
        return animationsPosition;
    }

    public void clearCachedWaterTextImage() {
        oldTwl = null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    ExtensionManager.getExtension().animate();
                    master.repaint();
                }
            });
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }
    }

    public BasicComponentSplashScreen getMaster() {
        return master;
    }

    public Point getAboutOfset() {
        return aboutOfset;
    }

    public Color getWaterColor() {
        return waterColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

}
