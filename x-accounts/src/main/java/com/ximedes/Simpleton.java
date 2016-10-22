package com.ximedes;

import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.Status.INSUFFICIENT_FUNDS;
import static com.ximedes.Status.PENDING;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simplistic implementation of the challenge systems.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Simpleton implements API {
    private final AtomicInteger nextAccount = new AtomicInteger(0);

    // 300.000 consumer accounts, 300 merchant accounts and 1 back account
    private static final int MAX_ACCOUNTS = 300301;
    private final Object[] locks = new Object[MAX_ACCOUNTS];
    private final int[] balance = new int[MAX_ACCOUNTS];

    private int bankOverdraft = -1;

    // It's a bit less than that, but let's not be picky about some RAM.
    private static final int EXPECTED_TRANSFERS = 4000000;
    private final Transaction[] transfers = new Transaction[EXPECTED_TRANSFERS];
    private final AtomicInteger nextTransaction = new AtomicInteger(0);

    /**
     * Create a new backend server, setting up the per-account lock for each
     * expected account. We also pre-allocate all transfer object, so that we
     * don't have to allocate that memory at run-time. Instead, we have that
     * overhead as part of starting up the server.
     */
    public Simpleton() {
        super();

        for (int i = 0; i < MAX_ACCOUNTS; i++) {
            locks[i] = new Object();
        }
        for (int i = 0; i < EXPECTED_TRANSFERS; i++) {
            transfers[i] = new Transaction(i, -1, -1, -1, PENDING);
        }
    }

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
            bankOverdraft = overdraft;
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
     * @see com.ximedes.API#getAccount(int)
     */
    @Override
    public Account getAccount(final int accountId) {
        return new Account(accountId, balance[accountId],
                accountId == 0 ? bankOverdraft : 0);
    }

    /**
     * @see com.ximedes.API#transfer(int, int, int)
     */
    @Override
    public int transfer(final int from, final int to, final int amount) {
        Status status = CONFIRMED;

        // To reduce lock contention we lock the "from" address separately from
        // the "to" address. This saves about 15% of the time in a lock-bound
        // situation.

        // Normally locking elements in random order would lead to a deadlock.
        // At some point one thread might hold element X and try to lock element
        // Y, while another thread holds the lock on element Y and is seeking to
        // lock element X. However, we know that all "from" elements are
        // consumer accounts and all the "to" elements are merchant accounts.
        // This ensures we never get the aforementioned deadlock and can use
        // this typically unsafe idiom safely.

        synchronized (locks[from]) {
            if (balance[from] < amount) {
                status = INSUFFICIENT_FUNDS;
            } else {
                synchronized (locks[to]) {
                    balance[from] -= amount;
                    balance[to] += amount;
                }
            }
        }

        final Transaction transfer = transfers[nextTransaction
                .getAndIncrement()];
        transfer.from = from;
        transfer.to = to;
        transfer.amount = amount;
        transfer.status = status;
        transfers[transfer.transactionId] = transfer;
        return transfer.transactionId;
    }

    /**
     * @see com.ximedes.API#getTransfer(int)
     */
    @Override
    public Transaction getTransfer(final int transferId) {
        return transfers[transferId];
    }

    /**
     * @see com.ximedes.API#ping()
     */
    @Override
    public void ping() {
        // just return quickly...
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
