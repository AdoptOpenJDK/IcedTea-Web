/* MethodOverloadResolver -- Resolves overloaded methods
   Copyright (C) 2009 Red Hat

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

package sun.applet;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This class resolved overloaded methods in Java objects using a cost
 * based-approach described here:
 *
 * http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS
 */

public class MethodOverloadResolver {
    static final int NUMERIC_SAME_COST = 1;
    static final int NULL_TO_OBJECT_COST = 2;
    static final int CLASS_SAME_COST = 3;
    static final int NUMERIC_CAST_COST = 4;
    static final int NUMERIC_BOOLEAN_COST = 5;

    static final int STRING_NUMERIC_CAST_COST = 5;

    static final int CLASS_SUPERCLASS_COST = 6;

    static final int CLASS_STRING_COST = 7;
    static final int ARRAY_CAST_COST = 8;

    /* A method signature with its casted parameters
     * We pretend a Constructor is a normal 'method' for ease of code reuse */
    static class ResolvedMethod {

        private java.lang.reflect.AccessibleObject method;
        private Object[] castedParameters;
        private int cost;

        public ResolvedMethod(int cost, java.lang.reflect.AccessibleObject method, Object[] castedParameters) {
            this.cost = cost;
            this.method = method;
            this.castedParameters = castedParameters;
        }

        java.lang.reflect.AccessibleObject getAccessibleObject() {
            return method;
        }

        public Method getMethod() {
            return (Method)method;
        }

        public Constructor<?> getConstructor() {
            return (Constructor<?>)method;
        }

        public Object[] getCastedParameters() {
            return castedParameters;
        }

        public int getCost() {
            return cost;
        }
    }

    /* A cast with an associated 'cost', used for picking method overloads */
    static class WeightedCast {

        private int cost;
        private final int distance;
        private Object castedObject;

        public WeightedCast(int cost, Object castedObject) {
            this.cost = cost;
            this.castedObject = castedObject;
            this.distance = 0;
        }

        public WeightedCast(int cost, Object castedObject, int distance) {
            this.cost = cost;
            this.castedObject = castedObject;
            this.distance = distance;
        }

        public Object getCastedObject() {
            return castedObject;
        }

        public int getCost() {
            return cost;
        }

        public int getDistance() {
            return distance;
        }
    }


    public static ResolvedMethod getBestMatchMethod(Class<?> c, String methodName, Object[] args) {
        Method[] matchingMethods = getMatchingMethods(c, methodName, args.length);

        if (PluginDebug.DEBUG) { /* avoid toString if not needed */
            PluginDebug.debug("getMatchingMethod called with: "
                    + Arrays.toString(args));
        } 

        return getBestOverloadMatch(c, args, matchingMethods);
    }

    public static ResolvedMethod getBestMatchConstructor(Class<?> c, Object[] args) {
        Constructor<?>[] matchingConstructors = getMatchingConstructors(c, args.length);

        if (PluginDebug.DEBUG) { /* avoid toString if not needed */
            PluginDebug.debug("getMatchingConstructor called with: "
                    + Arrays.toString(args));
        }

        return getBestOverloadMatch(c, args, matchingConstructors);
    }

