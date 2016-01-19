AC_DEFUN_ONCE([IT_CAN_HARDLINK_TO_SOURCE_TREE],
[
  AC_CACHE_CHECK([if we can hard link rather than copy from ${abs_top_srcdir}], it_cv_hardlink_src, [
    if cp -l ${abs_top_srcdir}/README tmp.$$ >&AS_MESSAGE_LOG_FD 2>&1; then
      it_cv_hardlink_src=yes;
    else
      it_cv_hardlink_src=no;
    fi
    rm -f tmp.$$
  ])
  AM_CONDITIONAL([SRC_DIR_HARDLINKABLE], test x"${it_cv_hardlink_src}" = "xyes")
])

AC_DEFUN_ONCE([IT_CP_SUPPORTS_REFLINK],
[
  AC_CACHE_CHECK([if cp supports --reflink], it_cv_reflink, [
    touch tmp.$$
    if cp --reflink=auto tmp.$$ tmp2.$$ >&AS_MESSAGE_LOG_FD 2>&1; then
      it_cv_reflink=yes;
    else
      it_cv_reflink=no;
    fi
    rm -f tmp.$$ tmp2.$$
  ])
  AM_CONDITIONAL([CP_SUPPORTS_REFLINK], test x"${it_cv_reflink}" = "xyes")
])

AC_DEFUN_ONCE([IT_CHECK_FOR_JDK],
[
  AC_MSG_CHECKING([for a JDK home directory])
  AC_ARG_WITH([jdk-home],
             [AS_HELP_STRING([--with-jdk-home],
                              [jdk home directory \
                               (default is first predefined JDK found)])],
             [
                if test "x${withval}" = xyes
                then
                  SYSTEM_JDK_DIR=
                elif test "x${withval}" = xno
                then
	          SYSTEM_JDK_DIR=
	        else
                  SYSTEM_JDK_DIR=${withval}
                fi
              ],
              [
	        SYSTEM_JDK_DIR=
              ])
  if test -z "${SYSTEM_JDK_DIR}"; then
    for dir in /etc/alternatives/java_sdk \
               /usr/lib/jvm/java-1.7.0-openjdk \
               /usr/lib/jvm/icedtea7 \
               /usr/lib/jvm/java-7-openjdk \
               /usr/lib/jvm/java-1.8.0-openjdk \
               /usr/lib/jvm/icedtea8 \
               /usr/lib/jvm/java-8-openjdk \
               /usr/lib/jvm/java-icedtea \
               /usr/lib/jvm/java-openjdk \
               /usr/lib/jvm/openjdk \
               /usr/lib/jvm/cacao \
               /usr/lib/jvm/jamvm ; do
       if test -d $dir; then
         SYSTEM_JDK_DIR=$dir
	 break
       fi
    done
  fi
  if ! test -d "${SYSTEM_JDK_DIR}"; then
    AC_MSG_ERROR("A JDK home directory could not be found. ${SYSTEM_JDK_DIR}")
  else
    READ=`readlink -f ${SYSTEM_JDK_DIR}`
    AC_MSG_RESULT(${SYSTEM_JDK_DIR} (link to ${READ}))
  fi
  AC_SUBST(SYSTEM_JDK_DIR)
])

AC_DEFUN_ONCE([IT_CHECK_FOR_JRE],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for a JRE home directory])
  AC_ARG_WITH([jre-home],
             [AS_HELP_STRING([--with-jre-home],
                              [jre home directory \
                               (default is the JRE under the JDK)])],
             [
               SYSTEM_JRE_DIR=${withval}
             ],
             [
               SYSTEM_JRE_DIR=
             ])
  if test -z "${SYSTEM_JRE_DIR}" ; then
    SYSTEM_JRE_DIR_EIGHT_AND_LESS="${SYSTEM_JDK_DIR}/jre"
    SYSTEM_JRE_DIR_MODULAR="${SYSTEM_JDK_DIR}"
    # try jdk8 or older compliant
    if test -d "${SYSTEM_JRE_DIR_EIGHT_AND_LESS}" -a -e "${SYSTEM_JRE_DIR_EIGHT_AND_LESS}/bin/java" -a -e "${SYSTEM_JRE_DIR_EIGHT_AND_LESS}/lib/rt.jar" ; then
      SYSTEM_JRE_DIR="${SYSTEM_JRE_DIR_EIGHT_AND_LESS}"
    fi
    # still not found?
    if test -z "${SYSTEM_JRE_DIR}" ; then
      # try modular, jdk9 or higher compliant
      if test -d "${SYSTEM_JRE_DIR_MODULAR}" -a -f "${SYSTEM_JRE_DIR_MODULAR}/bin/java" -a -d "${SYSTEM_JRE_DIR_MODULAR}/lib/modules" ; then
        SYSTEM_JRE_DIR="${SYSTEM_JRE_DIR_MODULAR}"
      fi
    fi
  fi
  if ! test -d "${SYSTEM_JRE_DIR}"; then
    AC_MSG_ERROR("A JRE home directory could not be found. ${SYSTEM_JRE_DIR}")
  else
    READ=`readlink -f ${SYSTEM_JRE_DIR}`
    AC_MSG_RESULT(${SYSTEM_JRE_DIR} (link to ${READ}))
  fi
  AC_SUBST(SYSTEM_JRE_DIR)
])

