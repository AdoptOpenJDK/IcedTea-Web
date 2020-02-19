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

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.Remember;
import net.sourceforge.jnlp.JNLPFile;

import java.util.Optional;

public interface UserDecisions {
    <T extends Enum<T>> Optional<T> getUserDecisions(UserDecision.Key key, JNLPFile file, Class<T> resultType);

    <T extends Enum<T>> void saveForDomain(JNLPFile file, UserDecision<T> userDecision);

    <T extends Enum<T>> void saveForApplication(JNLPFile file, UserDecision<T> userDecision);

    default <T extends Enum<T>> void save(Remember result, JNLPFile file, UserDecision<T> userDecision) {
        if (result == Remember.REMEMBER_BY_DOMAIN) {
            saveForDomain(file, userDecision);
        }
        else if (result == Remember.REMEMBER_BY_APPLICATION) {
            saveForApplication(file, userDecision);
        }
    }
}
