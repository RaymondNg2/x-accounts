package com.ximedes;

/**
 * The main API to the accounts challenge systems.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface API {
    /**
     * Create an account with the specified overdraft.
     * 
     * @param overdraft
     *            The maximum overdraft of this account.
     * @return The identifier of the new account.
     */
    int createAccount(int overdraft);

    /**
     * Retrieve an account that was created earlier.
     * 
     * @param accountId
     *            The identifier of the account to look up.
     * @return The account as identified by the identifier, or <code>null</code>
     *         if no account could be found for the specified id.
     */
    Account getAccount(int accountId);

    /**
     * @param from
     *            the account to transfer from.
     * @param to
     *            The account to transfer to.
     * @param amount
     *            The amount to transfer.
     * @return The resulting transfer identifier.
     */
    int transfer(int from, int to, int amount);

    /**
     * Retrieve a transfer that was scheduled earlier.
     * 
     * @param transferId
     *            The identifier of the transfer to look up.
     * @return The transfer as identified by the identifier, or
     *         <code>null</code> if no transfer could be found for that id.
     */
    Transaction getTransfer(int transferId);

    // The methods below are not specified by the challange documentation, but
    // we added them for convenience.

    /**
     * An empty method to measure response times. The server should do nothing
     * but send a response ASAP.
     */
    void ping();

    /**
     * Count the sum of all cents in the system. This call runs unsynchronised,
     * so make sure the system is quiet before calling it.
     * 
     * @return The sum of all cents in the system.
     */
    int countCentsInTheSystem();
}
