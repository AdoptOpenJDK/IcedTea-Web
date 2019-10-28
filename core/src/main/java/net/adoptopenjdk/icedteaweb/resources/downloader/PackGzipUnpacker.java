// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

/**
 * Unpacker for PACK200 and Gzip streams.
 */
public class PackGzipUnpacker implements StreamUnpacker {
    @Override
    public InputStream unpack(InputStream input) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (final JarOutputStream outputStream = new JarOutputStream(buffer)) {
            Pack200.newUnpacker().unpack(new GZIPInputStream(input), outputStream);
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
