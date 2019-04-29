/*   Copyright (C) 2015 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
 exception statement from your version.
 */
package net.adoptopenjdk.icedteaweb.client.controlpanel.desktopintegrationeditor;

import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.XDesktopEntry;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;

public class JListUtils {

    private static Map<File, Icon> iconCache = new HashMap<>();
    private static Map<File, String> textFilesCache = new HashMap<>();
    private static Map<File, Long> stamps = new HashMap<>();

    public static class InfrastructureFileDescriptorListingBasedJListModel extends FileListBasedJListModel {

        private final InfrastructureFileDescriptor source;

        public InfrastructureFileDescriptorListingBasedJListModel(InfrastructureFileDescriptor source, String mask) {
            super(source.getFile(), mask);
            this.source = source;
        }

        public InfrastructureFileDescriptorListingBasedJListModel(InfrastructureFileDescriptor source) {
            super(source.getFile());
            this.source = source;
        }

        public InfrastructureFileDescriptor getSource() {
            return source;
        }

        @Override
        protected File getFile() {
            return source.getFile();
        }

        @Override
        public String toString() {
            return source.toString();
        }

    }

    public static class FileListBasedJListModel implements ListModel {

        private final File directory;
        private File[] list;
        private final Pattern mask;

        /**
         * Construct list containing all files from given directory
         *
         * @param dir
         */
        public FileListBasedJListModel(File dir) {
            //calling constructor with regex matching every file
            this(dir, ".*");
        }

        /**
         * Construct list containing files from given directory matching regex of given mask, 
         *
         * @param dir directory to list
         * @param mask regex to match files to display
         */
        public FileListBasedJListModel(File dir, final String mask) {
            directory = dir;
            this.mask = Pattern.compile(mask);
        }

        protected File getFile() {
            return directory;
        }

        @Override
        public String toString() {
            return getFile().getAbsolutePath();
        }

        private File[] populateList() {
            list = getFile().listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return mask.matcher(name).matches();
                }
            });
            if (list == null) {
                list = new File[0];
            }
            return list;
        }

        @Override
        public int getSize() {
            if (list == null) {
                populateList();
            }
            return list.length;
        }

        @Override
        public Object getElementAt(int index) {
            if (list == null) {
                populateList();
            }
            if (list.length == 0) {
                return "??";
            }
            return list[index];
        }

        @Override
        public void addListDataListener(ListDataListener l) {

        }

        @Override
        public void removeListDataListener(ListDataListener l) {

        }

    }

    public static class CustomRendererJList extends JList<File> {

        public CustomRendererJList() {
            this.setCellRenderer(new FileCellRenderer());
        }

    }

    public static class CustomValidatingRendererJList extends JList<File> {

        public CustomValidatingRendererJList() {
            this.setCellRenderer(new ValidatingFileCellRenderer());
        }

    }

    public static class CustomRendererWithIconJList extends JList<File> {

        public CustomRendererWithIconJList() {
            setCellRenderer(new IconisedCellRenderer());
        }

    }

    private static class FileCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            File f = (File) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setText(f.getName());
            return label;
        }
    }

    private static class ValidatingFileCellRenderer extends FileCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            File f = (File) value;
            String s = processTextFilesCache(f);
            if (!isSelected) {
                if (isJavaws(s)) {
                    l.setBackground(new Color(0, 200, 0));

                } else if (isBrowser(s)) {
                    l.setBackground(new Color(100, 150, 0));
                } else {
                    l.setBackground(new Color(255, 200, 200));
                }
            } else {
                if (isJavaws(s)) {
                    l.setForeground(new Color(0, 200, 0));

                } else if (isBrowser(s)) {
                    l.setForeground(new Color(100, 150, 0));
                } else {
                    l.setForeground(new Color(255, 200, 200));
                }
            }
            return l;
        }

        private boolean isJavaws(String s) {
            return haveString(s, JAVAWS);
        }

        private boolean isBrowser(String s) {
            String[] browsers = XDesktopEntry.BROWSERS;
            for (String browser : browsers) {
                if (haveString(s, browser)) {
                    return true;
                }
            }
            return false;
        }

        private boolean haveString(String s, String i) {
            return s.matches("(?sm).*^.*Exec.*=.*" + i + ".*$.*");
        }
    }

    private static class IconisedCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            File f = (File) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(processIconCache(f));
            label.setText(f.getName());
            label.setHorizontalTextPosition(JLabel.RIGHT);
            return label;
        }

    }

    /**
     * This method looks to cache whether file F was already read as image.
     * If not, file is loaded, put to cache and returned
     * If so, it also check whether it was modified. If so, it is reloaded, replaced in cache and returned.
     * 
     * @param f
     * @return 
     */
    private static Icon processIconCache(File f) {
        Icon i = iconCache.get(f);
        if (i == null) {
            i = updateIconCache(f, i);
        } else {
            if (f.lastModified() != stamps.get(f)) {
                i = updateIconCache(f, i);
            }
        }
        return i;
    }

     /**
     * This method load Icon from file.
     * Once file is loaded, it is stored also to cache
     * Also the time stamp of last modification is stored to cache to allow reloading when changed.
     * 
     * @param f file to load, and to provide timestamp of last modification
     * @return loaded icon or null
     */
    private static Icon updateIconCache(File f, Icon i) {
        i = createImageIcon(f, f.getAbsolutePath());
        if (i != null) {
            iconCache.put(f, i);
            stamps.put(f, f.lastModified());
        }
        return i;
    }

     /**
     * This method looks to cache whether file F was already read as text file.
     * If not, file is loaded, and its content is put to cache and returned as String
     * If so, it also check whether it was modified. If so, it is reloaded, replaced in cache and returned.
     * 
     * @param f
     * @return 
     */
    private static String processTextFilesCache(File f) {
        String s = textFilesCache.get(f);
        if (s == null) {
            s = updateTextCache(f, s);
        } else {
            if (f.lastModified() != stamps.get(f)) {
                s = updateTextCache(f, s);
            }
        }
        return s;
    }

    /**
     * This method load Text from file.
     * Once file is loaded, it is stored also to cache
     * Also the time stamp of last modification is stored to cache to allow reloading when changed.
     * 
     * @param f file to load, and to provide timestamp of last modification
     * @return loaded Text, error message or null
     */
    private static String updateTextCache(File f, String s) {
        s = FreeDesktopIntegrationEditorFrame.fileToString(f, false);
        if (s != null) {
            textFilesCache.put(f, s);
            stamps.put(f, f.lastModified());
        }
        return s;
    }

    /**
     * Load BufferedImage form file, scale it and converts to icon.
     * 
     * @param f file to be loaded
     * @param description description to be provided
     * @return icon from file or null if something went wrong
     */
    private static ImageIcon createImageIcon(File f, String description) {
        try {
            BufferedImage i = ImageIO.read(f);
            return new ImageIcon(i.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        } catch (Exception ex) {
            //not worthy to log it. No image is there and so be it.
            return null;
        }
    }
}
