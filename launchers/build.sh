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

mkdir -p "$LIB_TARGET_DIR"
mkdir -p "$ITW_TARGET_DIR"
mkdir -p "$ETC_TARGET_DIR"
mkdir -p "$BIN_TARGET_DIR"
mkdir -p "$ICO_TARGET_DIR"
mkdir -p "$SPLASH_TARGET_DIR"

function getTarget() {
 echo "$2/`basename \"$1\"`"
}

function publishInternalLib() {
 cp $1 `getTarget $1 $2`
 RESOURCES_SRC_TO_DEST["$1"]="`getTarget $1 $2`"
}

function publishExternalLib() {
if [ "x$ITW_LIBS" = "xDISTRIBUTION" ] ; then
  RESOURCES_SRC_TO_DEST["$1"]="$1"
else
   publishInternalLib "$1" "$LIB_TARGET_DIR"
fi
}

publishExternalLib "$RHINO_SRC"
publishExternalLib "$TAGSOUP_SRC"
publishExternalLib "$SLFAPI_SRC"
publishExternalLib "$SLFSIMPLE_SRC"
publishExternalLib "$MSLINKS_SRC"
publishInternalLib "$CORE_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$COMMON_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$JNLPAPI_SRC" "$ITW_TARGET_DIR"
publishInternalLib "$XMLPARSER_SRC" "$ITW_TARGET_DIR"

publishInternalLib "$SPLASH_PNG_SRC" "$SPLASH_TARGET_DIR"
publishInternalLib "$JAVAWS_ICO_SRC" "$ICO_TARGET_DIR"
publishInternalLib "$MODULARJDK_ARGS_FILE_SRC" "$ETC_TARGET_DIR"


function build() {
  TYPE=$1 # sh+bats x rust+rust-coverage
  PROGRAM_NAME=$2 # only used to name the in file
  export MAIN_CLASS=$3
  export TAGSOUP_JAR=${RESOURCES_SRC_TO_DEST["$TAGSOUP_SRC"]}
  export RHINO_JAR=${RESOURCES_SRC_TO_DEST["$RHINO_SRC"]}
  export SLFAPI_JAR=${RESOURCES_SRC_TO_DEST["$SLFAPI_SRC"]}
  export SLFSIMPLE_JAR=${RESOURCES_SRC_TO_DEST["$SLFSIMPLE_SRC"]}
  export MSLINKS_JAR=${RESOURCES_SRC_TO_DEST["$MSLINKS_SRC"]}
  export JRE
  export ITW_LIBS
  export CORE_JAR=${RESOURCES_SRC_TO_DEST["$CORE_SRC"]}
  export COMMON_JAR=${RESOURCES_SRC_TO_DEST["$COMMON_SRC"]}
  export JNLPAPI_JAR=${RESOURCES_SRC_TO_DEST["$JNLPAPI_SRC"]}
  export XMLPARSER_JAR=${RESOURCES_SRC_TO_DEST["$XMLPARSER_SRC"]}
  export SPLASH_PNG=${RESOURCES_SRC_TO_DEST["$SPLASH_PNG_SRC"]}
  export MODULARJDK_ARGS_LOCATION=${RESOURCES_SRC_TO_DEST["$MODULARJDK_ARGS_FILE_SRC"]}
  BUILD_DIR=$TARGET/launcher.in.$PROGRAM_NAME
  if [ "x$TYPE" = "xrust" ]; then
    cp -r $SCRIPT_DIR/rust-launcher $BUILD_DIR
    pushd $BUILD_DIR
      if [ "x$PROGRAM_NAME" == "xcoverage" ] ; then
        cargo test  --no-run
        rm -fv $BUILD_DIR/target/debug/launcher-*.d ;
        $KCOV $BUILD_DIR $BUILD_DIR/target/debug/launcher-*
      else
        #on linux patch  out deps?
        RUST_BACKTRACE=1 cargo test
        #some windows?
        #[target.x86_64-pc-windows-msvc]
        #rustflags = ["-C", "target-feature=+crt-static"]
        cargo build --release
        cp -v $BUILD_DIR/target/release/launcher $BIN_TARGET_DIR/$PROGRAM_NAME ; \
      fi
    popd
  elif [ "x$TYPE" = "xsh" ]; then
    ls -l $SCRIPT_DIR/shell-launcher
  else
    echo "invlaid build type: $TYPE"
    exit 2
  fi
}

mkdir $TARGET_TMP
export ITW_TMP_REPLACEMENT=$TARGET_TMP
build rust javaws         net.sourceforge.jnlp.runtime.Boot
build rust itweb-settings net.sourceforge.jnlp.controlpanel.CommandLine
build rust policyeditor   net.sourceforge.jnlp.security.policyeditor.PolicyEditor
if [ ! "$KCOV" = "none" ] ; then 
  build rust coverage   net.sourceforge.jnlp.security.policyeditor.PolicyEditor
fi

build sh javaws         net.sourceforge.jnlp.runtime.Boot
build sh itweb-settings net.sourceforge.jnlp.controlpanel.CommandLine
build sh policyeditor   net.sourceforge.jnlp.security.policyeditor.PolicyEditor


