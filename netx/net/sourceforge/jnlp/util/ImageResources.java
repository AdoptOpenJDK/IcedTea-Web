/* ImageResources.java
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

package net.sourceforge.jnlp.util;

import net.sourceforge.jnlp.util.logging.OutputController;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public enum ImageResources {

    INSTANCE;

    private static final String APPLICATION_ICON_PATH = "net/sourceforge/jnlp/resources/netx-icon.png";

    private final Map<String, Image> cache = new HashMap<String, Image>();

    private ImageResources() {}

    /* this is for testing ONLY */
    void clearCache() {
        cache.clear();
    }

    /**
     * Returns an appropriate image, or null if there are errors loading the image.
     */
    private Image getApplicationImage() {
        if (cache.containsKey(APPLICATION_ICON_PATH)) {
            return cache.get(APPLICATION_ICON_PATH);
        }

        ClassLoader cl = this.getClass().getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        InputStream in = cl.getResourceAsStream(APPLICATION_ICON_PATH);
        try {
            Image image = ImageIO.read(in);
            cache.put(APPLICATION_ICON_PATH, image);
            return image;
        } catch (IOException ioe) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ioe);
            return null;
        }
    }

    /**
     * Returns an appropriate image, or null if there are errors loading the image.
     */
    public List<Image> getApplicationImages() {
        List<Image> images = new ArrayList<Image>();
        Image appImage = getApplicationImage();
        if (appImage != null) {
            images.add(appImage);
        }
        return images;
    }

}