    /*
     * Get best-matching method based on a cost based overload resolution
     * algorithm is used, described here:
     * 
     * http://jdk6.java.net/plugin2/liveconnect/#OVERLOADED_METHODS
     * 
     * Note that we consider Constructor's to be 'methods' for convenience. We
     * use the common parent class of Method/Constructor, 'AccessibleObject'
     * 
     * NB: Although the spec specifies that ambiguous method calls (ie, same
     * cost) should throw errors, we simply pick the first overload for
     * simplicity. Method overrides should not be doing wildly different things
     * anyway.
     */
    static ResolvedMethod getBestOverloadMatch(Class<?> c, Object[] args,
            java.lang.reflect.AccessibleObject[] candidates) {

        int lowestCost = Integer.MAX_VALUE;
        int lowestDistance = Integer.MAX_VALUE;
        java.lang.reflect.AccessibleObject cheapestMethod = null;
        Object[] cheapestArgs = null;
        boolean ambiguous = false;

        methodLoop:
        for (java.lang.reflect.AccessibleObject candidate : candidates) {
            int methodCost = 0;
            int distance = 0;

            Class<?>[] paramTypes = getParameterTypesFor(candidate);
            Object[] castedArgs = new Object[paramTypes.length];

            // Figure out which of the matched methods best represents what we
            // want
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramTypeClass = paramTypes[i];
                Object suppliedParam = args[i];
                Class<?> suppliedParamClass = suppliedParam != null ? suppliedParam
                        .getClass() : null;

                WeightedCast weightedCast = getCostAndCastedObject(
                        suppliedParam, paramTypeClass);

                if (weightedCast == null) {
                    continue methodLoop; // Cannot call this constructor!
                }

                methodCost += weightedCast.getCost();
                distance = weightedCast.getDistance();

                Object castedObj = paramTypeClass.isPrimitive() ? 
                            weightedCast.getCastedObject() 
                          : paramTypeClass.cast(weightedCast.getCastedObject());

                castedArgs[i] = castedObj;

                if (PluginDebug.DEBUG) { /* avoid toString if not needed */
                    Class<?> castedObjClass = castedObj == null ? null : castedObj.getClass();
                    boolean castedObjIsPrim = castedObj == null ? false : castedObj.getClass().isPrimitive();

                    PluginDebug.debug("Param " + i + " of method " + candidate
                            + " has cost " + weightedCast.getCost()
                            + " distance " + weightedCast.getDistance()
                            + " original param type " + suppliedParamClass
                            + " casted to " + castedObjClass + " isPrimitive="
                            + castedObjIsPrim + " value " + castedObj);
                }
            }

            if (methodCost < lowestCost || (methodCost == lowestCost && distance <= lowestDistance)) {
                if (methodCost < lowestCost
                        || argumentsAreSubclassesOf(castedArgs, cheapestArgs)) {
                    lowestCost = methodCost;
                    cheapestArgs = castedArgs;
                    cheapestMethod = candidate;
                    lowestDistance = distance;
                    ambiguous = false;
                } else {
                    ambiguous = true;
                }
            }

        }

        // The spec says we should error out if the method call is ambiguous
        // Instead we will report it in debug output
        if (ambiguous) {
            PluginDebug.debug("*** Warning: Ambiguous overload of ", c.getClass(), "#", cheapestMethod, "!");
        }

        if (cheapestMethod == null) {
            return null;
        }

