/*
Copyright (C) 2013 Red Hat, Inc.

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

package sun.applet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.jnlp.ServerAccess;
import netscape.javascript.JSObject;

import org.junit.Test;

import sun.applet.MethodOverloadResolver.ResolvedMethod;
import sun.applet.MethodOverloadResolver.WeightedCast;

public class MethodOverloadResolverTest {
    
    /**************************************************************************
     * MethodOverloadResolver.getCostAndCastedObject tests                    *
     **************************************************************************/

    // Helper methods

    // Helper class for overload order tests
    static class CandidateCast implements Comparable<CandidateCast>{
        public CandidateCast(int cost, Class<?> candidate) {
            this.cost = cost;
            this.candidate = candidate;
        }
        public int getCost() {
            return cost;
        }
        
        public Class<?> getCandidate() {
            return candidate;
        }

        @Override
        public int compareTo(CandidateCast other) {
            return cost > other.cost ? +1 : -1;
        }
        
        private int cost;
        private Class<?> candidate;
    }

    // asserts that these overloads have the given order of preference
    // and that none of the costs are equal
    static private void assertOverloadOrder(Object arg, Class<?>... orderedCandidates) {   
        String argClassName = arg.getClass().getSimpleName();
        TreeSet<CandidateCast> casts = new TreeSet<CandidateCast>();

        for (Class<?> candidate : orderedCandidates) {
            WeightedCast wc = MethodOverloadResolver.getCostAndCastedObject(arg, candidate);

            assertFalse("Expected valid overload from " + argClassName + " to "
                    + candidate.getSimpleName(), wc == null);

            // Check previous candidates, _should not_ be 'ambiguous', ie this cost == other cost
            for (CandidateCast cc : casts) {
                String failureString = "Unexpected ambiguity overloading " 
                        + argClassName + " between "
                        + candidate.getSimpleName() + " and " 
                        + cc.candidate.getSimpleName()
                        + " with cost " + cc.cost +"!";

                assertFalse(failureString, cc.cost == wc.getCost());
            }

            casts.add(new CandidateCast(wc.getCost(), candidate));
        }

        Class<?>[] actualOrder = new Class<?>[casts.size()];

        int n = 0;
        for (CandidateCast cc : casts) {
            actualOrder[n] = cc.candidate;
            ServerAccess.logOutputReprint(arg.getClass().getSimpleName() + " to "
                    + cc.candidate.getSimpleName() + " has cost " + cc.cost);
            n++;
        }

        assertArrayEquals(orderedCandidates, actualOrder);
    }

    // Asserts that the given overloads are all of same cost
    static private void assertInvalidOverloads(Object arg, Class<?>... candidates) {   
        for (Class<?> candidate : candidates) {
            WeightedCast wc = MethodOverloadResolver.getCostAndCastedObject(arg, candidate);

            int cost = (wc != null ? wc.getCost() : 0); // Avoid NPE on non-failure
            String argClassName = arg == null ? "null" : arg.getClass().getSimpleName();

            assertTrue("Expected to be unable to cast "
                    + argClassName + " to "
                    + candidate.getSimpleName()
                    + " but was able to with cost " + cost + "!",
                    wc == null);
        }
    }

    static private void assertNotPrimitiveCastable(Object arg) {
        assertInvalidOverloads(arg, Double.TYPE, Float.TYPE, Long.TYPE,
                Short.TYPE, Byte.TYPE, Character.TYPE);
    }

    static private void assertNotNumberCastable(Object arg) {
        assertNotPrimitiveCastable(arg);
        assertInvalidOverloads(arg, Double.class, Float.class, Long.class,
                Short.class, Byte.class, Character.class);
    }

    // Asserts that the given overloads are all of same cost
    static private void assertAmbiguousOverload(Object arg, Class<?>... candidates) {  
        String argClassName = arg == null ? "null" : arg.getClass().getSimpleName(); 
        List<CandidateCast> casts = new ArrayList<CandidateCast>();

        for (Class<?> candidate : candidates) {
            WeightedCast wc = MethodOverloadResolver.getCostAndCastedObject(arg, candidate);

            assertFalse("Expected valid overload from " + argClassName + " to "
                    + candidate.getSimpleName(), wc == null);

            // Check previous candidates, _should_ all 'ambiguous', ie this cost == other cost
            for (CandidateCast cc : casts) {
                String failureString = "Expected ambiguity " 
                        + argClassName + " between "
                        + candidate.getSimpleName() + " and " 
                        + cc.candidate.getSimpleName() 
                        + ", got costs " + wc.getCost() + " and " + cc.cost + "!";

                assertTrue(failureString, cc.cost == wc.getCost());
            }

            casts.add(new CandidateCast(wc.getCost(), candidate));
        }
    }

    // Test methods

    
    @Test
    public void testBooleanOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS

        assertOverloadOrder(new Boolean(false), Boolean.TYPE, Boolean.class,
                Double.TYPE, Object.class, String.class);
    }

    @Test
    public void testNumberOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS

        assertAmbiguousOverload(new Double(0), Integer.TYPE, Long.TYPE,
                Short.TYPE, Byte.TYPE, Character.TYPE);

        assertOverloadOrder(new Double(0), Double.TYPE, Double.class,
                Float.TYPE, Boolean.TYPE, Object.class, String.class);
    }

    @Test
    public void testStringOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS

        assertAmbiguousOverload("1", Double.TYPE, Float.TYPE, Integer.TYPE,
                Long.TYPE, Short.TYPE, Byte.TYPE);

        assertOverloadOrder("1.0", String.class, Double.TYPE, Object.class);
    }

    // Turned off until JSObject is unit-testable (privilege problem)