AC_DEFUN_ONCE([FIND_JAVAC],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  JAVAC=${SYSTEM_JDK_DIR}/bin/javac
  IT_FIND_JAVAC
  IT_FIND_ECJ
  IT_USING_ECJ

  AC_SUBST(JAVAC)
])

AC_DEFUN([IT_FIND_ECJ],
[
  AC_ARG_WITH([ecj],
	      [AS_HELP_STRING(--with-ecj,bytecode compilation with ecj)],
  [
    if test "x${withval}" != x && test "x${withval}" != xyes && test "x${withval}" != xno; then
      IT_CHECK_ECJ(${withval})
    else
      if test "x${withval}" != xno; then
        IT_CHECK_ECJ
      fi
    fi
  ],
  [ 
    IT_CHECK_ECJ
  ])
  if test "x${JAVAC}" = "x"; then
    if test "x{ECJ}" != "x"; then
      JAVAC="${ECJ} -nowarn"
    fi
  fi
])

AC_DEFUN([IT_CHECK_ECJ],
[
  if test "x$1" != x; then
    if test -f "$1"; then
      AC_MSG_CHECKING(for ecj)
      ECJ="$1"
      AC_MSG_RESULT(${ECJ})
    else
      AC_PATH_PROG(ECJ, "$1")
    fi
  else
    AC_PATH_PROG(ECJ, "ecj")
    if test -z "${ECJ}"; then
      AC_PATH_PROG(ECJ, "ecj-3.1")
    fi
    if test -z "${ECJ}"; then
      AC_PATH_PROG(ECJ, "ecj-3.2")
    fi
    if test -z "${ECJ}"; then
      AC_PATH_PROG(ECJ, "ecj-3.3")
    fi
  fi
])

AC_DEFUN([IT_FIND_JAVAC],
[
  AC_ARG_WITH([javac],
	      [AS_HELP_STRING(--with-javac,bytecode compilation with javac)],
  [
    if test "x${withval}" != x && test "x${withval}" != xyes && test "x${withval}" != xno; then
      IT_CHECK_JAVAC(${withval})
    else
      if test "x${withval}" != xno; then
        IT_CHECK_JAVAC
      fi
    fi
  ],
  [ 
    IT_CHECK_JAVAC
  ])
])

AC_DEFUN([IT_CHECK_JAVAC],
[
  if test "x$1" != x; then
    if test -f "$1"; then
      AC_MSG_CHECKING(for javac)
      JAVAC="$1"
      AC_MSG_RESULT(${JAVAC})
    else
      AC_PATH_PROG(JAVAC, "$1")
    fi
  else
    AC_PATH_PROG(JAVAC, "javac")
  fi
])

AC_DEFUN([FIND_JAR],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for jar])
  AC_ARG_WITH([jar],
              [AS_HELP_STRING(--with-jar,specify location of Java archive tool (jar))],
  [
    JAR="${withval}"
  ],
  [
    JAR=${SYSTEM_JDK_DIR}/bin/jar
  ])
  if ! test -f "${JAR}"; then
    AC_PATH_PROG(JAR, "${JAR}")
  fi
  if test -z "${JAR}"; then
    AC_PATH_PROG(JAR, "gjar")
  fi
  if test -z "${JAR}"; then
    AC_PATH_PROG(JAR, "jar")
  fi
  if test -z "${JAR}"; then
    AC_MSG_ERROR("No Java archive tool was found.")
  fi
  AC_MSG_RESULT(${JAR})
  AC_MSG_CHECKING([whether jar supports @<file> argument])
  touch _config.txt
  cat >_config.list <<EOF
_config.txt
EOF
  if $JAR cf _config.jar @_config.list >&AS_MESSAGE_LOG_FD 2>&1; then
    JAR_KNOWS_ATFILE=1
    AC_MSG_RESULT(yes)
  else
    JAR_KNOWS_ATFILE=
    AC_MSG_RESULT(no)
  fi
  AC_MSG_CHECKING([whether jar supports stdin file arguments])
  if cat _config.list | $JAR cf@ _config.jar >&AS_MESSAGE_LOG_FD 2>&1; then
    JAR_ACCEPTS_STDIN_LIST=1
    AC_MSG_RESULT(yes)
  else
    JAR_ACCEPTS_STDIN_LIST=
    AC_MSG_RESULT(no)
  fi
  rm -f _config.list _config.jar
  AC_MSG_CHECKING([whether jar supports -J options at the end])
  if $JAR cf _config.jar _config.txt -J-Xmx896m >&AS_MESSAGE_LOG_FD 2>&1; then
    JAR_KNOWS_J_OPTIONS=1
    AC_MSG_RESULT(yes)
  else
    JAR_KNOWS_J_OPTIONS=
    AC_MSG_RESULT(no)
  fi
  rm -f _config.txt _config.jar
  AC_SUBST(JAR)
  AC_SUBST(JAR_KNOWS_ATFILE)
  AC_SUBST(JAR_ACCEPTS_STDIN_LIST)
  AC_SUBST(JAR_KNOWS_J_OPTIONS)
])

