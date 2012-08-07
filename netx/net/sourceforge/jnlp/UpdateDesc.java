/* UpdateDesc.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp;

/**
 * Represents an 'update' element in a JNLP file. This element describes when to
 * check for updates and what actions to take if updates are available
 *
 * @see Check
 * @see Policy
 */
public class UpdateDesc {

    /**
     * Describes when/how long to check for updates.
     */
    public enum Check {
        /** Always check for updates before launching the application */
        ALWAYS,

        /**
         * Default. Check for updates until a certain timeout. If the update
         * check is not completed by timeout, launch the cached application and
         * continue updating in the background
         */
        TIMEOUT,

        /** Check for application updates in the background */
        BACKGROUND
    }

    /**
     * Describes what to do when the Runtime knows there is an applicatFion
     * update before the application is launched.
     */
    public enum Policy {
        /**
         * Default. Always download updates without any user prompt and then launch the
         * application
         */
        ALWAYS,

        /**
         * Prompt the user asking whether the user wants to download and run the
         * updated application or run the version in the cache
         */
        PROMPT_UPDATE,

        /**
         * Prompts the user asking to download and run the latest version of the
         * application or abort running
         */
        PROMPT_RUN,
    }

    private Check check;
    private Policy policy;

    public UpdateDesc(Check check, Policy policy) {
        this.check = check;
        this.policy = policy;
    }

    public Check getCheck() {
        return this.check;
    }

    public Policy getPolicy() {
        return this.policy;
    }

}