        return new ResolvedMethod(lowestCost, cheapestMethod, cheapestArgs);
    }

    public static WeightedCast getCostAndCastedObject(Object suppliedParam,
            Class<?> paramTypeClass) {
        Class<?> suppliedParamClass = suppliedParam != null ? suppliedParam
                .getClass() : null;

        boolean suppliedParamIsArray = suppliedParamClass != null
                && suppliedParamClass.isArray();

        if (suppliedParamIsArray) {
            if (paramTypeClass.isArray()) {
                return getArrayToArrayCastWeightedCost(suppliedParam,
                        paramTypeClass);
            }

            // Target type must be an array, Object or String
            // If it an object, we return "as is" [Everything can be narrowed to an
            // object, cost=CLASS_SUPERCLASS_COST]
            // If it is a string, we need to convert according to the JS engine
            // rules
            if (paramTypeClass != String.class
                    && paramTypeClass != Object.class) {
                return null;
            }
            if (paramTypeClass.equals(String.class)) {
                return new WeightedCast(ARRAY_CAST_COST,
                        arrayToJavascriptStyleString(suppliedParam));
            }
        }

        // If this is null, there are only 2 possible cases
        if (suppliedParamClass == null) {
            if (!paramTypeClass.isPrimitive()) {
                return new WeightedCast(NULL_TO_OBJECT_COST, null); // Null to any non-primitive type
            }
            return null;// Null to primitive not allowed
        }

        // Numeric type to the analogous Java primitive type
        if (paramTypeClass.isPrimitive()
                && paramTypeClass == getPrimitiveType(suppliedParam.getClass())) {
            return new WeightedCast(NUMERIC_SAME_COST, suppliedParam);

        }

        // Class type to Class type where the types are the same
        if (suppliedParamClass == paramTypeClass) {
            return new WeightedCast(CLASS_SAME_COST, suppliedParam);

        } 

        // Numeric type to a different primitive type
        boolean wrapsPrimitive = (getPrimitiveType(suppliedParam.getClass()) != null);
        if (wrapsPrimitive && paramTypeClass.isPrimitive()) {
            double val;

            // Coerce booleans
            if (suppliedParam.equals(Boolean.TRUE)) {
                val = 1.0;
            } else if (suppliedParam.equals(Boolean.FALSE)){
                val = 0.0;
            } else if (suppliedParam instanceof Character) {
                val = (double)(Character)suppliedParam;
            } else {
                val = ((Number)suppliedParam).doubleValue();
            }

            int castCost = NUMERIC_CAST_COST;
            Object castedObj;
            if (paramTypeClass.equals(Boolean.TYPE)) {
                castedObj = (val != 0D && !Double.isNaN(val));

                if (suppliedParam.getClass() != Boolean.class) {
                    castCost = NUMERIC_BOOLEAN_COST;
                }
            } else {
                castedObj = toBoxedPrimitiveType(val, paramTypeClass);
            }
            return new WeightedCast(castCost, castedObj);
        } 

        // Numeric string to numeric type
        if (isNumericString(suppliedParam) && paramTypeClass.isPrimitive()) {
            Object castedObj;
            if (paramTypeClass.equals(Character.TYPE)) {
                castedObj = (char) Short.decode((String)suppliedParam).shortValue();
            } else {
                castedObj = stringAsPrimitiveType((String)suppliedParam, paramTypeClass);
            }
            return new WeightedCast(STRING_NUMERIC_CAST_COST, castedObj);
        } 

        // Same cost as above
        if (suppliedParam instanceof java.lang.String
                && (paramTypeClass == java.lang.Boolean.class || paramTypeClass == java.lang.Boolean.TYPE)) {
            return new WeightedCast(STRING_NUMERIC_CAST_COST, !suppliedParam.equals(""));
        }

        // Class type to superclass type;
        if (paramTypeClass.isAssignableFrom(suppliedParamClass)) {
            return new WeightedCast(CLASS_SUPERCLASS_COST, paramTypeClass.cast(suppliedParam), classDistance(suppliedParamClass, paramTypeClass));
        }

        // Any java value to String
        if (paramTypeClass.equals(String.class)) {
            return new WeightedCast(CLASS_STRING_COST, suppliedParam.toString());
        }

        return null;
    }

    private static int classDistance(Class<?> subClass, Class<?> superClass) {
        
        int distance = 0;
        
        if (superClass.isAssignableFrom(subClass)) {
            while (!subClass.equals(superClass)) {
                subClass = subClass.getSuperclass();
                distance++;
            }
        }
        
        return distance;
    }
    
    private static WeightedCast getArrayToArrayCastWeightedCost(Object suppliedArray,
            Class<?> paramTypeClass) {

        int arrLength = Array.getLength(suppliedArray);
        Class<?> arrType = paramTypeClass.getComponentType();

        // If it is an array, we need to copy/cast as we scan the array
        Object newArray = Array.newInstance(arrType, arrLength);

        for (int i = 0; i < arrLength; i++) {
            Object original = Array.get(suppliedArray, i);

            // When dealing with arrays, we represent empty slots with
            // null. We need to convert this to 0 before recursive
            // calling, since normal transformation does not allow
            // null -> primitive

            if (original == null && arrType.isPrimitive()) {
                original = 0;
            }

            WeightedCast costAndCastedObject = getCostAndCastedObject(original,
                    paramTypeClass.getComponentType());

            if (costAndCastedObject == null) {
                return null;
            }

            Array.set(newArray, i, costAndCastedObject.getCastedObject());
        }

        return new WeightedCast(ARRAY_CAST_COST, newArray);
    }

    private static Method[] getMatchingMethods(Class<?> c, String name,
            int paramCount) {
        List<Method> matchingMethods = new ArrayList<Method>();

        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) {
                if (m.getParameterTypes().length == paramCount) {
                    matchingMethods.add(m);
                }
            }
        }

        return matchingMethods.toArray(new Method[0]);
    }

    private static Constructor<?>[] getMatchingConstructors(Class<?> c,
            int paramCount) {
        List<Constructor<?>> matchingConstructors = new ArrayList<Constructor<?>>();

        for (Constructor<?> cs : c.getConstructors()) {
            if (cs.getParameterTypes().length == paramCount) {
                matchingConstructors.add(cs);
            }
        }

        return matchingConstructors.toArray(new Constructor<?>[0]);
    }

    private static Class<?> getPrimitiveType(Class<?> c) {
        if (c.isPrimitive()) {
            return c;
        }

        if (c == Byte.class) {
            return Byte.TYPE;
        } else if (c == Character.class) {
            return Character.TYPE;
        } else if (c == Short.class) {
            return Short.TYPE;
        } else if (c == Integer.class) {
            return Integer.TYPE;
        } else if (c == Long.class) {
            return Long.TYPE;
        } else if (c == Float.class) {
            return Float.TYPE;
        } else if (c == Double.class) {
            return Double.TYPE;
        } else if (c == Boolean.class) {
            return Boolean.TYPE;
        } else {
            return null;
        }
    }

    private static boolean isNumericString(Object o) {
        // At this point, it _has_ to be a string else automatically
        // return false
        if (!(o instanceof java.lang.String)) {
            return false;
        }

        try {
            Long.parseLong((String) o); // whole number test
            return true;
        } catch (NumberFormatException nfe) {
        }

        try {
            Float.parseFloat((String) o); // decimal
            return true;
        } catch (NumberFormatException nfe) {
        }

        return false;
    }

    private static Object toBoxedPrimitiveType(double val, Class<?> c) {
        Class<?> prim = getPrimitiveType(c);

        // See if we need to collapse first
        if (prim == Integer.TYPE) {
            return (int)val;
        } else if (prim == Long.TYPE) {
            return (long)val;
        } else if (prim == Short.TYPE) {
            return (short)val;
        } else if (prim == Float.TYPE) {
            return (float)val;
        } else if (prim == Double.TYPE) {
            return val;
        } else if (prim == Byte.TYPE) {
            return (byte)val;
        } else if (prim == Character.TYPE) {
            return (char)(short)val;
        }
        return val;
    }

    private static Object stringAsPrimitiveType(String s, Class<?> c)
            throws NumberFormatException {
        double val = Double.parseDouble(s);
        return toBoxedPrimitiveType(val, c);

    }

    // Test whether we can get from 'args' to 'testArgs' only by using widening conversions,
    // eg String -> Object
    private static boolean argumentsAreSubclassesOf(Object[] args, Object[] testArgs) {
        for (int i = 0; i < args.length; i++) {
            if (!testArgs[i].getClass().isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    static Class<?>[] getParameterTypesFor(java.lang.reflect.AccessibleObject method) {
        if (method instanceof Method) {
            return ((Method)method).getParameterTypes();
        } else /*m instanceof Constructor*/ {
            return ((Constructor<?>)method).getParameterTypes();
        }
    }

    private static String arrayToJavascriptStyleString(Object array) {
        int arrLength = Array.getLength(array);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arrLength; i++) {
            Object element = Array.get(array, i);

            if (element != null) {
                if (element.getClass().isArray()) {
                    sb.append(arrayToJavascriptStyleString(element));
                } else {
                    sb.append(element);
                }
            }

            sb.append(',');
        }

        // Trim the final ","
        if (arrLength > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}