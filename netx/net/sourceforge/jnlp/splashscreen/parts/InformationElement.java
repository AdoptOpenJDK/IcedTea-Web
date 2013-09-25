/* InformationElement.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
/**
http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
 */
package net.sourceforge.jnlp.splashscreen.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * This class is wrapper arround <information> tag which should
 * javaws provide from source jnlp file
 */
public class InformationElement {

    private InfoItem title;
    private InfoItem vendor;
    private InfoItem homepage;
    private List<DescriptionInfoItem> descriptions = new ArrayList<DescriptionInfoItem>(5);

    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        this.title = new InfoItem(InfoItem.title, title);
    }

    public void setvendor(String vendor) {
        if (vendor == null) {
            return;
        }
        this.vendor = new InfoItem(InfoItem.vendor, vendor);
    }

    public void setHomepage(String homepage) {
        if (homepage == null) {
            return;
        }
        this.homepage = new InfoItem(InfoItem.homepage, homepage);
    }

    public void addDescription(String description) {
        addDescription(description, null);
    }

    /**
     * Just one description of each kind (4 including null)  are  allowed in information element.
     * This method should throw exception when trying to add second description of same kind
     * But I do not consider it as good idea to force this behaviour for somesing like psalsh screen,
     * so I jsut replace the previous one with new one. without any warning
     */
    public void addDescription(String description, String kind) {
        if (description == null) {
            return;
        }
        DescriptionInfoItem d = new DescriptionInfoItem(description, kind);
        for (DescriptionInfoItem descriptionInfoItem : descriptions) {
            if (descriptionInfoItem.isOfSameKind(d)) {
                descriptions.remove(descriptionInfoItem);
                descriptions.add(d);
                return;
            }
        }
        descriptions.add(d);

    }

    public InfoItem getBestMatchingDescriptionForSplash() {
        for (DescriptionInfoItem d : descriptions) {
            if (InfoItem.descriptionKindOneLine.equals(d.getKind())) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (d.getKind() == null) {
                return d;
            }
        }
        return null;
    }

    public InfoItem getLongestDescriptionForSplash() {
        for (DescriptionInfoItem d : descriptions) {
            if (InfoItem.descriptionKindShort.equals(d.getKind())) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (d.getKind() == null) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (InfoItem.descriptionKindOneLine.equals(d.getKind())) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (InfoItem.descriptionKindToolTip.equals(d.getKind())) {
                return d;
            }
        }
        return null;
    }

    public String getTitle() {
        if (title == null) {
            return null;
        }
        return title.toNiceString();
    }

    public String getVendor() {
        if (vendor == null) {
            return null;
        }
        return vendor.toNiceString();
    }

    public String getHomepage() {
        if (homepage == null) {
            return null;
        }
        return homepage.toNiceString();
    }

    List<DescriptionInfoItem> getDescriptions() {
        return Collections.unmodifiableList(descriptions);
    }

    public String getDescription() {
        InfoItem i = getBestMatchingDescriptionForSplash();
        if (i == null) {
            return null;
        }
        return i.toNiceString();
    }

    public List<String> getHeader() {
        List<String> r = new ArrayList<String>(4);
        String t = getTitle();
        String v = getVendor();
        String h = getHomepage();
        if (t != null) {
            r.add(t);
        }
        if (v != null) {
            r.add(v);
        }
        if (h != null) {
            r.add(h);
        }

        return r;
    }

    public static InformationElement createFromJNLP(JNLPFile file) {
        try {
            if (file == null) {
                String message = Translator.R(InfoItem.SPLASH + "errorInInformation");
                InformationElement ie = new InformationElement();
                ie.setHomepage("");
                ie.setTitle(message);
                ie.setvendor("");
                ie.addDescription(message);
                return ie;
            }
            if (file.getInformation() == null) {
                String message = Translator.R(InfoItem.SPLASH + "missingInformation");
                InformationElement ie = new InformationElement();
                ie.setHomepage("");
                ie.setTitle(message);
                ie.setvendor("");
                ie.addDescription(message);
                return ie;
            }
            InformationElement ie = new InformationElement();
            String homePage = Translator.R(InfoItem.SPLASH + "defaultHomepage");
            if (file.getInformation().getHomepage() != null) {
                homePage = file.getInformation().getHomepage().toString();
            }
            ie.setHomepage(homePage);
            ie.setTitle(file.getInformation().getTitle());
            ie.setvendor(file.getInformation().getVendor());
            ie.addDescription(file.getInformation().getDescriptionStrict((String) (InformationDesc.DEFAULT)));
            ie.addDescription(file.getInformation().getDescriptionStrict(InfoItem.descriptionKindOneLine), InfoItem.descriptionKindOneLine);
            ie.addDescription(file.getInformation().getDescriptionStrict(InfoItem.descriptionKindShort), InfoItem.descriptionKindShort);
            ie.addDescription(file.getInformation().getDescriptionStrict(InfoItem.descriptionKindToolTip), InfoItem.descriptionKindToolTip);
            return ie;
        } catch (Exception ex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            String message = Translator.R(InfoItem.SPLASH + "errorInInformation");
            InformationElement ie = new InformationElement();
            ie.setHomepage("");
            ie.setTitle(message);
            ie.setvendor("");
            ie.addDescription(ex.getMessage());
            return ie;
        }
    }
}
