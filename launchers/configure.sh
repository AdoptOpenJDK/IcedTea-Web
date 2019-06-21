#!/bin/sh

VERSION=$1
LOCALIZATIONS="en_US.UTF-8 cs_CZ.UTF-8 pl_PL.UTF-8 de_DE.UTF-8"

if [ "x$VERSION"  == "x" ] ; then
 readonly VERSION="unknown"
else
 readonly VERSION=$VERSION
fi

if [ "x$JRE" == "x" ] ; then
  echo "default jre is necessary"
  exit 1
else
  readonly JRE=$JRE
fi


readonly PROJECT_TOP=`dirname $SCRIPT_DIR`
readonly TARGET=$SCRIPT_DIR/target
readonly TARGET_TMP=$TARGET/tmp
readonly TARGET_IMAGES=$TARGET/images
readonly TARGET_DOCS_PARENT=$TARGET/icedtea-web-docs
readonly TARGET_DOCS=$TARGET_DOCS_PARENT/$VERSION

rm -rf "$TARGET"


# where to search for libraris [MAVEN/SYSTEM]
if [ "x$ITW_LIBS" = "x" ] ; then
  readonly ITW_LIBS="BUNDLED"
else
  readonly ITW_LIBS=$ITW_LIBS
fi

# where to gather external libraries
if [ "x$LIB_TARGET_DIR" == "x" ] ; then
  readonly LIB_TARGET_DIR=$TARGET/libs
else
  readonly LIB_TARGET_DIR=$LIB_TARGET_DIR
fi
# where to gather itw's libraries
if [ "x$ITW_TARGET_DIR" == "x" ] ; then
  readonly ITW_TARGET_DIR=$TARGET/libs
else
  readonly ITW_TARGET_DIR=$ITW_TARGET_DIR
fi
# where to gather config files
if [ "x$ETC_TARGET_DIR" == "x" ] ; then
  readonly ETC_TARGET_DIR=$TARGET/libs
else
  readonly ETC_TARGET_DIR=$ETC_TARGET_DIR
fi
# where to executables
if [ "x$BIN_TARGET_DIR" == "x" ] ; then
  readonly BIN_TARGET_DIR=$TARGET/bin
else
  readonly BIN_TARGET_DIR=$BIN_TARGET_DIR
fi
# where to splash
if [ "x$SPLASH_TARGET_DIR" == "x" ] ; then
  readonly SPLASH_TARGET_DIR=$TARGET/libs
else
  readonly SPLASH_TARGET_DIR=$SPLASH_TARGET_DIR
fi
# where to icons
if [ "x$ICO_TARGET_DIR" == "x" ] ; then
  readonly ICO_TARGET_DIR=$TARGET/libs
else
  readonly ICO_TARGET_DIR=$ICO_TARGET_DIR
fi

readonly CORE_SRC=`ls $PROJECT_TOP/core/target/icedtea-web-core-*.jar`
readonly COMMON_SRC=`ls $PROJECT_TOP/common/target/icedtea-web-common-*.jar`
readonly JNLPAPI_SRC=`ls $PROJECT_TOP/jnlp-api/target/jnlp-api-*.jar`
readonly XMLPARSER_SRC=`ls $PROJECT_TOP/xml-parser/target/icedtea-web-xml-parser-*.jar`
readonly CLIENTS_SRC=`ls $PROJECT_TOP/clients/target/icedtea-web-clients-*.jar`
readonly JNLPSERVER_SRC=`ls $PROJECT_TOP/jnlp-servlet/target/jnlp-servlet-*.jar`

if [ "x$MAVEN_REPO" == "x" ] ; then
  readonly MAVEN_REPO=${HOME}/.m2/
else
  readonly MAVEN_REPO=$MAVEN_REPO
fi
if [ "x$SYSTEM_JARS" == "x" ] ; then
  readonly SYSTEM_JARS=/usr/share
else
  readonly SYSTEM_JARS=$SYSTEM_JARS
fi

function findJar() {
  find "$1" 2>/dev/null | grep "$2" | grep -v -e debug -e example | grep .jar$ | sort  | tail -n 1
}


function getJar() {
  if [ "x$ITW_LIBS" == "xDISTRIBUTION" ] ; then
    findJar "$SYSTEM_JARS"  "$1"
  else
    findJar "$MAVEN_REPO" "$1"
  fi
}


if [ "x$RHINO_SRC" == "x" ] ; then
  readonly RHINO_SRC=`getJar "rhino"`
else
  readonly RHINO_SRC=$RHINO_SRC
fi
if [ "x$TAGSOUP_SRC" == "x" ] ; then
  readonly TAGSOUP_SRC=`getJar "tagsoup"`
else
  readonly TAGSOUP_SRC=$TAGSOUP_SRC
fi
if [ "x$MSLINKS_SRC" == "x" ] ; then
  readonly MSLINKS_SRC=`getJar "mslinks"`
else
  readonly MSLINKS_SRC=$MSLINKS_SRC
fi

readonly SPLASH_PNG_SRC=`find $PROJECT_TOP/core/src |  grep /javaws_splash.png$`
readonly JAVAWS_ICO_SRC=`find $SCRIPT_DIR |  grep /javaws.png$`
readonly MODULARJDK_ARGS_FILE_SRC=`find $SCRIPT_DIR |  grep /itw-modularjdk.args$` 

if [ "x$KCOV_HOME" == "x" ] ; then
  readonly KCOV_HOME=$HOME/kcov
else
  readonly KCOV_HOME=$KCOV_HOME
fi

# https://github.com/SimonKagstrom/kcov/
KCOV="none" ;
if [ -e $KCOV_HOME ] ; then
	if [ -f $KCOV_HOME/kcov ] ; then
	  KCOV=$KCOV_HOME/kcov ;
	elif [ -f $KCOV_HOME/bin/kcov ] ; then
	  KCOV=$KCOV_HOME/bin/kcov ;
	elif [ -f $KCOV_HOME/build/kcov ] ; then
	  KCOV=$KCOV_HOME/build/kcov ;
	elif [ -f $KCOV_HOME/build/src/kcov ] ; then
	  KCOV=$KCOV_HOME/build/src/kcov ;
	else
	  mkdir $KCOV_HOME/build ;
	  pushd $KCOV_HOME/build ;
	    cmake .. ;
	  make ;
	  popd ;
	  KCOV=$KCOV_HOME/build/src/kcov ;
	fi ;
fi

if [ "x$CARGO_RUST" == "x" ] ; then
  readonly CARGO_RUST=cargo
else
  readonly CARGO_RUST="$CARGO_RUST"
fi

isWindows() {
  if [[ $( uname ) == *"NT"* ]]; then
    return 0
  else
    return 1
  fi
}
