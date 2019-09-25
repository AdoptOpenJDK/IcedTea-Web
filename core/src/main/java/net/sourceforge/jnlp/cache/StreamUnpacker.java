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

package net.sourceforge.jnlp.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 * Allows to unpack an input stream.
 */
interface StreamUnpacker {

    /**
     * Unpacks the content of the input stream.
     * Provides a new input stream with the unpacked content.
     *
     * @param input a compressed input stream
     * @return an unpacked input stream
     * @throws IOException if anything goes wrong
     */
    InputStream unpack(InputStream input) throws IOException;
}
