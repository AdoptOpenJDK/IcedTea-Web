#!/bin/bash
declare -A RESOURCES_SRC_TO_DEST

set -x
set -e
set -o pipefail

## resolve folder of this script, following all symlinks,
## http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done

readonly SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"

source $SCRIPT_DIR/configure.sh
source $SCRIPT_DIR/utils.sh

mkdir -p "$LIB_TARGET_DIR"
mkdir -p "$ITW_TARGET_DIR"
mkdir -p "$ETC_TARGET_DIR"
mkdir -p "$BIN_TARGET_DIR"
mkdir -p "$ICO_TARGET_DIR"
mkdir -p "$SPLASH_TARGET_DIR"

publishExternalLib "$RHINO_SRC"
publishExternalLib "$TAGSOUP_SRC"
publishExternalLib "$MSLINKS_SRC"
publishInternalLib "$CORE_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$COMMON_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$JNLPAPI_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$XMLPARSER_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$CLIENTS_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$JNLPSERVER_SRC" "$ITW_TARGET_DIR"

publishInternalLib "$SPLASH_PNG_SRC" "$SPLASH_TARGET_DIR"
publishInternalLib "$JAVAWS_ICO_SRC" "$ICO_TARGET_DIR"
publishInternalLib "$MODULARJDK_ARGS_FILE_SRC" "$ETC_TARGET_DIR"

for TYPE in man html plain ; do
  for LANG_ID in $LOCALIZATIONS ;  do
    docs $TYPE $LANG_ID
  done
done
htmlIndex $LOCALIZATIONS

build sh javaws         net.sourceforge.jnlp.runtime.Boot
build sh itweb-settings net.adoptopenjdk.icedteaweb.client.commandline.CommandLine
build sh policyeditor   net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor

if which $CARGO_RUST ; then
  mkdir $TARGET_TMP  # for tests output
  export ITW_TMP_REPLACEMENT=$TARGET_TMP # for tests output
  build rust javaws         net.sourceforge.jnlp.runtime.Boot
  build rust itweb-settings net.adoptopenjdk.icedteaweb.client.commandline.CommandLine
  build rust policyeditor   net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor
  if [ ! "$KCOV" = "none" ] ; then 
    build rust coverage   net.sourceforge.jnlp.runtime.Boot
  fi
else
  if [ "$CARGO_RUST" = "none" ] ; then 
    echo "intentionally none cargo. Skipping build of native launchers"
  else
    echo "no cargo; can not build native launchers"
    exit 1
  fi
fi

if [ $ITW_LIBS == "DISTRIBUTION" ] ; then
  echo "not creating images in $ITW_LIBS mode; image is already done, as launchers are built against your system libraries."
  echo "TODO automate below; defualt none? or as in Fedora? Overwritable as all others in configure?"
  echo "If you wish, copy man pages, desktop files and bash completion. Sed as necessary"
  exit 0
fi

mkdir $TARGET_IMAGES
EXCLUDE="bin/javaws bin/itweb-settings bin/policyeditor bin/*.exe"
image portable.bin
if which $CARGO_RUST ; then
  EXCLUDE="bin/*.sh bin/*.bat"
  if isWindows; then
    EXCLUDE="$EXCLUDE icedtea-web-docs/*/man"
    image win.bin
  else
    image linux.bin
  fi
else
  echo "intentionally none cargo. Skipping build of native images. Staying on portable shell image one only"
fi