AC_DEFUN([FIND_ECJ_JAR],
[
  AC_REQUIRE([FIND_JAVAC])
  AC_MSG_CHECKING([for an ecj JAR file])
  AC_ARG_WITH([ecj-jar],
              [AS_HELP_STRING(--with-ecj-jar,specify location of the ECJ jar)],
  [
    if test -f "${withval}"; then
      ECJ_JAR="${withval}"
    fi
  ],
  [
    ECJ_JAR=
  ])
  if test -z "${ECJ_JAR}"; then
    for jar in /usr/share/java/eclipse-ecj.jar \
      /usr/share/java/ecj.jar \
      /usr/share/eclipse-ecj-3.{2,3,4,5}/lib/ecj.jar; do
        if test -e $jar; then
          ECJ_JAR=$jar
	  break
        fi
      done
      if test -z "${ECJ_JAR}"; then
        ECJ_JAR=no
      fi
  fi
  AC_MSG_RESULT(${ECJ_JAR})
  if test "x${JAVAC}" = x && test "x${ECJ_JAR}" = "xno" ; then
      AC_MSG_ERROR([cannot find a Java compiler or ecj JAR file, try --with-javac, --with-ecj or --with-ecj-jar])
  fi
  AC_SUBST(ECJ_JAR)
])

#
# IT_FIND_OPTIONAL_JAR
# --------------------
# Find an optional jar required for building and running
#
# $1 : jar/feature name
# $2 : used to set $2_JAR and WITH_$2
# $3 (optional) : used to specify additional file paths for searching
#
# Sets $2_JAR to the jar location (or blank if not found)
# Defines WITH_$2 if jar is found
# Sets $2_AVAILABLE to "true" if jar is found (or "false" if not)
#
AC_DEFUN([IT_FIND_OPTIONAL_JAR],
[
  AC_MSG_CHECKING([for $1 jar])
  AC_ARG_WITH([$1],
              [AS_HELP_STRING(--with-$1,specify location of the $1 jar)],
  [
    case "${withval}" in
      yes)
        $2_JAR=yes
        ;;
      no)
        $2_JAR=no
        ;;
      *)
        if test -f "${withval}"; then
          $2_JAR="${withval}"
        elif test -z "${withval}"; then
          $2_JAR=yes
        else
          AC_MSG_RESULT([not found])
          AC_MSG_ERROR("The $1 jar ${withval} was not found.")
        fi
        ;;
    esac
  ],
  [
    $2_JAR=yes
  ])
  it_extra_paths_$1="$3"
  if test "x${$2_JAR}" = "xyes"; then
    for path in ${it_extra_paths_$1}; do
      if test -f ${path}; then
        $2_JAR=${path}
        break
      fi
    done
  fi
  if test x"${$2_JAR}" = "xyes"; then
    if test -f "/usr/share/java/$1.jar"; then
      $2_JAR=/usr/share/java/$1.jar
    fi
  fi
  if test x"${$2_JAR}" = "xyes"; then
    $2_JAR=no
  fi
  AC_MSG_RESULT(${$2_JAR})
  AM_CONDITIONAL(WITH_$2, test x"${$2_JAR}" != "xno")
  # Clear $2_JAR if it doesn't contain a valid filename
  if test x"${$2_JAR}" = "xno"; then
    $2_JAR=
  fi
  if test -n "${$2_JAR}" ; then
    $2_AVAILABLE=true
  else
    $2_AVAILABLE=false
  fi
  AC_SUBST($2_JAR)
  AC_SUBST($2_AVAILABLE)
])

AC_DEFUN_ONCE([IT_CHECK_PLUGIN],
[
AC_MSG_CHECKING([whether to build the browser plugin])
AC_ARG_ENABLE([plugin],
              [AS_HELP_STRING([--disable-plugin],
                              [Disable compilation of browser plugin])],
              [enable_plugin="${enableval}"], [enable_plugin="yes"])
AC_MSG_RESULT(${enable_plugin})
])

AC_DEFUN_ONCE([IT_CHECK_PLUGIN_DEPENDENCIES],
[
dnl Check for plugin support headers and libraries.
dnl FIXME: use unstable
AC_REQUIRE([IT_CHECK_PLUGIN])
if test "x${enable_plugin}" = "xyes" ; then
  PKG_CHECK_MODULES(GLIB, glib-2.0)
  AC_SUBST(GLIB_CFLAGS)
  AC_SUBST(GLIB_LIBS)

  PKG_CHECK_MODULES(MOZILLA, npapi-sdk, [
    AC_CACHE_CHECK([for xulrunner version], [xulrunner_cv_collapsed_version],[
      # XXX: use NPAPI versions instead
      xulrunner_cv_collapsed_version=20000000
    ])
  ], [
    PKG_CHECK_MODULES(MOZILLA, mozilla-plugin)
  ])

  AC_SUBST(MOZILLA_CFLAGS)
  AC_SUBST(MOZILLA_LIBS)
fi
AM_CONDITIONAL(ENABLE_PLUGIN, test "x${enable_plugin}" = "xyes")
])

AC_DEFUN_ONCE([IT_CHECK_XULRUNNER_VERSION],
[
AC_REQUIRE([IT_CHECK_PLUGIN_DEPENDENCIES])
if test "x${enable_plugin}" = "xyes"
then
  AC_CACHE_CHECK([for xulrunner version], [xulrunner_cv_collapsed_version],[
    if pkg-config --modversion libxul >/dev/null 2>&1
    then
      xulrunner_cv_collapsed_version=`pkg-config --modversion libxul | awk -F. '{power=6; v=0; for (i=1; i <= NF; i++) {v += $i * 10 ^ power; power -=2}; print v}'`
    elif pkg-config --modversion mozilla-plugin >/dev/null 2>&1
    then
      xulrunner_cv_collapsed_version=`pkg-config --modversion mozilla-plugin | awk -F. '{power=6; v=0; for (i=1; i <= NF; i++) {v += $i * 10 ^ power; power -=2}; print v}'`
    else
      AC_MSG_FAILURE([cannot determine xulrunner version])
    fi])
  AC_SUBST(MOZILLA_VERSION_COLLAPSED, $xulrunner_cv_collapsed_version)
fi
])

