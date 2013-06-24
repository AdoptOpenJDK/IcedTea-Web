#!/bin/sh

# Test for autoconf commands.

# Test for autoconf.

HAVE_AUTOCONF=false

for AUTOCONF in autoconf autoconf259 autoconf2.59; do
    if ${AUTOCONF} --version > /dev/null 2>&1; then
        AUTOCONF_VERSION=`${AUTOCONF} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOCONF_VERSION}
        case ${AUTOCONF_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTOCONF=true
                break;
                ;;
        esac
    fi
done

# Test for autoheader.

HAVE_AUTOHEADER=false

for AUTOHEADER in autoheader autoheader259 autoheader2.59; do
    if ${AUTOHEADER} --version > /dev/null 2>&1; then
        AUTOHEADER_VERSION=`${AUTOHEADER} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOHEADER_VERSION}
        case ${AUTOHEADER_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTOHEADER=true
                break;
                ;;
        esac
    fi
done

# Test for autom4te.

HAVE_AUTOM4TE=false

for AUTOM4TE in autom4te autom4te259 autom4te2.59; do
    if ${AUTOM4TE} --version > /dev/null 2>&1; then
        AUTOM4TE_VERSION=`${AUTOM4TE} --version | head -1 | sed 's/^[^ ]* [^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOM4TE_VERSION}
        case ${AUTOM4TE_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTOM4TE=true
                break;
                ;;
        esac
    fi
done

# Test for autoreconf.

HAVE_AUTORECONF=false

for AUTORECONF in autoreconf autoreconf2.59; do
    if ${AUTORECONF} --version > /dev/null 2>&1; then
        AUTORECONF_VERSION=`${AUTORECONF} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTORECONF_VERSION}
        case ${AUTORECONF_VERSION} in
            2.59* | 2.6[0-9]* )
                HAVE_AUTORECONF=true
                break;
                ;;
        esac
    fi
done

if test ${HAVE_AUTOCONF} = false; then
    echo "No proper autoconf was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi

if test ${HAVE_AUTOHEADER} = false; then
    echo "No proper autoheader was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi

if test ${HAVE_AUTOM4TE} = false; then
    echo "No proper autom4te was found."
    echo "You must have autom4te 2.59 or later installed."
    exit 1
fi

if test ${HAVE_AUTORECONF} = false; then
    echo "No proper autoreconf was found."
    echo "You must have autoconf 2.59 or later installed."
    exit 1
fi


# Test for automake commands.

# Test for aclocal.

HAVE_ACLOCAL=false

for ACLOCAL in aclocal aclocal-1.10; do
    if ${ACLOCAL} --version > /dev/null 2>&1; then
        ACLOCAL_VERSION=`${ACLOCAL} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${ACLOCAL_VERSION}
        case ${ACLOCAL_VERSION} in
            1.9.[6-9] | 1.1[0-9]* )
                HAVE_ACLOCAL=true
                break;
                ;;
        esac
    fi
done

# Test for automake.

HAVE_AUTOMAKE=false

for AUTOMAKE in automake automake-1.10; do
    if ${AUTOMAKE} --version > /dev/null 2>&1; then
        AUTOMAKE_VERSION=`${AUTOMAKE} --version | head -1 | sed 's/^[^0-9]*\([0-9.][0-9.]*\).*/\1/'`
#        echo ${AUTOMAKE_VERSION}
        case ${AUTOMAKE_VERSION} in
            1.9.[6-9] | 1.1[0-9]* )
                HAVE_AUTOMAKE=true
                break;
                ;;
        esac
    fi
done

if test ${HAVE_ACLOCAL} = false; then
    echo "No proper aclocal was found."
    echo "You must have automake 1.9.6 or later installed."
    exit 1
fi

if test ${HAVE_AUTOMAKE} = false; then
    echo "No proper automake was found."
    echo "You must have automake 1.9.6 or later installed."
    exit 1
fi


export ACLOCAL AUTOCONF AUTOHEADER AUTOM4TE AUTOMAKE

${AUTORECONF} --force --install
