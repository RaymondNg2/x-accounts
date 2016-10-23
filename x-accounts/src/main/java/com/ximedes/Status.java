package com.ximedes;

/**
 * Transaction statuses.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public enum Status {
    /**
     * The transfer is in a pending state: entered into the system, but we don't
     * know whether it will be committed or rolled back. This is not a final
     * state.
     */
    PENDING,

    /**
     * The transfer was committed and the funds transferred. This is a final
     * state.
     */
    CONFIRMED,

    /**
     * The transfer was rolled back and funds were not transferred and never
     * will be. This is a final state.
     */
    INSUFFICIENT_FUNDS,

    /**
     * An account on the transfer could not be found. Funds were not transferred
     * and never will be. This is a final state.
     */
    ACCOUNT_NOT_FOUND
}
