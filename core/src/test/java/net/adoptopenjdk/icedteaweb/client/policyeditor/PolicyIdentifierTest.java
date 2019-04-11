/*Copyright (C) 2015 Red Hat, Inc.

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

import org.junit.Test;
import sun.security.provider.PolicyParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolicyIdentifierTest {

    private static final Set<PolicyParser.PrincipalEntry> principals = new HashSet<>(Arrays.asList(
            new PolicyParser.PrincipalEntry("aa", "bb"),
            new PolicyParser.PrincipalEntry("cc", "dd")
    ));
    public static final Set<PolicyParser.PrincipalEntry> EMPTY_SET = Collections.emptySet();

    @Test
    public void testEquals() {
        PolicyIdentifier exampleIdentifier = new PolicyIdentifier(null, EMPTY_SET, "http://example.com");
        PolicyIdentifier exampleIdentifier2 = new PolicyIdentifier(null, EMPTY_SET, "http://example.com");
        assertTrue(exampleIdentifier.equals(exampleIdentifier2));
        assertTrue(exampleIdentifier2.equals(exampleIdentifier));
        assertFalse(exampleIdentifier.equals(null));
        assertFalse(exampleIdentifier.equals(new Object()));
        assertFalse(exampleIdentifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER));
    }

    @Test
    public void testHashCode() {
        PolicyIdentifier exampleIdentifier = new PolicyIdentifier(null, EMPTY_SET, "http://example.com");
        PolicyIdentifier exampleIdentifier2 = new PolicyIdentifier(null, EMPTY_SET, "http://example.com");
        assertTrue(exampleIdentifier.hashCode() == exampleIdentifier2.hashCode());
        assertTrue(exampleIdentifier2.hashCode() == exampleIdentifier.hashCode());
        assertFalse(exampleIdentifier.hashCode() == PolicyIdentifier.ALL_APPLETS_IDENTIFIER.hashCode());
    }

    @Test
    public void testCompareTo() {
        PolicyIdentifier exampleIdentifier1 = createIdentifier("exampleIdentifier1", null, EMPTY_SET, "http://example.com");
        PolicyIdentifier exampleIdentifier2 = createIdentifier("exampleIdentifier2", null, EMPTY_SET, "http://example.com2");
        PolicyIdentifier exampleIdentifier3 = createIdentifier("exampleIdentifier3", "signedBy1", EMPTY_SET, "http://example.com");
        PolicyIdentifier exampleIdentifier4 = createIdentifier("exampleIdentifier4", "signedBy2", EMPTY_SET, "http://example.com");
        PolicyIdentifier exampleIdentifier5 = createIdentifier("exampleIdentifier5", "signedBy2", principals, "http://example.com");
        assertLesser(PolicyIdentifier.ALL_APPLETS_IDENTIFIER, exampleIdentifier1);
        assertLesser(PolicyIdentifier.ALL_APPLETS_IDENTIFIER, exampleIdentifier2);
        assertLesser(PolicyIdentifier.ALL_APPLETS_IDENTIFIER, exampleIdentifier3);
        assertLesser(PolicyIdentifier.ALL_APPLETS_IDENTIFIER, exampleIdentifier4);
        assertTrue("ALL_APPLETS_IDENTIFIER should be equal to itself",
                PolicyIdentifier.ALL_APPLETS_IDENTIFIER.compareTo(PolicyIdentifier.ALL_APPLETS_IDENTIFIER) == 0);

        assertLesser(exampleIdentifier1, exampleIdentifier2);
        assertLesser(exampleIdentifier3, exampleIdentifier2);
        assertLesser(exampleIdentifier3, exampleIdentifier1);
        assertLesser(exampleIdentifier3, exampleIdentifier4);
        assertLesser(exampleIdentifier4, exampleIdentifier5);
    }

    @Test
    public void testCompareToCodebases() {
        PolicyIdentifier a = createIdentifier("a", null, EMPTY_SET, "a");
        PolicyIdentifier aa = createIdentifier("aa", null, EMPTY_SET, "aa");
        PolicyIdentifier aaa = createIdentifier("aaa", null, EMPTY_SET, "aaa");
        PolicyIdentifier b = createIdentifier("b", null, EMPTY_SET, "b");
        assertLesser(a, aa);
        assertLesser(a, aaa);
        assertLesser(aa, aaa);
        assertLesser(a, b);
        assertLesser(aa, b);
        assertLesser(aaa, b);
    }

    @Test
    public void testCompareToSignedBys() {
        PolicyIdentifier a = createIdentifier("a", "a", EMPTY_SET, null);
        PolicyIdentifier aa = createIdentifier("aa", "aa", EMPTY_SET, null);
        PolicyIdentifier aaa = createIdentifier("aaa", "aaa", EMPTY_SET, null);
        PolicyIdentifier b = createIdentifier("b", "b", EMPTY_SET, null);
        assertLesser(a, aa);
        assertLesser(a, aaa);
        assertLesser(aa, aaa);
        assertLesser(a, b);
        assertLesser(aa, b);
        assertLesser(aaa, b);
    }

    //@Test
    public void testCompareToPrincipals() {
        // compareTo on principals set depends on implementation of Set.hashCode(), there is no real meaningful ordering on this field
    }

    static void assertLesser(PolicyIdentifier lesser, PolicyIdentifier greater) {
        String message = lesser.toString() + " should be less than " + greater.toString();
        assertTrue(message, lesser.compareTo(greater) < 0);
    }

    static PolicyIdentifier createIdentifier(final String name, String signedBy, Set<PolicyParser.PrincipalEntry> principals, String codebase) {
        return new PolicyIdentifier(signedBy, principals, codebase) {
            @Override
            public String toString() {
                return name;
            }
        };
    }

}
