// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
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

package net.adoptopenjdk.icedteaweb.jnlp.element.extension;

/**
 * A JNLP file is a component extension if the component-desc element is specified. A component extension is
 * typically used to factor out a set of resources that are shared between a large set applications.
 *
 * @implSpec See <b>JSR-56, Section 3.8.1 Component Extension</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class ComponentDesc {
    public static final String COMPONENT_DESC_ELEMENT = "component-desc";

   // TODO: complete implementation/support in JNLPFile, provide reference holder to its resources

    /**
     * Creates an component descriptor element.
     */
    public ComponentDesc() {
    }
}
