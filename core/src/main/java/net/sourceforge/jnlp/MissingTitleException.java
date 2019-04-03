// Copyright (C) 2012 Red Hat, Inc.
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

import static net.sourceforge.jnlp.runtime.Translator.R;

/**
 * Thrown when a title that is required from the information tag is not found
 * under the current JVM's locale or as a generalized element.
 */
public class MissingTitleException extends RequiredElementException {

    private static final long serialVersionUID = 1L;

    private static final String message = R("PMissingElement", R("PMissingTitle"));

    /* (non-Javadoc)
     * @see net.sourceforge.jnlp.ParseException(String)
     */
    public MissingTitleException() {
        super(message);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jnlp.ParseException(String, Throwable)
     */
    public MissingTitleException(Throwable cause) {
        super(message, cause);
    }
}
