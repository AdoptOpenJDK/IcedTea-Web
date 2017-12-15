AC_INIT([icedtea-web],[1.7.1],[distro-pkg-dev@openjdk.java.net], [icedtea-web], [http://icedtea.classpath.org/wiki/IcedTea-Web])
AM_INIT_AUTOMAKE([1.9 tar-pax foreign])
AC_CONFIG_FILES([Makefile netx.manifest])
AM_MAINTAINER_MODE([enable])

# Older automake doesn't generate these correctly
abs_top_builddir=`pwd -P`
AC_SUBST(abs_top_builddir)
abs_top_srcdir=`dirname $0`
cd $abs_top_srcdir
abs_top_srcdir=`pwd`
cd $abs_top_builddir
AC_SUBST(abs_top_srcdir)

AC_CANONICAL_HOST
AC_PROG_CC
AC_PROG_CXX
IT_SET_ARCH_SETTINGS

IT_CP_SUPPORTS_REFLINK
IT_CAN_HARDLINK_TO_SOURCE_TREE

AC_MSG_CHECKING([whether to build documentation])
AC_ARG_ENABLE([docs],
	      [AS_HELP_STRING([--disable-docs],
	      		      [Disable generation of documentation])],
	      [ENABLE_DOCS="${enableval}"], [ENABLE_DOCS='yes'])
AM_CONDITIONAL([ENABLE_DOCS], [test x$ENABLE_DOCS = xyes])
AC_MSG_RESULT(${ENABLE_DOCS})

AC_MSG_CHECKING([whether to filter by whitelisting on directory name when processing, compiling and running reproducers])
AC_ARG_ENABLE([whitelist-processing],
	      [AS_HELP_STRING([--enable-whitelist-processing],
	      		      [Enable whitelist filter on directory name when processing, compiling and running reproducers. Otherwise filter on testcase name when running reproducers])],
	      [ENABLE_WHITELIST="${enableval}"], [ENABLE_WHITELIST='no'])
AM_CONDITIONAL([ENABLE_WHITELIST], [test x$ENABLE_WHITELIST = xyes])
AC_MSG_RESULT(${ENABLE_WHITELIST})

AC_PATH_PROG([BIN_BASH], [bash],, [/bin])
if test x"$BIN_BASH" = x ; then
    AC_MSG_ERROR([/bin/bash is used in runtime and for about generation. Dying sooner rather then later])
fi

build_linux=no
build_windows=no
case "${host_os}" in
    linux*)
        build_linux=yes
        ;;
    cygwin*)
        build_windows=yes
        ;;
    *)
        AC_MSG_ERROR(["OS $host_os is not supported"])
        ;;
esac
AM_CONDITIONAL([LINUX], [test "$build_linux" = "yes"])
AM_CONDITIONAL([WINDOWS], [test "$build_windows" = "yes"])

IT_CHECK_WITH_GCJ
FIND_TOOL([ZIP], [zip])
FIND_JAVAC
FIND_JAR
FIND_ECJ_JAR
IT_CHECK_JAVA_VERSION
IT_FIND_JAVADOC
IT_FIND_KEYTOOL
IT_FIND_JARSIGNER
IT_FIND_PACK200

IT_SET_VERSION
IT_CHECK_XULRUNNER_VERSION
IT_CHECK_PLUGINJAR

if test "$enable_native_plugin" = yes -a  ! "$enable_pluginjar" = yes ; then
  AC_MSG_ERROR([$enable_native_plugin/$enable_pluginjar you can not build native_plugin without pluginjar])
fi

