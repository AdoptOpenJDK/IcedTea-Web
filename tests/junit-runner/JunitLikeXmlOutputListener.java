/*
 * Copyright 2011 Red Hat, Inc.
 *
 * This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.Remote;
import net.sourceforge.jnlp.browsertesting.Browsers;


import org.junit.internal.JUnitSystem;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * This class listens for events in junit testsuite and wrote output to xml.
 * Xml tryes to follow ant-tests schema, and is enriched for by-class statistics
 * stdout and err elements are added, but must be filled from elsewhere (eg tee
 * in make) as junit suite and listener run from our executer have no access to
 * them.
 * 
 */
public class JunitLikeXmlOutputListener extends RunListener {

    private BufferedWriter writer;
    private Failure testFailed = null;
    private static final String ROOT = "testsuite";
    private static final String DATE_ELEMENT = "date";
    private static final String TEST_ELEMENT = "testcase";
    private static final String BUGS = "bugs";
    private static final String BUG = "bug";
    private static final String K2F = "known-to-fail";
    private static final String REMOTE = "remote";
    private static final String TEST_NAME_ATTRIBUTE = "name";
    private static final String TEST_TIME_ATTRIBUTE = "time";
    private static final String TEST_IGNORED_ATTRIBUTE = "ignored";
    private static final String TEST_ERROR_ELEMENT = "error";
    private static final String TEST_CLASS_ATTRIBUTE = "classname";
    private static final String ERROR_MESSAGE_ATTRIBUTE = "message";
    private static final String ERROR_TYPE_ATTRIBUTE = "type";
    private static final String SOUT_ELEMENT = "system-out";
    private static final String SERR_ELEMENT = "system-err";
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";
    private static final String TEST_CLASS_ELEMENT = "class";
    private static final String STATS_ELEMENT = "stats";
    private static final String CLASSES_ELEMENT = "classes";
    private static final String SUMMARY_ELEMENT = "summary";
    private static final String SUMMARY_TOTAL_ELEMENT = "total";
    private static final String SUMMARY_PASSED_ELEMENT = "passed";
    private static final String SUMMARY_FAILED_ELEMENT = "failed";
    private static final String SUMMARY_IGNORED_ELEMENT = "ignored";
    private long testStart;

    private int  failedK2F=0;
    private int  passedK2F=0;
    private int  ignoredK2F=0;

    private class ClassStat {

        Class<?> c;
        int total;
        int failed;
        int passed;
        int ignored;
        long time = 0;
        int  totalK2F=0;
        int  failedK2F=0;
        int  passedK2F=0;
        int  ignoredK2F=0;
    }
    Map<String, ClassStat> classStats = new HashMap<String, ClassStat>();

    public JunitLikeXmlOutputListener(JUnitSystem system, File f) {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        openElement(ROOT);
        writeElement(DATE_ELEMENT, new Date().toString());
    }

    private void openElement(String name) throws IOException {
        openElement(name, null);
    }

