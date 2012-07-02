package net.sourceforge.jnlp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 * You just put eg @Bug(id="RH12345",id="http:/my.bukpage.com/terribleNew")
 * and  RH12345 will be transalated as
 * <a href="https://bugzilla.redhat.com/show_bug.cgi?id=123456">123456<a> or
 * similar, the url will be inclueded as is. Both added to proper tests or suites
 *
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bug {
 public String[] id();
}
