/* 
   Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.Translator;

public abstract class ReplacingTextFormatter implements Formatter {

    public static String backupVersion;
    public static final Pattern BOLD_OPEN_PATTERN = Pattern.compile("<\\s*[Bb]\\s*>");
    public static final Pattern BOLD_CLOSE_PATTERN = Pattern.compile("<\\s*/\\s*[Bb]\\s*>");

    @Override
    public String process(String s1) {
        Matcher m1 = BOLD_OPEN_PATTERN.matcher(s1);
        String s2 = m1.replaceAll(getBoldOpening());
        Matcher m2 = BOLD_CLOSE_PATTERN.matcher(s2);
        return (m2.replaceAll(getBoldClosing()));
    }

    protected String localizeTitle(String s) {
        return Translator.R("man"+s);
    }

    @Override
    public String getBold(String s) {
        return getBoldOpening() + s + getBoldClosing();
    }

    @Override
    public String getUrl(String url) {
        return getUrl(url, url);
    }

    @Override
    public String getUrl(String url, String look) {
        if (look == null || look.trim().length() == 0 || url.equals(look)) {
            return url;
        } else {
            return look + " (" + url + ")";
        }
    }
  
    public String getVersion() {
        if (Boot.version == null) {
            if (backupVersion == null) {
                return "unknown version";
            }
            return backupVersion;
        } else {
            return Boot.version;
        }
    }

    @Override
    public String getNewLine(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(getNewLine());
        }
        return sb.toString();
    }
    
    
    
    

}