    private void openElement(String name, Map<String, String> atts) throws IOException {
        StringBuilder attString = new StringBuilder();
        if (atts != null) {
            attString.append(" ");
            Set<Entry<String, String>> entries = atts.entrySet();
            for (Entry<String, String> entry : entries) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (v == null) {
                    v = "null";
                }
                attString.append(k).append("=\"").append(attributize(v)).append("\"");
                attString.append(" ");
            }
        }
        writer.write("<" + name + attString.toString() + ">");
        writer.newLine();
    }

    private static String attributize(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }

    private void closeElement(String name) throws IOException {
        writer.newLine();
        writer.write("</" + name + ">");
        writer.newLine();
    }

    private void writeContent(String content) throws IOException {
        writer.write(CDATA_START + content + CDATA_END);
    }

    private void writeElement(String name, String content) throws IOException {
        writeElement(name, content, null);
    }

    private void writeElement(String name, String content, Map<String, String> atts) throws IOException {
        openElement(name, atts);
        writeContent(content);
        closeElement(name);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testFailed = null;
        testStart = System.nanoTime();
    }

    @Override
    public void testFailure(Failure failure) throws IOException {
        testFailed = failure;
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        testDone(description, 0, 0, true);
    }

    @Override
    public void testFinished(org.junit.runner.Description description) throws Exception {
        long testTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStart);
        double testTimeSeconds = ((double) testTime) / 1000d;
        testDone(description, testTime, testTimeSeconds, false);
    }
    

    @SuppressWarnings("unchecked")
    private void testDone(Description description, long testTime, double testTimeSeconds, boolean ignored) throws Exception {
        Class<?> testClass = null;
        Method testMethod = null;
        try {
            testClass = description.getTestClass();
            String qs = description.getMethodName();
            //handling @Browser'bugsIds marking of used browser
            if (qs.contains(" - ")) {
                qs = qs.replaceAll(" - .*", "");
            }
            testMethod = testClass.getMethod(qs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Map<String, String> testcaseAtts = new HashMap<String, String>(4);
        NumberFormat formatter = new DecimalFormat("#0.0000");
        String stringedTime = formatter.format(testTimeSeconds);
        stringedTime.replace(",", ".");
        testcaseAtts.put(TEST_TIME_ATTRIBUTE, stringedTime);
        testcaseAtts.put(TEST_CLASS_ATTRIBUTE, description.getClassName());
        testcaseAtts.put(TEST_NAME_ATTRIBUTE, description.getMethodName());
        if (ignored){
            testcaseAtts.put(TEST_IGNORED_ATTRIBUTE, Boolean.TRUE.toString());
        }
        KnownToFail k2f = LessVerboseTextListener.getAnnotation(testClass, testMethod.getName(), KnownToFail.class);
        boolean thisTestIsK2F = false;
        Remote remote =  LessVerboseTextListener.getAnnotation(testClass, testMethod.getName(), Remote.class);
        if (k2f != null) {
            //determine if k2f in the current browser
            //??
            Browsers[] br = k2f.failsIn();
            if(0 == br.length){//the KnownToFail annotation without optional parameter
                thisTestIsK2F = true;
            }else{
                for(Browsers b : br){
                    if(description.toString().contains(b.toString())){
                        thisTestIsK2F = true;
                    }
                }
            }
        }
        if( thisTestIsK2F ) testcaseAtts.put(K2F, Boolean.TRUE.toString());
        if (remote != null) {
            testcaseAtts.put(REMOTE, Boolean.TRUE.toString());

        }
        openElement(TEST_ELEMENT, testcaseAtts);
        if (testFailed != null) {
            if (thisTestIsK2F) {
                failedK2F++;
            }
            Map<String, String> errorAtts = new HashMap<String, String>(3);

            errorAtts.put(ERROR_MESSAGE_ATTRIBUTE, testFailed.getMessage());
            int i = testFailed.getTrace().indexOf(":");
            if (i >= 0) {
                errorAtts.put(ERROR_TYPE_ATTRIBUTE, testFailed.getTrace().substring(0, i));
            } else {
                errorAtts.put(ERROR_TYPE_ATTRIBUTE, "?");
            }

            writeElement(TEST_ERROR_ELEMENT, testFailed.getTrace(), errorAtts);
        } else {
            if (thisTestIsK2F) {
                if (ignored) {
                    ignoredK2F++;
                } else {
                    passedK2F++;

                }
            }
        }
        try {
            if (testClass != null && testMethod != null) {
                Bug bug = testMethod.getAnnotation(Bug.class);
                if (bug != null) {
                    openElement(BUGS);
                    String[] bugsIds = bug.id();
                    for (String bugId : bugsIds) {
                        String idAndUrl[] = createBug(bugId);
                        Map<String, String> visibleNameAtt = new HashMap<String, String>(1);
                        visibleNameAtt.put("visibleName", idAndUrl[0]);
                        openElement(BUG, visibleNameAtt);
                        writer.write(idAndUrl[1]);
                        closeElement(BUG);
                    }
                    closeElement(BUGS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeElement(TEST_ELEMENT);
        writer.flush();

        ClassStat classStat = classStats.get(description.getClassName());
        if (classStat == null) {
            classStat = new ClassStat();
            classStat.c = description.getTestClass();
            classStats.put(description.getClassName(), classStat);
        }
        classStat.total++;
        if (thisTestIsK2F) {
            classStat.totalK2F++;
        }
        classStat.time += testTime;
        if (testFailed == null) {
            if (ignored) {
                classStat.ignored++;
                if (thisTestIsK2F) {
                    classStat.ignoredK2F++;
                }
            } else {
                classStat.passed++;
                if (thisTestIsK2F) {
                    classStat.passedK2F++;
                }
            }
        } else {
            classStat.failed++;
            if (thisTestIsK2F) {
                classStat.failedK2F++;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void testRunFinished(Result result) throws Exception {

        writeElement(SOUT_ELEMENT, "@sout@");
        writeElement(SERR_ELEMENT, "@serr@");
        openElement(STATS_ELEMENT);
        openElement(SUMMARY_ELEMENT);
        int passed = result.getRunCount() - result.getFailureCount() - result.getIgnoreCount();
        int failed = result.getFailureCount();
        int ignored = result.getIgnoreCount();
        writeElement(SUMMARY_TOTAL_ELEMENT, String.valueOf(result.getRunCount()),createKnownToFailSumamryAttribute(failedK2F+passedK2F+ignoredK2F));
        writeElement(SUMMARY_FAILED_ELEMENT, String.valueOf(failed),createKnownToFailSumamryAttribute(failedK2F));
        writeElement(SUMMARY_IGNORED_ELEMENT, String.valueOf(ignored),createKnownToFailSumamryAttribute(ignoredK2F));
        writeElement(SUMMARY_PASSED_ELEMENT, String.valueOf(passed),createKnownToFailSumamryAttribute(passedK2F));
        closeElement(SUMMARY_ELEMENT);
        openElement(CLASSES_ELEMENT);
        Set<Entry<String, ClassStat>> e = classStats.entrySet();
        for (Entry<String, ClassStat> entry : e) {

            Map<String, String> testcaseAtts = new HashMap<String, String>(3);
            testcaseAtts.put(TEST_NAME_ATTRIBUTE, entry.getKey());
            testcaseAtts.put(TEST_TIME_ATTRIBUTE, String.valueOf(entry.getValue().time));

            openElement(TEST_CLASS_ELEMENT, testcaseAtts);
            writeElement(SUMMARY_PASSED_ELEMENT, String.valueOf(entry.getValue().passed),createKnownToFailSumamryAttribute(entry.getValue().passedK2F));
            writeElement(SUMMARY_FAILED_ELEMENT, String.valueOf(entry.getValue().failed),createKnownToFailSumamryAttribute(entry.getValue().failedK2F));
            writeElement(SUMMARY_IGNORED_ELEMENT, String.valueOf(entry.getValue().ignored),createKnownToFailSumamryAttribute(entry.getValue().ignoredK2F));
            writeElement(SUMMARY_TOTAL_ELEMENT, String.valueOf(entry.getValue().total),createKnownToFailSumamryAttribute(entry.getValue().totalK2F));
            try {
                Bug b = null;
                if (entry.getValue().c != null) {
                    b = entry.getValue().c.getAnnotation(Bug.class);
                }
                if (b != null) {
                    openElement(BUGS);
                    String[] s = b.id();
                    for (String string : s) {
                        String ss[] = createBug(string);
                        Map<String, String> visibleNameAtt = new HashMap<String, String>(1);
                        visibleNameAtt.put("visibleName", ss[0]);
                        openElement(BUG, visibleNameAtt);
                        writer.write(ss[1]);
                        closeElement(BUG);
                    }
                    closeElement(BUGS);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            closeElement(TEST_CLASS_ELEMENT);
        }
        closeElement(CLASSES_ELEMENT);
        closeElement(STATS_ELEMENT);

        closeElement(ROOT);
        writer.flush();
        writer.close();

    }

    public Map<String, String> createKnownToFailSumamryAttribute(int count) {
        Map<String, String> atts = new HashMap<String, String>(1);
        atts.put(K2F, String.valueOf(count));
        return atts;
    }

    /**
     * When declare for suite class or for Test-marked method,
     * should be interpreted by report generating tool to links.
     * Known shortcuts are
     * SX  - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=X
     * PRX - http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=X
     * RHX - https://bugzilla.redhat.com/show_bug.cgi?id=X
     * DX  - http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=X
     * GX  - http://bugs.gentoo.org/show_bug.cgi?id=X
     * CAX - http://server.complang.tuwien.ac.at/cgi-bin/bugzilla/show_bug.cgi?id=X
     * LPX - https://bugs.launchpad.net/bugs/X
     *
     * http://mail.openjdk.java.net/pipermail/distro-pkg-dev/
     * and http://mail.openjdk.java.net/pipermail/ are proceed differently
     *
     * You just put eg @Bug(id="RH12345",id="http:/my.bukpage.com/terribleNew")
     * and  RH12345 will be transalated as
     * <a href="https://bugzilla.redhat.com/show_bug.cgi?id=123456">123456<a> or
     * similar, the url will be inclueded as is. Both added to proper tests or suites
     *
     * @return Strng[2]{nameToBeShown, hrefValue}
     */
    public static String[] createBug(String string) {
        String[] r = {"ex", string};
        String[] prefixes = {
            "S",
            "PR",
            "RH",
            "D",
            "G",
            "CA",
            "LP",};
        String[] urls = {
            "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=",
            "http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=",
            "https://bugzilla.redhat.com/show_bug.cgi?id=",
            "http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=",
            "http://bugs.gentoo.org/show_bug.cgi?id=",
            "http://server.complang.tuwien.ac.at/cgi-bin/bugzilla/show_bug.cgi?id",
            "https://bugs.launchpad.net/bugs/",};

        for (int i = 0; i < urls.length; i++) {
            if (string.startsWith(prefixes[i])) {
                r[0] = string;
                r[1] = urls[i] + string.substring(prefixes[i].length());
                return r;
            }

        }

        String distro = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/";
        String openjdk = "http://mail.openjdk.java.net/pipermail/";
        String pushHead = "http://icedtea.classpath.org/hg/";
        String pushBranch = "http://icedtea.classpath.org/hg/release/";
        if (string.startsWith(distro)) {
            r[0] = "distro-pkg";
            return r;
        }
        if (string.startsWith(openjdk)) {
            r[0] = "openjdk";
            return r;
        }
        if (string.startsWith(pushBranch)) {
            r[0] = "push (branch)";
            return r;
        }
        if (string.startsWith(pushHead)) {
            r[0] = "push (head)";
            return r;
        }
        return r;

    }

    public static void main(String[] args) {
        String[] q = createBug("PR608");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("S4854");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("RH649423");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("D464");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("G6554");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("CA1654");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("LP5445");
        System.out.println(q[0] + " : " + q[1]);

        q = createBug("http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2011-November/016178.html");
        System.out.println(q[0] + " : " + q[1]);
        q = createBug("http://mail.openjdk.java.net/pipermail/awt-dev/2012-March/002324.html");
        System.out.println(q[0] + " : " + q[1]);

        q = createBug("http://lists.fedoraproject.org/pipermail/chinese/2012-January/008868.html");
        System.out.println(q[0] + " : " + q[1]);
        
        q = createBug("http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7");
        System.out.println(q[0] + " : " + q[1]);
        
        q = createBug("http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d");
        System.out.println(q[0] + " : " + q[1]);
    }
}
