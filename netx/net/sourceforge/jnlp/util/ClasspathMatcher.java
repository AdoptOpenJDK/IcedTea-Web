// Copyright (C) 2013 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.sourceforge.jnlp.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClasspathMatcher {

    public static class ClasspathMatchers {

        private final ArrayList<ClasspathMatcher> matchers;
        private final boolean includePath;

        ArrayList<ClasspathMatcher> getMatchers() {
            return matchers;
        }

        /**
         * space separated list of ClasspathMatcher source strings
         *
         * @param s string to be read 
         * @return returns compiled matcher
         */
        public static ClasspathMatchers compile(String s) {
            return compile(s, false);
        }

        public static ClasspathMatchers compile(String s, boolean includePath) {
            if (s == null) {
                return new ClasspathMatchers(new ArrayList<ClasspathMatcher>(0), includePath);
            }
            String[] splitted = s.trim().split("\\s+");
            ArrayList<ClasspathMatcher> matchers = new ArrayList<>(splitted.length);
            for (String string : splitted) {
                matchers.add(ClasspathMatcher.compile(string.trim()));
            }

            return new ClasspathMatchers(matchers, includePath);
        }

        public ClasspathMatchers(ArrayList<ClasspathMatcher> matchers, boolean includePath) {
            this.matchers = matchers;
            this.includePath = includePath;
        }

        public boolean matches(URL s) {
            return or(s);
        }

        private boolean or(URL s) {
            for (ClasspathMatcher classpathMatcher : matchers) {
                if (classpathMatcher.match(s, includePath)) {
                    return true;
                }
            }
            return false;
        }

        private boolean and(URL s) {
            for (ClasspathMatcher classpathMatcher : matchers) {
                if (!classpathMatcher.match(s, includePath)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (ClasspathMatcher classpathMatcher : matchers) {
                sb.append(classpathMatcher.toString()).append(" ");
            }
            return sb.toString();
        }
    }
    public static final String PROTOCOL_DELIMITER = "://";
    public static final String PATH_DELIMITER = "/";
    public static final String PORT_DELIMITER = ":";
    private final String source;
    private Parts parts;

    static class Parts {

        String protocol;
        String domain;
        String port;
        String path;
        Pattern protocolRegEx;
        Pattern domainRegEx;
        Pattern portRegEx;
        Pattern pathRegEx;

        @Override
        public String toString() {
            return protocol + PROTOCOL_DELIMITER + domain + PORT_DELIMITER + port + PATH_DELIMITER + path;
        }

        public void compilePartsToPatterns() {
            protocolRegEx = ClasspathMatcher.sourceToRegEx(protocol);
            //the http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
            //clearly says: *.example.com  matches  both
            //https://example.com, http://example.com
            //it sounds like bug, but well, who am I...            
            domainRegEx = domainToRegEx(domain);
            portRegEx = ClasspathMatcher.sourceToRegEx(port);
            pathRegEx = ClasspathMatcher.sourceToRegEx(path);
        }

        private boolean matchDomain(String source) {
            return generalMatch(source, domainRegEx);
        }

        private boolean matchProtocol(String source) {
            return generalMatch(source, protocolRegEx);
        }

        private boolean matchPath(String source) {
            if (source.startsWith(PATH_DELIMITER)) {
                source = source.substring(1);
            }
            return generalMatch(source, pathRegEx);
        }

        private boolean matchPort(int port) {
            return generalMatch(Integer.toString(port), portRegEx);
        }

        private static boolean generalMatch(String input, Pattern pattern) {
            return pattern.matcher(input).matches();
        }

        private static Pattern domainToRegEx(String domain) {
            String pre = "";
            String post = "";
            if (domain.startsWith("*.")) {
                //this is handling case, when *.abc.xy
                //should match also abc.xy except whatever.abc.xz
                //but NOT whatewerabc.xy
                pre = "(" + convertWildcardToRegEx(domain.substring(2)) + ")|(";
                post = ")";
            }
            return Pattern.compile(pre + ClasspathMatcher.sourceToRegExString(domain) + post);
        }
    }

    /**
     * http://www.w3.org/Addressing/URL/url-spec.txt
     */
    private ClasspathMatcher(String source) {
        this.source = source;
    }

    Parts getParts() {
        return parts;
    }

    @Override
    public String toString() {
        return source;
    }

    public static ClasspathMatcher compile(String source) {
        ClasspathMatcher r = new ClasspathMatcher(source);
        r.parts = splitToParts(source);
        r.parts.compilePartsToPatterns();
        return r;
    }

  
    private boolean match(URL url, boolean includePath) {
        String protocol = url.getProtocol();
        int port = url.getPort(); //negative if not set
        String domain = url.getHost();
        String path = url.getPath();
        boolean always = parts.matchPort(port)
                && parts.matchProtocol(protocol)
                && parts.matchDomain(domain);

        if (includePath) {
            return always
                    && (parts.matchPath(UrlUtils.sanitizeLastSlash(path))
                    || parts.matchPath(path));
        } else {
            return always;
        }
    }

    /*
     * For testing purposes
     */
    public boolean match(URL url) {
        return match(url, false);
    }
    
    public boolean matchWithPath(URL url) {
        return match(url, true);
    }

    public boolean matchWithoutPath(URL url) {
        return match(url, false);
    }

    static boolean hasProtocol(final String source) {
        int indexOfProtocolMark = source.indexOf(PROTOCOL_DELIMITER);
        if (indexOfProtocolMark < 0) {
            return false;
        }
        /*
         * Here is small trap
         * We do not know, if protocol is specifed
         * if so, the protocol://blah.blah/blah is already recognized
         * but we must ensure that we have not found eg:
         * blah.blah/blah://in/path - which is perfectly valid url...
         */
        //the most easy part - dot in url
        int indexofFirstDot = source.indexOf(".");
        if (indexofFirstDot >= 0) {
            return indexOfProtocolMark < indexofFirstDot;
        }

        //more nasty part - path specified
        String degradedProtocol = source.replace(PROTOCOL_DELIMITER, "%%%");
        int indexofFirstPath = degradedProtocol.indexOf(PATH_DELIMITER);
        if (indexofFirstPath >= 0) {
            return indexOfProtocolMark < indexofFirstPath;
        }
        //no path? no dot? it must be it!
        return true;

    }

    private static String[] extractProtocolImpl(String source) {
        //we must know it have protocoll;
        return splitOnFirst(source, PROTOCOL_DELIMITER);
    }

    static String extractProtocol(String source) {
        //we must know it have protocoll;
        return extractProtocolImpl(source)[0];
    }

    static String removeProtocol(String source) {
        //we must know it have protocoll;
        return extractProtocolImpl(source)[1];
    }

    static boolean hasPath(String source) {
        //protocol free source
        return source.contains(PATH_DELIMITER);
    }

    private static String[] extractPathImpl(String source) {
        //protocol free source
        return splitOnFirst(source, PATH_DELIMITER);
    }

    static String extractPath(String source) {
        //protocol free source
        return extractPathImpl(source)[1];
    }

    static String removePath(String source) {
        //protocol free source
        return extractPathImpl(source)[0];
    }

    static boolean hasPort(String source) {
        //protocol and path free source
        return source.contains(PORT_DELIMITER);
    }

    private static String[] extractPortImpl(String source) {
        //protocol and path free source
        return splitOnFirst(source, PORT_DELIMITER);

    }

    static String extractPort(String source) {
        //protocol and path free source
        return extractPortImpl(source)[1];
    }

    static String removePort(String source) {
        //protocol and path free source
        return extractPortImpl(source)[0];
    }

    public static String[] splitOnFirst(final String source, final String delimiter) {
        String s1 = source.substring(0, source.indexOf(delimiter));
        String s2 = source.substring(source.indexOf(delimiter) + delimiter.length());
        return new String[]{s1, s2};
    }

    public static String sourceToRegExString(String s) {
        //http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
        if (s.equals("*")) {
            return ".*";
        }
        return convertWildcardToRegEx(s);
    }
    
    private static String convertWildcardToRegEx(String s) {
        if (s.startsWith("*") && s.endsWith("*")) {
            return "^.*" + Pattern.quote(s.substring(1, s.length() - 1)) + ".*$";
        } else if (s.endsWith("*")) {
            return "^" + Pattern.quote(s.substring(0, s.length() - 1)) + ".*$";

        } else if (s.startsWith("*")) {
            return "^.*" + Pattern.quote(s.substring(1)) + "$";

        } else {
            return "^" + Pattern.quote(s) + "$";
        }
    }

    public static Pattern sourceToRegEx(String s) {
        return Pattern.compile(sourceToRegExString(s));
    }

    static Parts splitToParts(String source) {
        Parts parts = new Parts();
        String urlWithoutprotocol = source;
        boolean haveProtocol = hasProtocol(source);
        if (haveProtocol) {
            parts.protocol = extractProtocol(source);
            urlWithoutprotocol = removeProtocol(source);
        } else {
            parts.protocol = "*";
        }
        boolean havePath = hasPath(urlWithoutprotocol);
        String remianedUrl = urlWithoutprotocol;
        if (havePath) {
            parts.path = extractPath(urlWithoutprotocol);
            remianedUrl = removePath(urlWithoutprotocol);
        } else {
            parts.path = "*";
        }
        //case for url like "some.url/"
        if (parts.path.length() == 0) {
            //behaving like it do not exists
            parts.path = "*";
        }

        boolean havePort = hasPort(remianedUrl);
        String domain = remianedUrl;
        if (havePort) {
            parts.port = extractPort(remianedUrl);
            domain = removePort(remianedUrl);
        } else {
            parts.port = "*";
        }
        //case for port like "some.url:"
        if (parts.port.length() == 0) {
            //behaving like it do not exists
            parts.port = "*";
        }
        parts.domain = domain;
        return parts;
    }
}
