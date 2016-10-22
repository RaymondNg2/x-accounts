package com.ximedes;

/**
 * The representation of an account.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Account {
    public Account(final int accountId, final int balance,
            final int overdraft) {
        super();

        this.accountId = accountId;
        this.balance = balance;
        this.overdraft = overdraft;
    }

    public final int accountId;
    public final int balance;
    public final int overdraft;
}
