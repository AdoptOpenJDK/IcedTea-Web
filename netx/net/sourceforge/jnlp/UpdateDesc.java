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
