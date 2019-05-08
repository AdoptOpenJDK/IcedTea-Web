/* BasicValueCheckers.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;

import java.io.File;


public class ValidatorUtils {


    public final static String DELIMITER = ",";

    public static String[] splitCombination(final String value) {
        Assert.requireNonNull(value, "value");
        return value.split(DELIMITER);
    }

    public static String verifyFileOrCommand(final String value) {
        final String cmd = value.split("\\s+")[0];
          if (cmd.equals(ConfigurationConstants.ALWAYS_ASK) || cmd.equals(ConfigurationConstants.INTERNAL_HTML)) {
              return "keyword";
          }
        final File fileCandidate = new File(cmd);
        if (fileCandidate.exists() && !fileCandidate.isDirectory()) {
            return cmd;
        }
        final String path = System.getenv("PATH");
        if (path != null) {
            final String[] pathMembers = path.split(File.pathSeparator);
            for (final String s : pathMembers) {
                final File pathCandidate = new File(s, cmd);
                if (pathCandidate.exists() && !pathCandidate.isDirectory()) {
                    return pathCandidate.toString();
                }
            }
        }
        return null;
    }


}
