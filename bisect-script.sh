#!/bin/sh

#set -x
set -e

#script only for master branch currently as none other uses maven
JNLPFILE=$1

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

set +e
  #cat ~/.config/icedtea-web/deployment.properties
set -e

LOGFILEPATH="/dev/null"

function join_by { local IFS="$1"; shift; echo "$*"; }

function testonecommit () {
  COMMIT=$1
  JNLPFILE=$2
  cd "$SCRIPT_DIR" 
  git checkout $COMMIT &>$LOGFILEPATH

  mvn clean install -DskipTests 1>$LOGFILEPATH
  clearCache
  JARS=$(join_by ":" `find $PWD | grep "\.jar$"` $TAGSOUP $SLF4J) 1>$LOGFILEPATH
  #runWithTimeout "echo 'YES
#YES
#YES
#YES
#YES
#YES' | java -Xbootclasspath/a:$JARS net.sourceforge.jnlp.runtime.Boot $JNLPFILE -Xnofork -headless"
  echo "YES 1>$LOGFILEPATH
YES
YES
YES
YES
YES
YES" | java -Xbootclasspath/a:$JARS net.sourceforge.jnlp.runtime.Boot $JNLPFILE -Xnofork -headless
  RES=$?
  clearCache
  mvn clean 1>$LOGFILEPATH
  return $RES
}

function hashToNumber () {
  HASH=$1
  NUMBER=`cd "$SCRIPT_DIR" && git checkout master &>$LOGFILEPATH ; git rev-list HEAD | grep -n $HASH | cut -d : -f 1`
  echo "$NUMBER"
}

function numberToHash () {
  NUMBER=$1
  HASH=`cd "$SCRIPT_DIR" && git checkout master &>$LOGFILEPATH && git rev-list HEAD | head -n $NUMBER | tail -n 1`
  echo "$HASH"
}

function clearCache () {
  #java -Xbootclasspath/a:$JARS net.sourceforge.jnlp.runtime.Boot -Xnofork -XclearCache
  rm -rf ~/.cache/icedtea-web/*
}

# https://stackoverflow.com/questions/687948/timeout-a-command-in-bash-without-unnecessary-delay
# WIP - killing process as soon as it starts, not working currently
function runWithTimeout () {
  COMMAND=$1
  if [ -z $2 ]; then
    TIMEOUT=30
  else
    TIMEOUT=$2
  fi
  ( eval "$COMMAND" ) 2>/dev/null & pid=$!
  ( sleep $TIMEOUT && kill -SIGKILL $pid ) 2>/dev/null & watcher=$!
  RES=`wait $pid`
  if wait $pid 2>/dev/null; then
    RES=0
    pkill -HUP -P $watcher
    wait $watcher
  else
    RES=1
  fi
  return $RES
}


HIGHERHASH=$(numberToHash 1)
LOWERHASH="d8ca907c1b98fabc1d2129046ae704ec0d6416e6"

if [[ -z "$TAGSOUP" || ! -f "$TAGSOUP" ]]
then
  TAGSOUP=`find ~/.m2 /usr/share / 2>$LOGFILEPATH | grep -v "javadoc" | grep "tagsoup" | grep "\.jar$" | head -n 4`
fi

# slf4j-api and slf4j-simple jars are required for some past commits and not provided via mvn install
if [[ -z "$SLF4J" || ! -f "$SLF4J" ]] 
then
	SLF4J="$(find /usr/share ~/.m2 / -type f 2>$LOGFILEPATH | grep -v "javadoc" | grep "slf4j-simple" | grep "\.jar$" | head -n 4 ) $(find /usr/share ~/.m2 / -type f 2>$LOGFILEPATH | grep -v "javadoc" | grep "slf4j-api" | grep "\.jar$" | head -n 4 )"
fi

# translating hashes to numbers
LOWER=$(hashToNumber $LOWERHASH)
HIGHER=$(hashToNumber $HIGHERHASH)

# switch if in wrong order - probably not necessary anymore

if [ $HIGHER -ge $LOWER ] 
then
  TMP=$HIGHER
  HIGHER=$LOWER
  LOWER=$TMP
fi

function main () {
  while [ $(($LOWER - $HIGHER)) -gt 1 ] 
  do
    MID=$(( ($HIGHER+$LOWER)/2 ))
    CURRENT=$(numberToHash $MID)
      set +e
      testonecommit $CURRENT $JNLPFILE &>$LOGFILEPATH
      RES=$?
      set -e
    if [ $RES -eq 0 ] ; then
      LOWER=$MID
      echo "its closer than $MID"
    else
      HIGHER=$MID
      echo "its farther than $MID"
    fi
  done


  CURRENT=`cd "$SCRIPT_DIR" && git rev-list HEAD | head -n 1`
  echo "[DONE]: sought commit should be - $HIGHER with hash: $CURRENT"
}

main
