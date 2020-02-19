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
package net.adoptopenjdk.icedteaweb.userdecision;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.PathsAndFiles;

import java.io.File;
import java.util.Optional;

public class UserDecisionsFileStore implements UserDecisions {
    private final static File store = PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile();

    @Override
    public <T extends Enum<T>> Optional<T> getUserDecisions(final UserDecision.Key key, final JNLPFile file, final Class<T> resultType) {
        // TODO implementation missing
        // lock file
        return Optional.empty();
    }

    @Override
    public <T extends Enum<T>> void saveForDomain(final JNLPFile file, final UserDecision<T> userDecision) {
        // TODO implementation missing
        // lock file, clean domain, see old impl for UrlUtils/UrlRegEx
    }

    @Override
    public <T extends Enum<T>> void saveForApplication(final JNLPFile file, final UserDecision<T> userDecision) {
        // TODO implementation missing
        // lock file, clean domain, see old impl for UrlUtils/UrlRegEx
    }
}
