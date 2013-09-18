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

package net.sourceforge.jnlp;

import java.io.*;

/**
 * Thrown to indicate that an error has occurred while parsing a
 * JNLP file.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class ParseException extends Exception {

    // todo: add meaningful information, such as the invalid
    // element, parse position, etc.

   
    /**
     * Create a parse exception with the specified message.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Create a parse exception with the specified message and
     * cause.
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }  

}
