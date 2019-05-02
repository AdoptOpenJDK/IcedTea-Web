/*
 Copyright (C) 2015 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.icon;

import net.adoptopenjdk.icedteaweb.icon.impl.ImageInputStreamIco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class IcoReader extends ImageReader {

    private static final Logger LOG = LoggerFactory.getLogger(IcoReader.class);

    private ImageInputStreamIco cheat;

    IcoReader(final ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    private void loadIcon() {
        if (cheat == null) {
            try {
                final Object input = getInput();
                if (input instanceof ImageInputStream) {
                    final ImageInputStream iis = (ImageInputStream) input;
                    cheat = new ImageInputStreamIco(iis);

                }

            } catch (final Exception ex) {
                LOG.error("Error in loading icon", ex);
            }
        }
    }

    @Override
    public int getNumImages(final boolean allowSearch) {
        loadIcon();
        return cheat.getHeader().getCountOfIcons();
    }

    private void checkIndex(final int imageIndex) {
        loadIcon();
        if (imageIndex < 0 || imageIndex >= cheat.getHeader().getCountOfIcons()) {
            throw new IndexOutOfBoundsException("bad index " + imageIndex + ". Should be >=0 and < " + cheat.getHeader().getCountOfIcons());
        }
    }

    @Override
    public int getWidth(final int imageIndex) {
        checkIndex(imageIndex);
        return cheat.getHeader().getEntries().get(imageIndex).getWidth();
    }

    @Override
    public int getHeight(final int imageIndex) {
        checkIndex(imageIndex);
        return cheat.getHeader().getEntries().get(imageIndex).getHeight();

    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) {
        checkIndex(imageIndex);
        final List<ImageTypeSpecifier> l = new ArrayList<>();
        final Vector<RenderedImage> q = cheat.getImage(imageIndex).getSources();
        for (final RenderedImage q1 : q) {
            l.add(new ImageTypeSpecifier(q1));
        }

        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(final int imageIndex) {
        checkIndex(imageIndex);
        return null;
    }

    @Override
    public BufferedImage read(final int imageIndex, final ImageReadParam param) {
        checkIndex(imageIndex);
        return cheat.getImage(imageIndex);
    }
}
