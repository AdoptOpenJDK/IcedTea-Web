AC_INIT([icedtea-web],[1.0pre],[distro-pkg-dev@openjdk.java.net])
AM_INIT_AUTOMAKE([1.9 tar-pax])
AC_CONFIG_FILES([Makefile])

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

IT_CHECK_WITH_GCJ
FIND_TOOL([ZIP], [zip])
FIND_JAVAC
FIND_JAR
FIND_ECJ_JAR
AC_CONFIG_FILES([javac], [chmod +x javac])

IT_GET_PKGVERSION
IT_OBTAIN_HG_REVISIONS
IT_CHECK_XULRUNNER_VERSION

AC_CHECK_LIB(z, main,, [AC_MSG_ERROR("zlib not found - try installing zlib-devel")])
dnl Check for libX11 headers and libraries.
PKG_CHECK_MODULES(X11, x11,[X11_FOUND=yes],[X11_FOUND=no])
if test "x${X11_FOUND}" = xno
then
  AC_MSG_ERROR([Could not find x11 - \
  Try installing libX11-devel.])
fi
AC_SUBST(X11_CFLAGS)
AC_SUBST(X11_LIBS)

AC_OUTPUT