AC_DEFUN_ONCE([IT_CHECK_FOR_TAGSOUP],
[
  AC_MSG_CHECKING([for tagsoup])
  AC_ARG_WITH([tagsoup],
             [AS_HELP_STRING([--with-tagsoup],
                             [tagsoup.jar])],
             [
                TAGSOUP_JAR=${withval}
             ],
             [
                TAGSOUP_JAR=
             ])
  if test -z "${TAGSOUP_JAR}"; then
    for dir in /usr/share/java /usr/local/share/java ; do
      if test -f $dir/tagsoup.jar; then
        TAGSOUP_JAR=$dir/tagsoup.jar
	    break
      fi
    done
  fi
  AC_MSG_RESULT(${TAGSOUP_JAR})
  if test -z "${TAGSOUP_JAR}"; then
    AC_MSG_RESULT(***********************************************)
    AC_MSG_RESULT(*  Warning you are building without tagsoup   *)
    AC_MSG_RESULT(* Some jnlps and most htmls will be malformed *)
    AC_MSG_RESULT(***********************************************)
  fi
  AC_SUBST(TAGSOUP_JAR)
  AM_CONDITIONAL([HAVE_TAGSOUP], [test x$TAGSOUP_JAR != xno -a x$TAGSOUP_JAR != x ])
])

dnl Generic macro to check for a Java class
dnl Takes the name of the class as an argument.  The macro name
dnl is usually the name of the class with '.'
dnl replaced by '_' and all letters capitalised.
dnl e.g. IT_CHECK_FOR_CLASS([JAVA_UTIL_SCANNER],[java.util.Scanner])
dnl Test class has to be in sun.applet for some internal classes
AC_DEFUN([IT_CHECK_FOR_CLASS],[
AC_REQUIRE([IT_FIND_JAVAC])
AC_REQUIRE([IT_FIND_JAVA])
AC_CACHE_CHECK([if $2 is available], it_cv_$1, [
CLASS=sun/applet/Test.java
BYTECODE=$(echo $CLASS|sed 's#\.java##')
mkdir -p tmp.$$/$(dirname $CLASS)
cd tmp.$$
cat << \EOF > $CLASS
[/* [#]line __oline__ "configure" */
package sun.applet;

import $2;

public class Test
{
  public static void main(String[] args)
    throws Exception
  {
    System.out.println(Class.forName("$2"));
  }
}
]
EOF
if $JAVAC -cp . $JAVACFLAGS -nowarn $CLASS >&AS_MESSAGE_LOG_FD 2>&1; then
  if $JAVA -classpath . $BYTECODE >&AS_MESSAGE_LOG_FD 2>&1; then
      it_cv_$1=yes;
  else
      it_cv_$1=no;
  fi
else
  it_cv_$1=no;
fi
])
rm -f $CLASS *.class
cd ..
# should be rmdir but has to be rm -rf due to sun.applet usage
rm -rf tmp.$$
if test x"${it_cv_$1}" = "xno"; then
   AC_MSG_ERROR([$2 not found.])
fi
AC_PROVIDE([$0])dnl
])

dnl Macro to check for a Java class HexDumpEncoder
AC_DEFUN([IT_CHECK_FOR_HEXDUMPENCODER],[
AC_REQUIRE([IT_FIND_JAVAC])
AC_REQUIRE([IT_FIND_JAVA])
AC_CACHE_CHECK([if HexDumpEncoder is available], it_cv_HEXDUMPENCODER, [
CLASS=sun/applet/Test.java
BYTECODE=$(echo $CLASS|sed 's#\.java##')
mkdir -p tmp.$$/$(dirname $CLASS)
cd tmp.$$
cat << \EOF > $CLASS
[/* [#]line __oline__ "configure" */
package sun.applet;

import sun.misc.*;
import sun.security.util.*;

public class Test
{
  public static void main(String[] args)
    throws Exception
  {
    try {
      System.out.println(Class.forName("sun.misc.HexDumpEncoder"));
    } catch (ClassNotFoundException e) {
      System.out.println(Class.forName("sun.security.util.HexDumpEncoder"));
    }
  }
}
]
EOF
if $JAVAC -cp . $JAVACFLAGS -nowarn $CLASS >&AS_MESSAGE_LOG_FD 2>&1; then
  if $JAVA -classpath . $BYTECODE >&AS_MESSAGE_LOG_FD 2>&1; then
      it_cv_HEXDUMPENCODER=yes;
  else
      it_cv_HEXDUMPENCODER=no;
  fi
else
  it_cv_HEXDUMPENCODER=no;
fi
])
rm -f $CLASS *.class
cd ..
# should be rmdir but has to be rm -rf due to sun.applet usage
rm -rf tmp.$$
if test x"${it_cv_HEXDUMPENCODER}" = "xno"; then
   AC_MSG_ERROR([HexDumpEncoder not found.])
fi
])

AC_DEFUN_ONCE([IT_CHECK_FOR_MERCURIAL],
[
  AC_PATH_TOOL([HG],[hg])
  AC_SUBST([HG])
])