//    @Test
    public void testJSObjectOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS
        assertOverloadOrder(new JSObject(0L), JSObject.class, String.class);
        assertAmbiguousOverload(new JSObject(0L), Object[].class, String.class);
    }

    @Test
    public void testNullOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS

        assertNotPrimitiveCastable(null);
        assertAmbiguousOverload(null, Object.class, String.class);
    }

    @Test
    public void testInheritanceOverloading() {
        // based on http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS

        class FooParent {}
        class FooChild extends FooParent {}
        class FooChildOfChild extends FooChild {}

        assertNotNumberCastable(new FooChildOfChild());

        // NB: this is ambiguious as far as costs are concerned, however
        // MethodOverloadResolver.getBestOverloadMatch sorts out this ambiguity
        assertAmbiguousOverload(new FooChildOfChild(), FooChild.class,
                FooParent.class, Object.class);

        assertOverloadOrder(new FooChild(), FooChild.class, FooParent.class, String.class);
    }

    /**************************************************************************
     * MethodOverloadResolver.getMatchingMethod tests                         *
     **************************************************************************/

    // Helper methods

    // Convenient representation of resulting method signature
    static private String simpleSignature(java.lang.reflect.AccessibleObject m) {
        StringBuilder sb = new StringBuilder();

        for (Class<?> c : MethodOverloadResolver.getParameterTypesFor(m)) {
            sb.append(c.getSimpleName());
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2); // Trim last ", "

        return sb.toString();
    }

    static private Object[] args(Class<?> klazz, Object... params) {
        List<Object> objects = new ArrayList<Object>();
        objects.add(klazz);
        // assumes our method test name is "testmethod"
        objects.add("testmethod");
        objects.addAll(Arrays.asList(params));
        return objects.toArray( new Object[0]);
    }

    // Takes {class, method, arguments...} bundled in one array
    static private ResolvedMethod getResolvedMethod(Object[] methodAndParams) { 
        /* Copy over argument portion (class and method are bundled in same array for convenience) */
        Class<?> c = (Class<?>)methodAndParams[0];
        String methodName = (String)methodAndParams[1];
        /* Copy over argument portion (class and method are bundled in same array for convenience) */
        Object[] params = Arrays.copyOfRange(methodAndParams, 2, methodAndParams.length);

        return MethodOverloadResolver.getBestMatchMethod(c, methodName, params);
    }

    /* Assert that the overload completed properly by simply providing a type signature*/
    static private void assertExpectedOverload(Object[] methodAndParams,
            String expectedSignature, int expectedCost) {

        ResolvedMethod result = getResolvedMethod(methodAndParams);
        // Check signature array as string for convenience
        assertEquals(expectedSignature, simpleSignature(result.getAccessibleObject()));
        assertEquals(expectedCost, result.getCost());
    }

    /* Assert that the overload completed by providing the expected objects */
    static private void assertExpectedOverload(Object[] methodAndParams,  
            Object[] expectedCasts, int expectedCost) {

        ResolvedMethod result = getResolvedMethod(methodAndParams);
        assertArrayEquals(expectedCasts, result.getCastedParameters());
        assertEquals(expectedCost, result.getCost());
    }

    // Test methods

    @Test
    public void testMultipleArgResolve() {

        abstract class MultipleArg {
            public abstract void testmethod(String s, int i);
            public abstract void testmethod(String s, Integer i);
        }

        // Numeric to java primitive
        assertExpectedOverload(
                args( MultipleArg.class, "teststring", 1 ), 
                "String, int",
                MethodOverloadResolver.CLASS_SAME_COST + MethodOverloadResolver.NUMERIC_SAME_COST);

        // String to java primitive
        assertExpectedOverload(
                args( MultipleArg.class, "teststring", "1.1" ), 
                "String, int",
                MethodOverloadResolver.CLASS_SAME_COST + MethodOverloadResolver.STRING_NUMERIC_CAST_COST);

        // Null to non-primitive type
        assertExpectedOverload(
                args( MultipleArg.class, "teststring", (Object)null ), 
                "String, Integer",
                MethodOverloadResolver.CLASS_SAME_COST + MethodOverloadResolver.NULL_TO_OBJECT_COST);
    }

    @Test
    public void testBoxedNumberResolve() {

        abstract class BoxedNumber {
            public abstract void testmethod(Number n);
            public abstract void testmethod(Integer i);
        }
 
        assertExpectedOverload(
                args( BoxedNumber.class, 1), 
                "Integer", MethodOverloadResolver.CLASS_SAME_COST);

        assertExpectedOverload(
                args( BoxedNumber.class, (short)1), 
                "Number", MethodOverloadResolver.CLASS_SUPERCLASS_COST);
    }

    @Test
    public void testPrimitivesResolve() {

        abstract class Primitives {
            public abstract void testmethod(int i);
            public abstract void testmethod(long l);
            public abstract void testmethod(float f);
            public abstract void testmethod(double d);
        }

        assertExpectedOverload(
                args( Primitives.class, 1), 
                "int", MethodOverloadResolver.NUMERIC_SAME_COST);
 

        assertExpectedOverload(
                args( Primitives.class, 1L), 
                "long", MethodOverloadResolver.NUMERIC_SAME_COST);
 
        assertExpectedOverload(
                args( Primitives.class, 1.1f), 
                "float", MethodOverloadResolver.NUMERIC_SAME_COST);
 
        assertExpectedOverload(
                args( Primitives.class, 1.1), 
                "double", MethodOverloadResolver.NUMERIC_SAME_COST);
    }

    @Test
    public void testComplexResolve() {

        abstract class Complex {
            public abstract void testmethod(float f);
            public abstract void testmethod(String s);
            public abstract void testmethod(JSObject j);
        }

        assertExpectedOverload(
                args( Complex.class, 1), 
                "float", MethodOverloadResolver.NUMERIC_CAST_COST);
 

        assertExpectedOverload(
                args( Complex.class, "1"), 
                "String", MethodOverloadResolver.CLASS_SAME_COST);
 
        assertExpectedOverload(
                args( Complex.class, 1.1f), 
                "float", MethodOverloadResolver.NUMERIC_SAME_COST);

        // This test is commented out until JSObject can be unit tested (privilege problem)
//        assertExpectedOverload(
//                args( Complex.class, new JSObject(0L)), 
//                "JSObject", MethodOverloadResolver.CLASS_SAME_COST);
    }

    @Test
    public void testInheritanceResolve() {

        class FooParent {}
        class FooChild extends FooParent {}
        class FooChildOfChild extends FooChild {}

        abstract class Inheritance {
            public abstract void testmethod(FooParent fp);
            public abstract void testmethod(FooChild fc);
        }

        assertExpectedOverload(
                args( Inheritance.class, new FooParent()), 
                "FooParent", MethodOverloadResolver.CLASS_SAME_COST);
 

        assertExpectedOverload(
                args( Inheritance.class, new FooChild()), 
                "FooChild", MethodOverloadResolver.CLASS_SAME_COST);
 
        assertExpectedOverload(
                args( Inheritance.class, new FooChildOfChild()), 
                "FooChild", MethodOverloadResolver.CLASS_SUPERCLASS_COST);
    }

    /*
     * Test that arrays are casted to strings by using Javascript rules.
     * Notably, commas have no spacing, and null values are printed as empty strings.
     */
    @Test
    public void testArrayToStringResolve() {
        abstract class ArrayAsStringResolve {
            public abstract void testmethod(String stringRepr);
        }

        final Object[] asStringExpectedResult = {"foo,,bar"};

        assertExpectedOverload(
                args( ArrayAsStringResolve.class, (Object) new String[] {"foo", null, "bar"}), 
                asStringExpectedResult, MethodOverloadResolver.ARRAY_CAST_COST);
    }

    /*
     * Test that arrays are casted to other arrays by recursively invoking the 
     * casting rules on each element. 
     */
    @Test 
    public void testArrayToArrayResolve() {

        abstract class IntArrayResolve {
            public abstract void testmethod(int[] intArray);
        }

        // Note that currently, the only array actually received from the Javascript side is
        // a String[] array, but this may change.
        final Object[] intArrayExpectedResult = {new int[] {0, 1, 2}};

        assertExpectedOverload(
                args(IntArrayResolve.class, (Object) new String[] {null, "1", "2.1"}), 
                intArrayExpectedResult, MethodOverloadResolver.ARRAY_CAST_COST);

        assertExpectedOverload(
                args(IntArrayResolve.class, new int[] {0, 1, 2}), 
                intArrayExpectedResult, MethodOverloadResolver.ARRAY_CAST_COST);

        assertExpectedOverload(
                args(IntArrayResolve.class, new double[] {0, 1, 2.1}), 
                intArrayExpectedResult, MethodOverloadResolver.ARRAY_CAST_COST);

        abstract class NestedArrayResolve {
            public abstract void testmethod(int[][] nestedArray);
        }

        final Object[] nestedArrayExpectedResult = { new int[][] { {1,1}, {2,2} } };

        assertExpectedOverload(
                args(NestedArrayResolve.class, (Object) new String[][] { {"1", "1"}, {"2", "2"} }), 
                nestedArrayExpectedResult, MethodOverloadResolver.ARRAY_CAST_COST);
    }
}
