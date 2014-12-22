/*   Copyright (C) 2011 Red Hat, Inc.

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
 exception
 */
package net.sourceforge.jnlp.runtime;

import java.util.List;
import java.util.Map;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.ParserSettings;
import static net.sourceforge.jnlp.runtime.Boot.init;
import static net.sourceforge.jnlp.runtime.Translator.R;
import net.sourceforge.jnlp.util.logging.OutputController;

import net.sourceforge.jnlp.util.optionparser.OptionParser;

public final class JnlpBoot {

    private final OptionParser optionParser;

    public JnlpBoot(OptionParser optionParser) {
        this.optionParser = optionParser;
    }

    boolean run(Map<String, List<String>> extra) {
        ParserSettings settings = init(extra);
        if (settings == null) {
            return false;
        }
        try {
            OutputController.getLogger().log("Proceeding with jnlp");
            Launcher launcher = new Launcher(false);
            launcher.setParserSettings(settings);
            launcher.setInformationToMerge(extra);
            launcher.launch(Boot.getFileLocation());
        } catch (LaunchException ex) {
            // default handler prints this
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            Boot.fatalError(R("RUnexpected", ex.toString(), ex.getStackTrace()[0]));
        }
        return true;
    }
}