AC_DEFUN_ONCE([IT_OBTAIN_HG_REVISIONS],
[
  AC_REQUIRE([IT_CHECK_FOR_MERCURIAL])
  ICEDTEA_REVISION="none";
  if which ${HG} >&AS_MESSAGE_LOG_FD 2>&1; then
    AC_MSG_CHECKING([for IcedTea Mercurial revision ID])
    if test -e ${abs_top_srcdir}/.hg ; then 
      ICEDTEA_REVISION="r`(cd ${abs_top_srcdir}; ${HG} id -i)`" ;
    fi ;
    AC_MSG_RESULT([${ICEDTEA_REVISION}])
    AC_SUBST([ICEDTEA_REVISION])
  fi;
  AM_CONDITIONAL([HAS_ICEDTEA_REVISION], test "x${ICEDTEA_REVISION}" != xnone)
])

AC_DEFUN_ONCE([IT_GET_PKGVERSION],
[
AC_MSG_CHECKING([for distribution package version])
AC_ARG_WITH([pkgversion],
        [AS_HELP_STRING([--with-pkgversion=PKG],
                        [Use PKG in the version string in addition to "IcedTea"])],
        [case "$withval" in
          yes) AC_MSG_ERROR([package version not specified]) ;;
          no)  PKGVERSION=none ;;
          *)   PKGVERSION="$withval" ;;
         esac],
        [PKGVERSION=none])
AC_MSG_RESULT([${PKGVERSION}])
AM_CONDITIONAL(HAS_PKGVERSION, test "x${PKGVERSION}" != "xnone") 
AC_SUBST(PKGVERSION)
])

AC_DEFUN_ONCE([IT_CHECK_GLIB_VERSION],[
   PKG_CHECK_MODULES([GLIB2_V_216],[glib-2.0 >= 2.16],[],[AC_DEFINE([LEGACY_GLIB])])
 ])

AC_DEFUN_ONCE([IT_CHECK_XULRUNNER_MIMEDESCRIPTION_CONSTCHAR],
[
  AC_MSG_CHECKING([for legacy xulrunner api])
  AC_LANG_PUSH(C++)
  CXXFLAGS_BACKUP="$CXXFLAGS"
  CXXFLAGS="$CXXFLAGS"" ""$MOZILLA_CFLAGS"
  AC_COMPILE_IFELSE([
    AC_LANG_SOURCE([[#include <npfunctions.h>
                     const  char* NP_GetMIMEDescription ()
                       {return (char*) "yap!";}]])
    ],[
    AC_MSG_RESULT(no)
    ],[
    AC_MSG_RESULT(yes)
    AC_DEFINE([LEGACY_XULRUNNERAPI])
  ])
  CXXFLAGS="$CXXFLAGS_BACKUP"
  AC_LANG_POP(C++)
])

AC_DEFUN_ONCE([IT_CHECK_XULRUNNER_REQUIRES_C11],
[
  AC_MSG_CHECKING([for xulrunner enforcing C++11 standard])
  AC_LANG_PUSH(C++)
  CXXFLAGS_BACKUP="$CXXFLAGS"
  CXXFLAGS="$CXXFLAGS"" ""$MOZILLA_CFLAGS"
  AC_COMPILE_IFELSE([
    AC_LANG_SOURCE([[#include <npapi.h>
                     #include <npruntime.h>
                     void setnpptr (NPVariant *result)
                       { VOID_TO_NPVARIANT(*result);}]])
    ],[
    AC_MSG_RESULT(no)
    CXXFLAGS="$CXXFLAGS_BACKUP"
    ],[
    AC_MSG_RESULT(yes)
    CXXFLAGS="$CXXFLAGS_BACKUP -std=c++11"
  ])
  AC_LANG_POP(C++)
])

AC_DEFUN([IT_CHECK_WITH_GCJ],
[
  AC_MSG_CHECKING([whether to compile ecj natively])
  AC_ARG_WITH([gcj],
	      [AS_HELP_STRING(--with-gcj,location of gcj for natively compiling ecj)],
  [
    GCJ="${withval}"
  ],
  [ 
    GCJ="no"
  ])
  AC_MSG_RESULT([${GCJ}])
  if test "x${GCJ}" = xyes; then
    AC_PATH_TOOL([GCJ],[gcj])
  fi
  AC_SUBST([GCJ])
])

AC_DEFUN([IT_USING_ECJ],[
AC_CACHE_CHECK([if we are using ecj as javac], it_cv_ecj, [
if $JAVAC -version 2>&1| grep '^Eclipse' >&AS_MESSAGE_LOG_FD ; then
  it_cv_ecj=yes;
else
  it_cv_ecj=no;
fi
])
USING_ECJ=$it_cv_ecj
AC_SUBST(USING_ECJ)
AC_PROVIDE([$0])dnl
])

AC_DEFUN([FIND_TOOL],
[AC_PATH_TOOL([$1],[$2])
 if test x"$$1" = x ; then
   AC_MSG_ERROR([$2 program not found in PATH])
 fi
 AC_SUBST([$1])
])

