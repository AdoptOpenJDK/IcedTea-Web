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

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class IcoReaderSpi extends ImageReaderSpi {


    private static final String readerClassName = Icons.class.getName();
    private static final String[] localNames = {IconConstants.ICO};
    private static final String[] localSuffixes = {IconConstants.ICO, IconConstants.ICO_CAMELCASE, IconConstants.ICO_UPPERCASE};
    private static final String[] localMIMETypes = {IconConstants.IMAGE_VND_MICROSOFT_ICON, IconConstants.IMAGE_X_ICON};

    public IcoReaderSpi() {
        super("icedtea-web",
                "1.0",
                localNames,
                localSuffixes,
                localMIMETypes,
                readerClassName,
                new Class[]{ImageInputStream.class, InputStream.class, File.class, URL.class, String.class},
                new String[0],
                false,
                null,
                null,
                new String[0],
                new String[0],
                false,
                null,
                null,
                new String[0],
                new String[0]);
    }

    @Override
    public String getDescription(final Locale locale) {
        return "icedtea-web ico decoder provider";
    }

    @Override
    public boolean canDecodeInput(final Object input) {
        try {
            if (input instanceof ImageInputStream) {
                final ImageInputStream in = (ImageInputStream) input;
                in.mark();
                try {
                    new IcoHeader(in);
                } finally {
                    in.reset();
                }
                return true;
            }
            return false;
        } catch (final Exception ex) {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(final Object extension) {
        return new IcoReader(this);
    }

}
