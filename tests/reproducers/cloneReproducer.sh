#!/bin/sh
#sh cloneReproducer SandboxSignedSandbox XYZ signed signed
#when reprodcuer is well done, then only "id" is enough to replace in names and files. this scriptis doing it.
#then of course you need to evaluate  all asserst on your own

from=$1
to=$2

familyFrom=$3
familyTo=$4
#^simple,signed,signed2,custom (script will proabbly not work fine for custom)
#run from this dir (tests/reproducers), or use absolute path
top1=.
#top1=/home/jvanek/hg/icedtea-web/tests/reproducers
top2=$top1
#top2=/home/jvanek/Desktop


srcDir=$top1/$familyFrom
destDir=$top2/$familyTo

desc=$destDir/$to
src=$srcDir/$from
drs="resources srcs testcases"

tree  $src

mkdir -v $desc
for x in $drs ; do
	mkdir -v $desc/$x
done

tree  $desc
cp -vr $src/srcs/META-INF  $desc/srcs/
SED1="sed s/$from/$to/g"
SED2="sed s/a/a/"
if [ \( $familyFrom = signed -o $familyFrom = signed2 \) -a $familyTo = simple ] ; then
SED2='sed s/bgcolor=\"red\"/bgcolor=\"blue\"/g'
fi;
if [ \( $familyTo = signed -o $familyTo = signed2 \) -a $familyFrom = simple ] ; then
SED2='sed s/bgcolor=\"blue\"/bgcolor=\"red\"/g'
fi;

for x in $drs ; do
	files=`ls $src/$x/`
	for f in $files ; do 
		target=`echo $f | $SED1`
		cat $src/$x/$f | $SED1  | sed "s/@Test/@Test FIXME!/g" | $SED2 >  $desc/$x/$target
	done
done



ls -lR $desc
