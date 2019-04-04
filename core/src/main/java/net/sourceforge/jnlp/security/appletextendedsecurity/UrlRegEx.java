/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import java.util.regex.Pattern;

public class UrlRegEx {

    private static String quoteString(String s) {
        return Pattern.quote(s);
    }

    private final String regEx;

    public static UrlRegEx quote(String s) {
        return new UrlRegEx(quoteString(s));
    }
    
    public static UrlRegEx quoteAndStar(String s) {
        return new UrlRegEx(quoteString(s)+".*");
    }
    
    public static UrlRegEx exact(String s) {
        return new UrlRegEx(s);
    }
    
    private UrlRegEx(String s) {
        regEx = s;
    }

    @Override
    public String toString() {
        return getRegEx();
    }

    public String getRegEx() {
        return regEx;
    }

    /**
     * Just cosmetic method to show nicer tables, as \Qsomething\Emaybe is most
     * common record when cell is edited, the regex is shown fully
     *
     * @return unquted pattern or original string
     */
    public String getFilteredRegEx() {
        try {
            return simpleUnquote(regEx);
        } catch (Exception ex) {
            return regEx;
        }
    }

    //needs testing
    static String replaceLast(String where, String what, String by) {
        if (!where.contains(what)) {
            return where;
        }
        StringBuilder b = new StringBuilder(where);
        b.replace(where.lastIndexOf(what), where.lastIndexOf(what)+what.length(), by);
        return b.toString();
    }
    
     //needs testing
    static String simpleUnquote(String s) {
        //escaped run needs at least \E\Q, but only single char actually hurts
        if (s.length()<=1){
            return s;
        }
        boolean in = false;
        for(int i = 1 ; i < s.length() ; i++){
            if ( i == 0) {
                continue;
            }
            if (!in && s.charAt(i) == 'Q' && s.charAt(i-1) ==  '\\'){
                in = true;
                String s1=s.substring(0, i - 1);
                String s2=s.substring(i + 1);
                s= s1+s2;
                i = i - 2;
                continue;
            }
            if (in && s.charAt(i) == 'E' && s.charAt(i-1) ==  '\\'){
                String s1=s.substring(0, i - 1);
                String s2=s.substring(i + 1);
                s= s1+s2;
                i = i - 2;
                in = false;
                continue;
            }
        }
        //all text\Etext were replaced  \Qtext\E\\E\Qtext\E
        //after above text\\Etext  should remain
        return s.replace("\\\\E", "\\E");
    }
}
