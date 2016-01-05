#!/bin/bash

# html-gen.sh
#   Copyright (C) 2013  Red Hat
#
# This file is part of IcedTea.
#
# IcedTea is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2, or (at your option)
# any later version.
#
# IcedTea is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with IcedTea; see the file COPYING.  If not, write to the
# Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301 USA.
#
# Linking this library statically or dynamically with other modules is
# making a combined work based on this library.  Thus, the terms and
# conditions of the GNU General Public License cover the whole
# combination.
#
# As a special exception, the copyright holders of this library give you
# permission to link this library with independent modules to produce an
# executable, regardless of the license terms of these independent
# modules, and to copy and distribute the resulting executable under
# terms of your choice, provided that you also meet, for each linked
# independent module, the terms and conditions of the license of that
# module.  An independent module is a module which is not derived from
# or based on this library.  If you modify this library, you may extend
# this exception to your version of the library, but you are not
# obligated to do so.  If you do not wish to do so, delete this
# exception statement from your version.

################################################################################

# This script is used by the stamps/html-gen target in Makefile.am. Its purpose
# is to produce HTML-escaped and formatted documents from a set of plaintext
# documents, namely AUTHORS, NEWS, ChangeLog, and COPYING, located in the
# same directory as this script. These generated HTML documents are then used
# in the netx About Dialog, which can be invoked with "javaws -about".

# The only configuration option is the number of Changesets, and the files processed
# are hardcoded. To run the script manually, create a directory "html-gen" in the
# same directory as this script, containing files named AUTHORS, NEWS, ChangeLog,
# and COPYING. Note that these files WILL be modified in-place during the HTML
# "conversion" process. Setting the environment variable "HTML_GEN_DEBUG" to "true"
# will enable some output from the script, which may be useful if you encounter
# issues with this script's processing of an input file.
# The number of Changesets to process into the ChangeLog can be set by setting the
# environment variable HTML_GEN_CHANGESETS, or by passing an integer argument to
# the script. The parameter will take priority over the environment variable.

print_debug() {
    if [ "$HTML_GEN_DEBUG" ]; then echo "$1"; fi
}

CHANGESETS="$1"

if [ -z "$CHANGESETS" ]; then CHANGESETS="$HTML_GEN_CHANGESETS"; fi

if [ -z "$CHANGESETS" ] || [ "$CHANGESETS" -lt 0 ]; then CHANGESETS=10; fi

NEWS_ITEMS=2

if [ -d .hg ]; then
    REPO_URL="$(hg paths default | sed -r 's/.*icedtea.classpath.org\/(.*)/\1/')"
else
    unset REPO_URL
fi

start_time="$(date +%s.%N)"

if [ ! -e html-gen ]; then
	echo "No html-gen directory, exiting. See Makefile.am for usage"
	exit 1
fi

cd html-gen

print_debug "Generating HTML content for javaws -about${REPO_URL:+ for }$REPO_URL. $CHANGESETS changesets, $NEWS_ITEMS news items"
print_debug "Starting sed substitutions"
for FILE in NEWS AUTHORS COPYING ChangeLog
do
    print_debug "Processing $FILE..."
    sed -i -r 's/\t/    /g' "./$FILE" # Convert tabs into four spaces
    sed -i -r 's/\&/\&amp;/g' "./$FILE" # "&" -> "&amp;"
    sed -i -r 's/  /\&ensp;\&ensp;/g' "./$FILE" # Double-spaces into HTML whitespace for format preservation
    sed -i -r 's/</\&lt;/g' "./$FILE" # "<" -> "&lt;"
    sed -i -r 's/>/\&gt;/g' "./$FILE" # ">" -> "&gt;"
    sed -i -r 's_(\&lt;)?(https?://[^ ]*)(\&gt;| |$)_\1<a href="\2">\2</a>\3_i' "./$FILE" # Create hyperlinks from http(s) URLs
    sed -i -r 's/\&lt;(.*@.*)\&gt;/\&lt;<a href="mailto:\1\?subject=IcedTea-Web">\1<\/a>\&gt;/i' "./$FILE" # Create mailto links from email addresses formatted as <email@example.com>
    sed -i -r 's/$/<br>/' "./$FILE" # "\n" -> "<br>"

    mv "$FILE" "$FILE.html"
    print_debug "$FILE.html finished."
done

print_debug "Done sed subs. Starting in-place additions"

# Centre the column of author names in the Authors file
sed -i '4i <center>' AUTHORS.html
# Insert jamIcon above author names
sed -i '5i <br><img src="jamIcon.jpg" alt="Jam Icon" width="87" height="84"><br><br>' AUTHORS.html
echo "</center>" >> AUTHORS.html

if [ -n "${REPO_URL}" ]; then
    REVS=(`hg log -l"$CHANGESETS" | grep 'changeset:' | cut -d: -f3 | tr '\n' ' '`)
fi

print_debug "Done. Starting formatting (bolding, mailto and hyperlink creation)"

for FILE in NEWS.html ChangeLog.html
do
    print_debug "Processing $FILE..."
    mv "$FILE" "$FILE.old"
    COUNTER=0
    while read LINE
    do
        BOLD=1
        if [ "$FILE" = "NEWS.html" ]
        then
            if [[ "$LINE" =~ New\ in\ release* ]]
            then
                BOLD=0
                COUNTER="$(( COUNTER + 1 ))"
            fi
            if [ "$COUNTER" -gt "$NEWS_ITEMS" ] # Cut to two releases
            then
                break
            fi
        else
            email_regex=".*\&lt;.*\@.*\&gt;"
            if [[ "$LINE" =~ $email_regex ]] # Matches eg <aazores@redhat.com>, after HTML-escaping
            then
                BOLD=0
            fi
            date_regex=[0-9]{4}-[0-9]{2}-[0-9]{2}
            if [[ "$LINE" =~ $date_regex* ]] # Matches line starting with eg 2013-07-01
            then
                html_space="\&ensp;\&ensp;"
                if [ -n "${REPO_URL}" ]; then
                    REV="${REVS["$COUNTER"]}"
                    # Turn the date into a hyperlink for the revision this changelog entry describes
                    LINE=$(echo "$LINE" | sed -r "s|($date_regex)($html_space.*$html_space.*)|<a href=http://icedtea.classpath.org/$REPO_URL/rev/$REV>\1</a>\2|")
                fi
                COUNTER="$(( COUNTER + 1 ))"
            fi
            if [ "$COUNTER" -gt "$CHANGESETS" ] # Cut to ten changesets
            then
                break
            fi
        fi
        if [ "$BOLD" -eq 0 ] # Highlight "New In Release" in News, and author name lines in ChangeLog
        then
            LINE="<b>$LINE</b>"
        fi
        echo "$LINE" >> "$FILE"
    done < "$FILE.old"
    rm "$FILE.old"
    print_debug "$FILE finished"
done

sed -i -r 's|(\*\ .*):|<u>\1</u>:|' ChangeLog.html # Underline changed files in ChangeLog, eg "* Makefile.am:"

end_time="$(date +%s.%N)"

print_debug "HTML generation complete"
print_debug "Total elapsed time: $(echo "$end_time - $start_time" | bc )"
