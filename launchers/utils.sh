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

function build() {
  TYPE=$1 # sh+bats x rust+rust-coverage
  PROGRAM_NAME=$2 # only used to name the in file
  export MAIN_CLASS=$3
  export TAGSOUP_JAR=${RESOURCES_SRC_TO_DEST["$TAGSOUP_SRC"]}
  export RHINO_JAR=${RESOURCES_SRC_TO_DEST["$RHINO_SRC"]}
  export MSLINKS_JAR=${RESOURCES_SRC_TO_DEST["$MSLINKS_SRC"]}
  export JRE
  export ITW_LIBS
  export CORE_JAR=${RESOURCES_SRC_TO_DEST["$CORE_SRC"]}
  export COMMON_JAR=${RESOURCES_SRC_TO_DEST["$COMMON_SRC"]}
  export JNLPAPI_JAR=${RESOURCES_SRC_TO_DEST["$JNLPAPI_SRC"]}
  export XMLPARSER_JAR=${RESOURCES_SRC_TO_DEST["$XMLPARSER_SRC"]}
  export CLIENTS_JAR=${RESOURCES_SRC_TO_DEST["$CLIENTS_SRC"]}
  export JNLPSERVER_JAR=${RESOURCES_SRC_TO_DEST["$JNLPSERVER_SRC"]}
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
    for x in `find $SCRIPT_DIR/shell-launcher -type f ` ; do
      nwname=`basename $x | sed "s/launchers/$PROGRAM_NAME/" | sed "s/\\.in//"`
      cat $x | sed \
        -e "s|[@]TAGSOUP_JAR[@]|$TAGSOUP_JAR|g" \
        -e "s|[@]RHINO_JAR[@]|$RHINO_JAR|g" \
        -e "s|[@]MSLINKS_JAR[@]|$MSLINKS_JAR|g" \
        -e "s|[@]TAGSOUP_JAR[@]|$TAGSOUP_JAR|g" \
        -e "s|[@]CORE_JAR[@]|$CORE_JAR|g" \
        -e "s|[@]COMMON_JAR[@]|$COMMON_JAR|g" \
        -e "s|[@]JNLPAPI_JAR[@]|$JNLPAPI_JAR|g" \
        -e "s|[@]XMLPARSER_JAR[@]|$XMLPARSER_JAR|g" \
        -e "s|[@]CLIENTS_JAR[@]|$CLIENTS_JAR|g" \
        -e "s|[@]JNLPSERVER_JAR[@]|$JNLPSERVER_JAR|g" \
        -e "s|[@]SPLASH_PNG[@]|$SPLASH_PNG|g" \
        -e "s|[@]MODULARJDK_ARGS_LOCATION[@]|$MODULARJDK_ARGS_LOCATION|g" \
        -e "s|[@]JRE[@]|$JRE|g" \
        -e "s|[@]MAIN_CLASS[@]|$MAIN_CLASS|g" \
        -e "s|[@]ITW_LIBS[@]|$ITW_LIBS|g" \
      >  $BIN_TARGET_DIR/$nwname ;
      chmod 755 $BIN_TARGET_DIR/$nwname
    done
  else
    echo "invlaid build type: $TYPE"
    exit 2
  fi
}

function image() {
  local img_name=icedtea-web-$VERSION-$1
  mkdir $TARGET_IMAGES/icedtea-web
  cp -r $LIB_TARGET_DIR  $BIN_TARGET_DIR $TARGET_IMAGES/icedtea-web
  cp -r $TARGET_DOCS_PARENT  $TARGET_IMAGES/icedtea-web/
  pushd $TARGET_IMAGES/icedtea-web
    rm -rvf $EXCLUDE
  popd
  pushd $TARGET_IMAGES
    zip -r $img_name.zip icedtea-web
  popd
  mv $TARGET_IMAGES/icedtea-web $TARGET_IMAGES/$img_name
}


function htmlIndex() {
  local htmlIndexFile="$TARGET_DOCS/html/index.html"
  echo "<html><head><title>$VERSION</title></head>" > "$htmlIndexFile"
  echo "<body><h3>$VERSION docs:</h3>"  >> "$htmlIndexFile"
  for LANG_ID in "$@" ; do 
    local ID=`echo "$LANG_ID" | head -c 2`
    echo "<li><a href='$ID/icedtea-web.html'>$LANG_ID</a></li>"  >> "$htmlIndexFile"
  done
  echo "</body></html>"  >> $htmlIndexFile
}

function createCp() {
 local CP=""
 if isWindows; then
    local CP_DEL=";"
  else
    local CP_DEL=":"
  fi
 for CP_ELEMENT in "$@" ; do
   CP="$CP$CP_ELEMENT$CP_DEL"
 done
 echo -n $CP
}

function docs() {
  local type=$1 # html plain man
  local LANG_ID=$2
  local docDir=$TARGET_DOCS/$type
  mkdir -p $docDir
  local LANG_BACKUP=$LANG
  local ID=`echo "$LANG_ID" | head -c 2`
  local ENCOD=`echo "$LANG_ID" | tail -c 6 -`
  export LANG=$LANG_ID
  local langDir="$docDir/$ID"
  if [ $type == "plain" ] ; then
    WIDTH=160
  else
    unset WIDTH
  fi
  if [ $type == "man" ] ; then 
      local langDir="$docDir/$ID/man1"
    if [ $ID == "en" ] ; then
      local langDir="$docDir/man1"
    fi
    ENPARAM=$ENCOD
  else
   unset ENPARAM
  fi
  mkdir -p $langDir
  $JRE/bin/java \
    -cp `createCp $CORE_SRC $COMMON_SRC $JNLPAPI_SRC` \
    net.sourceforge.jnlp.util.docprovider.TextsProvider \
    $type $ENPARAM $langDir $WIDTH false $VERSION "-authorString=https://github.com/AdoptOpenJDK/icedtea-web/graphs/contributors"
  # TODO, genere on fly resource
  # $TP_COMMAND htmlIntro "$(NETX_DIR)/net/sourceforge/jnlp/resources/about_$ID.html" $TP_TAIL; \
  export LANG=$LANG_BACKUP ; \
}

