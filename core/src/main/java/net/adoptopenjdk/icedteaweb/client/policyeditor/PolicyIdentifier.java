/*Copyright (C) 2014 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.client.policyeditor;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import sun.security.provider.PolicyParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// http://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html
public class PolicyIdentifier implements Comparable<PolicyIdentifier>, Serializable {

    public static final PolicyIdentifier ALL_APPLETS_IDENTIFIER = new PolicyIdentifier(null, Collections.<PolicyParser.PrincipalEntry>emptySet(), null) {
        @Override
        public String toString() {
            return Translator.R("PEGlobalSettings");
        }
    };

    private final String signedBy;
    private final LinkedHashSet<PolicyParser.PrincipalEntry> principals = new LinkedHashSet<>();
    private final String codebase;

    public PolicyIdentifier(final String signedBy, final Collection<PolicyParser.PrincipalEntry> principals, final String codebase) {
        if (signedBy != null && signedBy.isEmpty()) {
            this.signedBy = null;
        } else {
            this.signedBy = signedBy;
        }
        this.principals.addAll(principals);
        if (codebase == null) {
            this.codebase = "";
        } else {
            this.codebase = codebase;
        }
    }

    public String getSignedBy() {
        return signedBy;
    }

    public Set<PolicyParser.PrincipalEntry> getPrincipals() {
        return principals;
    }

    public String getCodebase() {
        return codebase;
    }

    public static boolean isDefaultPolicyIdentifier(final PolicyIdentifier policyIdentifier) {
        return policyIdentifier.getSignedBy() == null
                && policyIdentifier.getPrincipals().isEmpty()
                && policyIdentifier.getCodebase().isEmpty();
    }

    @Override
    public String toString() {
        final String newline = "<br>";
        final List<String> props = new ArrayList<>();
        if (!codebase.isEmpty()) {
            props.add("codebase=" + codebase);
            props.add(newline);
        }
        if (!principals.isEmpty()) {
            props.add("principals=" + principals);
            props.add(newline);
        }
        if (signedBy != null && !signedBy.isEmpty()) {
            props.add("signedBy=" + signedBy);
            props.add(newline);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        if (!props.isEmpty()) {
            for (final String prop : props.subList(0, props.size() - 1)) {
                sb.append(prop);
            }
        }
        sb.append("</html>");
        return sb.toString();
    }

    public String toStringNoHtml() {
        return "codebase='" + codebase + '\'' +
                   " principals=" + principals +
                   " signedBy='" + signedBy + '\'';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyIdentifier)) return false;

        final PolicyIdentifier that = (PolicyIdentifier) o;

        if (signedBy != null ? !signedBy.equals(that.signedBy) : that.signedBy != null) return false;
        if (!principals.equals(that.principals)) return false;
        return codebase.equals(that.codebase);

    }

    @Override
    public int hashCode() {
        int result = signedBy != null ? signedBy.hashCode() : 0;
        result = 31 * result + (principals != null ? principals.hashCode() : 0);
        result = 31 * result + (codebase.hashCode());
        return result;
    }

    @Override
    public int compareTo(PolicyIdentifier policyIdentifier) {
        if (this.equals(ALL_APPLETS_IDENTIFIER) && policyIdentifier.equals(ALL_APPLETS_IDENTIFIER)) {
            return 0;
        } else if (this.equals(ALL_APPLETS_IDENTIFIER) && !policyIdentifier.equals(ALL_APPLETS_IDENTIFIER)) {
            return -1;
        } else if (!this.equals(ALL_APPLETS_IDENTIFIER) && policyIdentifier.equals(ALL_APPLETS_IDENTIFIER)) {
            return 1;
        }

        final int codebaseComparison = compareComparable(this.getCodebase(), policyIdentifier.getCodebase());
        if (codebaseComparison != 0) {
            return codebaseComparison;
        }

        final int signedByComparison = compareComparable(this.getSignedBy(), policyIdentifier.getSignedBy());
        if (signedByComparison != 0) {
            return signedByComparison;
        }

        return Integer.compare(this.getPrincipals().hashCode(), policyIdentifier.getPrincipals().hashCode());
    }

    private static <T extends Comparable<T>> int compareComparable(T a, T b) {
        if (a == null && b != null) {
            return 1;
        } else if (a != null && b == null) {
            return -1;
        } else if (a == b) {
            return 0;
        } else {
            return a.compareTo(b);
        }
    }
}