AC_DEFUN([IT_SET_ARCH_SETTINGS],
[
  case "${host_cpu}" in
    x86_64)
      BUILD_ARCH_DIR=amd64
      INSTALL_ARCH_DIR=amd64
      JRE_ARCH_DIR=amd64
      ARCHFLAG="-m64"
      ;;
    i?86)
      BUILD_ARCH_DIR=i586
      INSTALL_ARCH_DIR=i386
      JRE_ARCH_DIR=i386
      ARCH_PREFIX=${LINUX32}
      ARCHFLAG="-m32"
      ;;
    alpha*)
      BUILD_ARCH_DIR=alpha
      INSTALL_ARCH_DIR=alpha
      JRE_ARCH_DIR=alpha
      ;;
    arm*)
      BUILD_ARCH_DIR=arm
      INSTALL_ARCH_DIR=arm
      JRE_ARCH_DIR=arm
      ;;
    mips)
      BUILD_ARCH_DIR=mips
      INSTALL_ARCH_DIR=mips
      JRE_ARCH_DIR=mips
       ;;
    mipsel)
      BUILD_ARCH_DIR=mipsel
      INSTALL_ARCH_DIR=mipsel
      JRE_ARCH_DIR=mipsel
       ;;
    powerpc)
      BUILD_ARCH_DIR=ppc
      INSTALL_ARCH_DIR=ppc
      JRE_ARCH_DIR=ppc
      ARCH_PREFIX=${LINUX32}
      ARCHFLAG="-m32"
       ;;
    powerpc64)
      BUILD_ARCH_DIR=ppc64
      INSTALL_ARCH_DIR=ppc64
      JRE_ARCH_DIR=ppc64
      ARCHFLAG="-m64"
       ;;
    sparc)
      BUILD_ARCH_DIR=sparc
      INSTALL_ARCH_DIR=sparc
      JRE_ARCH_DIR=sparc
      ARCH_PREFIX=${LINUX32}
      ARCHFLAG="-m32"
       ;;
    sparc64)
      BUILD_ARCH_DIR=sparcv9
      INSTALL_ARCH_DIR=sparcv9
      JRE_ARCH_DIR=sparc64
      ARCHFLAG="-m64"
       ;;
    s390)
      BUILD_ARCH_DIR=s390
      INSTALL_ARCH_DIR=s390
      JRE_ARCH_DIR=s390
      ARCH_PREFIX=${LINUX32}
      ARCHFLAG="-m31"
       ;;
    s390x)
      BUILD_ARCH_DIR=s390x
      INSTALL_ARCH_DIR=s390x
      JRE_ARCH_DIR=s390x
      ARCHFLAG="-m64"
       ;;
    sh*)
      BUILD_ARCH_DIR=sh
      INSTALL_ARCH_DIR=sh
      JRE_ARCH_DIR=sh
      ;;
    *)
      BUILD_ARCH_DIR=`uname -m`
      INSTALL_ARCH_DIR=$BUILD_ARCH_DIR
      JRE_ARCH_DIR=$INSTALL_ARCH_DIR
      ;;
  esac
  AC_SUBST(BUILD_ARCH_DIR)
  AC_SUBST(INSTALL_ARCH_DIR)
  AC_SUBST(JRE_ARCH_DIR)
  AC_SUBST(ARCH_PREFIX)
  AC_SUBST(ARCHFLAG)
])

AC_DEFUN_ONCE([IT_FIND_JAVA],
[
  AC_REQUIRE([IT_CHECK_FOR_JRE])
  AC_MSG_CHECKING([for a Java virtual machine])
  AC_ARG_WITH([java],
              [AS_HELP_STRING(--with-java, specify location of the Java 1.7 VM)],
  [
    JAVA="${withval}"
  ],
  [
    JAVA="${SYSTEM_JRE_DIR}/bin/java"
  ])
  if ! test -f "${JAVA}"; then
    AC_PATH_PROG(JAVA, "${JAVA}")
  fi
  if test -z "${JAVA}"; then
    AC_PATH_PROG(JAVA, "java")
  fi
  if test -z "${JAVA}"; then
    AC_MSG_ERROR("A 1.7+-compatible Java VM is required.")
  fi
  AC_MSG_RESULT(${JAVA})
  AC_SUBST(JAVA)
])

AC_DEFUN_ONCE([IT_CHECK_JAVA_VERSION],
[
  AC_REQUIRE([IT_FIND_JAVA])
  AC_MSG_CHECKING([JDK version])
  JAVA_VERSION=`$JAVA -version 2>&1 | sed -n '1s/@<:@^"@:>@*"\(.*\)"$/\1/p'`
  AC_MSG_RESULT($JAVA_VERSION)
  HAVE_JAVA7=`echo $JAVA_VERSION | awk '{if ($(0) >= 1.7) print "yes"}'`
  HAVE_JAVA8=`echo $JAVA_VERSION | awk '{if ($(0) >= 1.8) print "yes"}'`
  HAVE_JAVA9=`echo $JAVA_VERSION | awk '{if ($(0) >= 1.9) print "yes"}'`
  if test -z "$HAVE_JAVA7"; then
    AC_MSG_ERROR([JDK7 or newer is required, detected was: $JAVA_VERSION])
  fi
  if ! test -z "$HAVE_JAVA8"; then
    VERSION_DEFS="-DHAVE_JAVA8"
  fi
  if ! test -z "$HAVE_JAVA9"; then
    VERSION_DEFS="-DHAVE_JAVA9"
  fi
  AC_SUBST(VERSION_DEFS)
  AM_CONDITIONAL([HAVE_JAVA7], test x"${HAVE_JAVA7}" = "xyes")
  AM_CONDITIONAL([HAVE_JAVA8], test x"${HAVE_JAVA8}" = "xyes")
  AM_CONDITIONAL([HAVE_JAVA9], test x"${HAVE_JAVA9}" = "xyes")
])