dnl PR46074 (gcc) - Missing java.net cookie code required by IcedTea plugin
dnl IT563 - NetX uses sun.security code
dnl IT605 - NetX depends on sun.misc HexDumpEncoder or sun.security.util.HexDumpEncoder
dnl IT570 - NetX depends on sun.applet.AppletViewPanel
dnl IT571 - NetX depends on com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager.java
dnl IT573 - Plugin depends on sun.awt,X11.XEmbeddedFrame.java
dnl IT575 - Plugin depends on com.sun/jndi.toolkit.url.UrlUtil
dnl IT576 - Plugin depends on sun.applet.AppletImageRef
dnl IT578 - Remove need for patching AppletPanel for Plugin/Webstart
if test x"$HAVE_JAVA9" = xyes ; then
  JAVA_BASE=java.base
  JAVA_DESKTOP=java.desktop
  JAVA_NAMING=java.naming
fi
IT_CHECK_FOR_CLASS(JAVA_UTIL_JAR_PACK200, [java.util.jar.Pack200], [some.pkg], [])
IT_CHECK_FOR_CLASS(JAVA_NET_COOKIEMANAGER, [java.net.CookieManager], [some.pkg], [])
IT_CHECK_FOR_CLASS(JAVA_NET_HTTPCOOKIE, [java.net.HttpCookie], [some.pkg], [])
IT_CHECK_FOR_CLASS(JAVA_NET_COOKIEHANDLER, [java.net.CookieHandler], [some.pkg], [])
# in jdk9, those classes are using internal apis, must be enabled via module cheats
IT_CHECK_FOR_CLASS(SUN_SECURITY_UTIL_SECURITYCONSTANTS, [sun.security.util.SecurityConstants], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_SECURITY_UTIL_HOSTNAMECHECKER, [sun.security.util.HostnameChecker], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_SECURITY_X509_X500NAME, [sun.security.x509.X500Name], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_SECURITY_ACTION_GETPROPERTYACTION, [sun.security.action.GetPropertyAction], [some.pkg], [$JAVA_BASE])
# the classname cant be substitued by variable, as it is substituted to inner class
if test x"$HAVE_JAVA9" = xyes ; then
  IT_CHECK_FOR_CLASS(HEXDUMPENCODER, [sun.security.util.HexDumpEncoder], [some.pkg], [$JAVA_BASE])
  IT_CHECK_FOR_CLASS(SUN_MISC_JARINDEX, [jdk.internal.util.jar.JarIndex], [some.pkg], [$JAVA_BASE])
else
  IT_CHECK_FOR_CLASS(HEXDUMPENCODER, [sun.misc.HexDumpEncoder], [some.pkg], [$JAVA_BASE])
  IT_CHECK_FOR_CLASS(SUN_MISC_JARINDEX, [sun.misc.JarIndex], [some.pkg], [$JAVA_BASE])
  IT_CHECK_FOR_CLASS(SUN_MISC_LAUNCHER, [sun.misc.Launcher], [some.pkg], [$JAVA_BASE])
