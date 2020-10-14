/* Bug.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.adoptopenjdk.icedteaweb.testing.annotations;

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
 * <p>
 * http://mail.openjdk.java.net/pipermail/distro-pkg-dev/
 * and http://mail.openjdk.java.net/pipermail/ are proceed differently
 * You just put eg @Bug(id="RH12345",id="http:/my.bukpage.com/terribleNew")
 * and  RH12345 will be translated as
 * <a href="https://bugzilla.redhat.com/show_bug.cgi?id=123456">123456</a> or
 * similar, the url will be included as is. Both added to proper tests or suites
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bug {
    String[] id();
}