AC_DEFUN_ONCE([IT_FIND_KEYTOOL],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for keytool])
  AC_ARG_WITH([keytool],
              [AS_HELP_STRING(--with-keytool,specify location of keytool for signed part of run-netx-dist)],
  [
    if test "${withval}" = "yes" ; then 
      KEYTOOL=${SYSTEM_JDK_DIR}/bin/keytool  
    else 
      KEYTOOL="${withval}"
    fi
  ],
  [
    KEYTOOL=${SYSTEM_JDK_DIR}/bin/keytool
  ])
  if ! test -f "${KEYTOOL}"; then
    AC_PATH_PROG(KEYTOOL, keytool)
  fi
  if ! test -f "${KEYTOOL}"; then
    KEYTOOL=""
  fi
  if test -z "${KEYTOOL}" ; then
     AC_MSG_WARN("keytool not found so signed part of run-netx-dist will fail")
  fi
  AC_MSG_RESULT(${KEYTOOL})
  AC_SUBST(KEYTOOL)
])

AC_DEFUN_ONCE([IT_FIND_PACK200],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for pack200])
  AC_ARG_WITH([pack200],
              [AS_HELP_STRING(--with-pack200,specify location of pack200 for custom part of run-netx-dist)],
  [
    if test "${withval}" = "yes" ; then 
      PACK200=${SYSTEM_JDK_DIR}/bin/pack200  
    else 
      PACK200="${withval}"
    fi
  ],
  [
    PACK200=${SYSTEM_JDK_DIR}/bin/pack200
  ])
  if ! test -f "${PACK200}"; then
    AC_PATH_PROG(PACK200, pack200)
  fi
  if ! test -f "${PACK200}"; then
    PACK200=""
  fi
  if test -z "${PACK200}" ; then
     AC_MSG_WARN("pack200 not found so custom part of run-netx-dist will fail")
  fi
  AC_MSG_RESULT(${PACK200})
  AC_SUBST(PACK200)
])


AC_DEFUN_ONCE([IT_FIND_JARSIGNER],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for jarsigner])
  AC_ARG_WITH([jarsigner],
              [AS_HELP_STRING(--with-jarsigner,specify location of jarsigner for signed part od run-netx-dist)],
  [
    if test "${withval}" = "yes" ; then 
      JARSIGNER=${SYSTEM_JDK_DIR}/bin/jarsigner  
    else 
      JARSIGNER="${withval}"
    fi
  ],
  [
    JARSIGNER=${SYSTEM_JDK_DIR}/bin/jarsigner
  ])
  if ! test -f "${JARSIGNER}"; then
    AC_PATH_PROG(JARSIGNER, jarsigner,"")
  fi
  if ! test -f "${JARSIGNER}"; then
    JARSIGNER=""
  fi
  if test -z "${JARSIGNER}"; then
     AC_MSG_WARN("jarsigner not found so signed part of run-netx-dist will fail")
  fi
  AC_MSG_RESULT(${JARSIGNER})
  AC_SUBST(JARSIGNER)
])

AC_DEFUN([IT_FIND_JAVADOC],
[
  AC_REQUIRE([IT_CHECK_FOR_JDK])
  AC_MSG_CHECKING([for javadoc])
  AC_ARG_WITH([javadoc],
              [AS_HELP_STRING(--with-javadoc,specify location of Java documentation tool (javadoc))],
  [
    JAVADOC="${withval}"
  ],
  [
    JAVADOC=${SYSTEM_JDK_DIR}/bin/javadoc
  ])
  if ! test -f "${JAVADOC}"; then
    AC_PATH_PROG(JAVADOC, "${JAVADOC}")
  fi
  if test -z "${JAVADOC}"; then
    AC_PATH_PROG(JAVADOC, "javadoc")
  fi
  if test -z "${JAVADOC}"; then
    AC_PATH_PROG(JAVADOC, "gjdoc")
  fi
  if test -z "${JAVADOC}" && test "x$ENABLE_DOCS" = "xyes"; then
    AC_MSG_ERROR("No Java documentation tool was found.")
  fi
  AC_MSG_RESULT(${JAVADOC})
  AC_MSG_CHECKING([whether javadoc supports -J options])
  CLASS=pkg/Test.java
  mkdir tmp.$$
  cd tmp.$$
  mkdir pkg
  cat << \EOF > $CLASS
[/* [#]line __oline__ "configure" */
package pkg;

public class Test
{
  /**
   * Does stuff.
   *
   *
   * @param args arguments from cli.
   */
  public static void main(String[] args)
  {
    System.out.println("Hello World!");
  }
}
]
EOF
  if $JAVADOC -J-Xmx896m pkg >&AS_MESSAGE_LOG_FD 2>&1; then
    JAVADOC_KNOWS_J_OPTIONS=yes
  else
    JAVADOC_KNOWS_J_OPTIONS=no
  fi
  cd ..
  rm -rf tmp.$$
  AC_MSG_RESULT([${JAVADOC_KNOWS_J_OPTIONS}])
  AC_SUBST(JAVADOC)
  AC_SUBST(JAVADOC_KNOWS_J_OPTIONS)
  AM_CONDITIONAL([JAVADOC_SUPPORTS_J_OPTIONS], test x"${JAVADOC_KNOWS_J_OPTIONS}" = "xyes")
])

