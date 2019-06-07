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
package net.adoptopenjdk.icedteaweb.client.parts.splashscreen.parts;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind.DEFAULT;

/**
 * This class is wrapper around the *information* tag element which should
 * javaws provide from source jnlp file
 */
public class InformationElement {

    private final static Logger LOG = LoggerFactory.getLogger(InformationElement.class);

    private InfoItem title;
    private InfoItem vendor;
    private InfoItem homepage;
    private List<DescriptionInfoItem> descriptions = new ArrayList<DescriptionInfoItem>(5);

    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        this.title = new InfoItem(InformationDesc.TITLE_ELEMENT, title);
    }

    public void setvendor(String vendor) {
        if (vendor == null) {
            return;
        }
        this.vendor = new InfoItem(InformationDesc.VENDOR_ELEMENT, vendor);
    }

    public void setHomepage(String homepage) {
        if (homepage == null) {
            return;
        }
        this.homepage = new InfoItem(HomepageDesc.HOMEPAGE_ELEMENT, homepage);
    }

    public void addDescription(String description) {
        addDescription(description, null);
    }

    /**
     * Just one description of each kind (4 including null)  are  allowed in information element.
     * This method should throw exception when trying to add second description of same kind
     * But I do not consider it as good idea to force this behaviour for something like splash screen,
     * so I just replace the previous one with new one. without any warning
     */
    public void addDescription(String description, DescriptionKind kind) {
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
            if (DescriptionKind.ONE_LINE.equals(d.getKind())) {
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
            if (DescriptionKind.SHORT.equals(d.getKind())) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (d.getKind() == null) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (DescriptionKind.ONE_LINE.equals(d.getKind())) {
                return d;
            }
        }
        for (DescriptionInfoItem d : descriptions) {
            if (DescriptionKind.TOOLTIP.equals(d.getKind())) {
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
            ie.addDescription(file.getInformation().getDescriptionStrict(DEFAULT));
            ie.addDescription(file.getInformation().getDescriptionStrict(DescriptionKind.ONE_LINE), DescriptionKind.ONE_LINE);
            ie.addDescription(file.getInformation().getDescriptionStrict(DescriptionKind.SHORT), DescriptionKind.SHORT);
            ie.addDescription(file.getInformation().getDescriptionStrict(DescriptionKind.TOOLTIP), DescriptionKind.TOOLTIP);
            return ie;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
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
