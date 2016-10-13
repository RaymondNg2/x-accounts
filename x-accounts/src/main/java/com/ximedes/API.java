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
     * @param from
     *            the account to transfer from.
     * @param to
     *            The account to transfer to.
     * @param amount
     *            The amount to transfer.
     * @return The resulting transaction.
     */
    Transaction transfer(int from, int to, int amount);

    /**
     * Count the sum of all cents in the system. This call runs unsynchronised,
     * so make sure the system is quiet before calling it.
     * 
     * @return The sum of all cents in the system.
     */
    int countCentsInTheSystem();
}