dnl Checks that sun.applet.AppletViewerPanel is available
dnl and public (via the patch in IcedTea6, applet_hole.patch)
dnl Can be removed when that is upstream or unneeded
AC_DEFUN([IT_CHECK_FOR_SUN_APPLET_ACCESSIBILITY],[
AC_REQUIRE([IT_FIND_JAVAC])
AC_REQUIRE([IT_FIND_JAVA])
AC_CACHE_CHECK([if selected classes, fields and methods from sun.applet are accessible via reflection], it_cv_applet_hole, [
CLASS=TestAppletViewer.java
BYTECODE=$(echo $CLASS|sed 's#\.java##')
mkdir -p tmp.$$
cd tmp.$$
cat << \EOF > $CLASS
[/* [#]line __oline__ "configure" */
import java.lang.reflect.*;

public class TestAppletViewer
{
  public static void main(String[] args) throws Exception
  {
   Class<?> ap = Class.forName("sun.applet.AppletPanel");
   Class<?> avp = Class.forName("sun.applet.AppletViewerPanel");
   Field f1 = ap.getDeclaredField("applet");
   Field f2 = avp.getDeclaredField("documentURL");
   Method m1 = ap.getDeclaredMethod("run");
   Method m2 = ap.getDeclaredMethod("runLoader");
   Field f3 = avp.getDeclaredField("baseURL");
  }
}
]
EOF
if $JAVAC -cp . $JAVACFLAGS -nowarn $CLASS >&AS_MESSAGE_LOG_FD 2>&1; then
  if $JAVA -classpath . $BYTECODE >&AS_MESSAGE_LOG_FD 2>&1; then
      it_cv_applet_hole=yes;
  else
      it_cv_applet_hole=no;
  fi
else
  it_cv_applet_hole=no;
fi
])
rm -f $CLASS *.class
cd ..
rmdir tmp.$$
if test x"${it_cv_applet_hole}" = "xno"; then
   AC_MSG_ERROR([Some of the checked items is not avaiable. Check logs.])
fi
AC_PROVIDE([$0])dnl
])

AC_DEFUN_ONCE([IT_SET_VERSION],
[
  AC_REQUIRE([IT_OBTAIN_HG_REVISIONS])
  AC_REQUIRE([IT_GET_PKGVERSION])
  AC_MSG_CHECKING([what version string to use])
  if test "x${ICEDTEA_REVISION}" != xnone; then
    ICEDTEA_REV="+${ICEDTEA_REVISION}"
  fi
  if test "x${PKGVERSION}" != "xnone"; then
    ICEDTEA_PKG=" (${PKGVERSION})"
  fi
  FULL_VERSION="${PACKAGE_VERSION}${ICEDTEA_REV}${ICEDTEA_PKG}"
  AC_MSG_RESULT([${FULL_VERSION}])
  AC_SUBST([FULL_VERSION])
])

dnl Allows you to configure (enable/disable/set path to) the browser
dnl REQUIRED Parameters: 
dnl [browser name, variable to store path, default command to run browser (if not provided, assume it's the same as the browser name]
AC_DEFUN([IT_FIND_BROWSER],
[
  AC_ARG_WITH([$1],
              [AS_HELP_STRING(--with-$1,specify the location of $1)],
  [
   if test "${withval}" = "no" || test "${withval}" = "yes" || test "${withval}" = "" ; then
    $2=""
   elif test -f "${withval}" ; then
    $2="${withval}"
   else 
    AC_MSG_CHECKING([for $1])
    AC_MSG_RESULT([not found])
    AC_MSG_FAILURE([invalid location specified to $1: ${withval}])
   fi
  ],
  [
   withval="yes"
  ])

  if test -f "${$2}"; then
   AC_MSG_CHECKING([for $1])
   AC_MSG_RESULT([${$2}])
  elif test "${withval}" != "no"; then
   if test $# -gt 2; then
    AC_PATH_TOOL([$2], [$3], [], [])  
   else
    AC_PATH_TOOL([$2], [$1], [], [])
   fi
  else
   AC_MSG_CHECKING([for $1])        
   AC_MSG_RESULT([no])
  fi
])

AC_DEFUN_ONCE([IT_SET_GLOBAL_BROWSERTESTS_BEHAVIOUR],
[
  AC_MSG_CHECKING([how browser test will be run])
  AC_ARG_WITH([browser-tests],
             [AS_HELP_STRING([--with-browser-tests],
                              [yes - as defined (default), no - all browser tests will be skipped, one - all "mutiple browsers" test will behave as "one" test, all - all browser tests will be run for all set browsers])],
             [
               BROWSER_SWITCH=${withval}
             ],
             [
               BROWSER_SWITCH="yes"
             ])
  D_PARAM_PART="-Dmodified.browsers.run"
  case "$BROWSER_SWITCH" in
    "yes" )
        BROWSER_TESTS_MODIFICATION="" ;;
    "no" )
        BROWSER_TESTS_MODIFICATION="$D_PARAM_PART=ignore" ;;
    "one" )
        BROWSER_TESTS_MODIFICATION="$D_PARAM_PART=one" ;;
    "all" )
        BROWSER_TESTS_MODIFICATION="$D_PARAM_PART=all" ;;
    *) 
        AC_MSG_ERROR([unknown valkue of with-browser-tests ($BROWSER_SWITCH), so not use (yes) or set yes|no|one|all])
  esac
  AC_MSG_RESULT(${BROWSER_SWITCH})
  AC_SUBST(BROWSER_TESTS_MODIFICATION)
])
