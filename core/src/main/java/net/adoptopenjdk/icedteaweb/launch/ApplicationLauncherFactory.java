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
//
package net.adoptopenjdk.icedteaweb.launch;

import net.sourceforge.jnlp.ParserSettings;

import java.util.List;
import java.util.Map;

public interface ApplicationLauncherFactory {
    /**
     * Creates an {@link ApplicationLauncher}.
     *
     * @param parserSettings the parser settings to use when the Launcher initiates parsing of a JNLP file
     * @param extraInformation map containing extra information to add to the main JNLP. The values for
     *              keys "arguments", "parameters", and "properties" are used.
     * @return
     */
    ApplicationLauncher createLauncher(ParserSettings parserSettings, Map<String, List<String>> extraInformation);
}