fi
IT_CHECK_FOR_CLASS(SUN_SECURITY_VALIDATOR_VALIDATOREXCEPTION, [sun.security.validator.ValidatorException], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(COM_SUN_NET_SSL_INTERNAL_SSL_X509EXTENDEDTRUSTMANAGER, [com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_NET_WWW_PROTOCOL_JAR_URLJARFILE, [sun.net.www.protocol.jar.URLJarFile], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_NET_WWW_PROTOCOL_JAR_URLJARFILECALLBACK, [sun.net.www.protocol.jar.URLJarFileCallBack], [some.pkg], [$JAVA_BASE])
if test "x$build_linux" = xyes ; then
  IT_CHECK_FOR_CLASS(SUN_AWT_X11_XEMBEDDEDFRAME, [sun.awt.X11.XEmbeddedFrame], [some.pkg], [$JAVA_DESKTOP])
fi
if test "x$build_windows" = xyes ; then
  IT_CHECK_FOR_CLASS(SUN_AWT_WEMBEDDEDFRAME, [sun.awt.windows.WEmbeddedFrame], [some.pkg], [$JAVA_DESKTOP])
fi
IT_CHECK_FOR_CLASS(COM_SUN_JNDI_TOOLKIT_URL_URLUTIL, [com.sun.jndi.toolkit.url.UrlUtil], [some.pkg], [$JAVA_NAMING])
IT_CHECK_FOR_CLASS(SUN_NET_WWW_PROTOCOL_HTTP_HANDLER, [sun.net.www.protocol.http.Handler], [some.pkg], [$JAVA_BASE])
IT_CHECK_FOR_CLASS(SUN_APPLET_APPLETIMAGEREF, [sun.applet.AppletImageRef], [sun.applet], [$JAVA_DESKTOP])

IT_CHECK_FOR_SUN_APPLET_ACCESSIBILITY
IT_CHECK_GLIB_VERSION
IT_CHECK_XULRUNNER_MIMEDESCRIPTION_CONSTCHAR
IT_CHECK_XULRUNNER_REQUIRES_C11

#
# Find optional depedencies
#

AC_CHECK_PROGS([XSLTPROC],[xsltproc],[], [])
# browser to be linked/tested
# Example: IT_FIND_BROWSER([browser-name],[variable-to-store-path],[default-run-command-if-different-from-the-browser-name])
IT_FIND_BROWSER([firefox],[FIREFOX])
IT_FIND_BROWSER([chrome],[CHROME],[google-chrome])
IT_FIND_BROWSER([chromium],[CHROMIUM],[chromium-browser])
IT_FIND_BROWSER([opera],[OPERA])
IT_FIND_BROWSER([midori],[MIDORI])
IT_FIND_BROWSER([epiphany],[EPIPHANY])
IT_SET_GLOBAL_BROWSERTESTS_BEHAVIOUR

AM_CONDITIONAL([WITH_XSLTPROC], [test x"$XSLTPROC" != x ])
IT_FIND_OPTIONAL_JAR([rhino], RHINO,
    [/usr/share/java/js.jar /usr/share/rhino-1.6/lib/js.jar])
IT_FIND_OPTIONAL_JAR([junit], JUNIT,
    [/usr/share/java/junit4.jar /usr/share/junit-4/lib/junit.jar])
IT_FIND_OPTIONAL_JAR([hamcrest], HAMCREST,
    [/usr/share/java/hamcrest/all.jar /usr/share/java/hamcrest-core.jar])
IT_FIND_OPTIONAL_JAR([jacoco], JACOCO,
    [/usr/share/java/jacoco/org.jacoco.core.jar])
IT_FIND_OPTIONAL_JAR([asm], ASM,
    [/usr/share/java/objectweb-asm4/asm-all.jar /usr/share/java/objectweb-asm4/asm-all-4.0.jar /usr/share/java/objectweb-asm/asm-all.jar])

IT_CHECK_FOR_TAGSOUP

if test "x$build_windows" = xyes ; then
  IT_CHECK_FOR_WIX
fi
IT_FIND_OPTIONAL_JAR([wixgen], WIXGEN, [/usr/share/java/wixgen.jar $WIX_TOOLSET_DIR/wixgen.jar $WIX_TOOLSET_DIR/../wixgen.jar])

echo -n "checking for MSI deps... "
if test "x$WIX_TOOLSET_DIR" = "x" ; then
  if test "x$WIXGEN_JAR" = "x" ; then
    echo  "No WiX tools, nor wixgen jar, correct - build on linux or on windows witout MSI" 
  else
    echo  "No WiX tools, but wixgen jar - wixgen jar will do its job, but is useless. No MSI can be generated" 
  fi
else
  if test "x$WIXGEN_JAR" = "x" ; then
    echo  "WiX tools, but no wixgen jar, fatal. You need wixgen.jar from https://github.com/akashche/wixgen" 
    exit 1
  else
    echo  "WiX tools and wixgen jar - build on windows with MSI generation support" 
  fi
fi

PKG_CHECK_VAR(bashcompdir, [bash-completion], [completionsdir], ,
     bashcompdir="${sysconfdir}/bash_completion.d")
AC_SUBST(bashcompdir)

AC_CONFIG_FILES([jrunscript], [chmod u+x jrunscript])
AC_CONFIG_FILES([build.properties])

AC_OUTPUT
