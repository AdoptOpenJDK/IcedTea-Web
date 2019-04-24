/* package-info.java
   Copyright (C) 2015 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.*/
/**
 *package generally about showing various security prompts
 *<h3>Following diagram shows how dialog is handled when some application/applet needs to show it</h3>
 *
 *<pre>
{@code
        ITW-thread(s)               | dialogs-thread                                                                                                                                  X
                                    |                                                                                                                                                 X
   presteps in SecurityDialogs      |                                                                                                                                                 X
      # eg handle trustall/none     |                                                                                                                                                 X
            |                       |                                                                                                                                                 X
    prepare message                 |                                                                                                                                                 X
               # set JNLPfile       |                                                                                                                                                 X
               # set type of dialog |                                                                                                                                                 X
                   see              |                                                                                                                                                 X
               # extrass...         |                                                                                                                                                 X
               # lock               |                                                                                                                                                 X
            |                       |                                                                                                                                                 X
    post message to queue           |                                                                                                                                                 X
            | >------------------------------------> | <----------------------------------------------------------------------------------------------------------------------------| X
    wait for result from            |            read message from queue                                                                                                            | X
          getUserResponse(lock lock)|                |                                                                                                                              | X
                                    |            create instance of dialogue                                                                                                        | X
                                    |                |                                                                                                                              | X
                                    |            according to type of dialogue, create and place panel  (this is important, panel is keeper of rememberable decision)               | X
                                    |                |                                                                                                                              | X
                                    |            if panel is instance of RememberableDialog                                                                                         | X
                                    |              else                        then                                                                                                 | X
                                    |                |                          |                                                                                                   | X
                                    |                |                         check whether this applet+action was already stored and permanently remembered in .appletSecurity    | X
                                    |                |                          no                                                                        yes                       | X
                                    |                |<-------------------------|                                                                          |                        | X
                                    |                |                                                                                                     |                        | X
                                    |                |                                                                                                     |                        | X
                                    |                |                                                                                                     |                        | X
                                    |                |                                                                                                     |                        | X
                                    |            add closing and disposing listener(s) to button(s)                                                        |                        | X
                                    |                |     * set return value to listener?                                                                 |                        | X
                                    |                |                                                                                                     |                        | X
                                    |                      according to set value, set default selected button?                                            |                        | X
                                    |                |                                                                                                     |                        | X
                                    |            if his applet+action was already stored in .appletSecurity include text approved/denied and when          |                        | X
                                    |                |                                                                                                     |                        | X
                                    |            wait for user to click button or close dialogue                                                          |                        | X
                                    |                |                                                                                                     |                        | X
                                    |                --> set selected value (via listener?) to message, dispose dialog -> <- set stored value to message <-|                        | X
                                    |                                                                                    |                                                          | X
                                    |                    if panel is instance of RememberableDialogue crate new, update old(date/decision,jars...) record in .appletSecurity        | X
                                    |                                                                                    |                                                          | X
            | <------------------------------------------------------------------------------------------< unlock lock of this message  >--------------------------------------------| X
  read result from message          |                                                                                                                                                 X
            |                       |                                                                                                                                                 X
   continue accordingly             |                                                                                                                                                 X
                                                                                                                                                                                      X
}
 *</pre>
 *
 *<h3>How to make your dialog to be remembered</h3>
 *<ul>
 *<li>make your extension of SecurityPanel implementing RememberableDialog: </li>
 *</ul>
 *<blockquote>
 *<ul>
 * <li>  RememberPanelResult getRememberAction - if your dialogue uses RememberPanel, then you get RememberPanelResult for free</li>
 * <li>  DialogResult getValue() - what your dialogue actually returns. If it is some simple Yes, No.. Then you can use existing types in dialogresults package. If it handles something more complex, you can inspire yourself in AccessWarningPaneComplexReturn </li>
 * <li>  JNLPFile getFile() - ok, file keeps all needed to identify applet/app, so it is a must.</li>
 * <li>  DialogResult readValue(String s) - the dialog must be able to read answer from String, which is supplied to it via engine. If you use some PrimitivesSubset extension, then it is mostly only static call its factory creator from String. </li>
 *</ul>
 *</blockquote>
 *This should be all. The value your type writeValue to file, is then stored under Key, which is your extension of SecurityPanel implementing RememberableDialog name
 *
 *
 */
package net.sourceforge.jnlp.security;
