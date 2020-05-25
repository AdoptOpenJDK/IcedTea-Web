/*
Copyright (C) 2008-2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/


package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.AccessWarningPane;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingPermissionsAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.MatchingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.PartiallySignedAppTrustWarningPanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.sourceforge.jnlp.JNLPFile;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RememberableDialog {

    RememberPanelResult getRememberAction();
    DialogResult getValue();
    JNLPFile getFile();
    DialogResult readValue(String s);

    List<Class<? extends RememberableDialog>> ALL_REMEMBERABLE_DIALOG = Collections.unmodifiableList(Stream.of(
            AccessWarningPane.class,
            MatchingALACAttributePanel.class,
            PartiallySignedAppTrustWarningPanel.class,
            UnsignedAppletTrustWarningPanel.class,
            MissingALACAttributePanel.class,
            MissingPermissionsAttributePanel.class
    )
            .sorted(Comparator.comparing(Class::getSimpleName))
            .collect(Collectors.toList()));

}
