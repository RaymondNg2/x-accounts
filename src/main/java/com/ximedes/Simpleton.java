package com.ximedes;

import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.Status.INSUFFICIENT_FUNDS;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simplistic implementation of the challenge systems.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Simpleton implements API {
    // 300.000 consumer accounts, 300 merchant accounts and 1 back account
    private static final int MAX_ACCOUNTS = 300301;

    // Operations on 'overdrafts' and 'nextAccount' are synchronised on the
    // 'overdrafts' object monitor.
    private final AtomicInteger nextAccount = new AtomicInteger(0);

    // XXX only the bank has overdraft --> make special case

    // XXX very special case for bank account -> only count transfers, no
    // locking for draft, log events instead

    private final int[] balance = new int[MAX_ACCOUNTS];
    private int nextTransaction = 0;

    /**
     * @see com.ximedes.API#createAccount(int)
     */
    @Override
    public int createAccount(final int overdraft) {
        if (nextAccount.get() == 0) {
            if (overdraft < 1) {
                throw new IllegalArgumentException(
                        "expected a large overdraft for bank account, found "
                                + nextAccount.get());
            }
            balance[0] = overdraft;
        } else if (overdraft != 0) {
            throw new IllegalArgumentException(
                    "expected overdraft to be 0, found " + overdraft);
        }
        if (nextAccount.get() >= MAX_ACCOUNTS) {
            throw new IllegalStateException("creating more than MAX_ACCOUNTS");
        }

        return nextAccount.getAndIncrement();
    }

    /**
     * @see com.ximedes.API#transfer(int, int, int)
     */
    @Override
    public Transaction transfer(final int from, final int to, final int amount) {
        synchronized (balance) {
            final Status status;
            if (balance[from] < amount) {
                status = INSUFFICIENT_FUNDS;
            } else {
                // XXX where's the transaction
                balance[from] -= amount;
                balance[to] += amount;
                status = CONFIRMED;
            }
            return new Transaction(nextTransaction++, from, to, amount, status);
        }
    }

    /**
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        int centsInTheSystem = 0;

        for (int i = 0; i < balance.length; i++) {
            centsInTheSystem += balance[i];
        }

        return centsInTheSystem;
    }
}
