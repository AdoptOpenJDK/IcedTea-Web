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
package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.ITW_BIN_LOCATION;

/**
 * Implementation of {@link JvmLauncher} which uses the IcedTea-Web launcher to start a new JVM.
 */
public class ItwJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(ItwJvmLauncher.class);

    @Override
    public void launchExternal(final JNLPFile jnlpFile, final List<String> args) throws Exception {
        requireNonNull(jnlpFile, "JNLPFile must not be null.");
        requireNonNull(args, "args must not be null.");

        launchExternal(jnlpFile.getNewVMArgs(), args);
    }

    /**
     * @param vmArgs     the arguments to pass to the jvm
     * @param javawsArgs the arguments to pass to javaws (aka IcedTea-Web)
     */
    private void launchExternal(final List<String> vmArgs, final List<String> javawsArgs) throws Exception {
        final List<String> commands = new LinkedList<>();

        // this property is set by the javaws launcher to point to the javaws binary
        final String pathToItwBinary = System.getProperty(ITW_BIN_LOCATION);
        commands.add(pathToItwBinary);

        // use -Jargument format to pass arguments to the JVM through the launcher
        for (String arg : vmArgs) {
            commands.add("-J" + arg);
        }

        commands.addAll(javawsArgs);

        LOG.info("About to launch external with commands: '{}'", commands.toString());

        final Process p = new ProcessBuilder()
                .command(commands)
                .inheritIO()
                .start();

        ProcessUtils.waitForSafely(p);
    }
}
