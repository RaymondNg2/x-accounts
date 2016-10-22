package com.ximedes.client;

import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.Status.INSUFFICIENT_FUNDS;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.ximedes.API;
import com.ximedes.Transaction;

/**
 * The big test scenario that is described on
 * https://www.ximedes.com/testing-the-sva-challange-application/
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ClientMain {
    // for local, functional testing
    // private final API api = new Simpleton();

    private static final API api = new HttpApiClient("http://127.0.0.1:8080/",
            110, 110);

    private static final Object transferCountLock = new Object();
    private static int transferCount = 0;
    private static int highestTransferId = -1;

    private static void register(final Transaction transaction) {
        synchronized (transferCountLock) {
            transferCount++;
            highestTransferId = max(highestTransferId,
                    transaction.transactionId);
        }
    }

    public static void main(final String[] args) throws Exception {
        final long start = currentTimeMillis();

        // 1 instance, 300.000 consumer accounts, 10 cents each
        final int centsInTheSystem = 300 * 1000 * 10;
        final int bankAccount = createOneBankAccount(centsInTheSystem);

        // 1 * 10 * 10 * 3 = 300 instances
        final List<Integer> merchantAccounts = synchronizedList(
                new ArrayList<Integer>(300));

        // 1000 * 10 * 10 * 3 = 300.000 instances
        final List<Integer> consumerAccounts = synchronizedList(
                new ArrayList<Integer>(300000));

        final Thread[] machines = new Thread[10];
        // 10 machines each run ...
        for (int machine = 0; machine < 10; machine++) {
            machines[machine] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        machineRun(bankAccount, merchantAccounts,
                                consumerAccounts);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        exit(1);
                    }
                }
            });
            machines[machine].start();
        }
        for (int machine = 0; machine < 10; machine++) {
            machines[machine].join();
        }

        // after all is done...
        assertEquals(300, merchantAccounts.size());
        assertEquals(300000, consumerAccounts.size());
        assertEquals(300000 + 3600000, transferCount);
        assertEquals(300000 + 3600000 - 1, highestTransferId);

        final long elapsedSeconds = MILLISECONDS
                .toSeconds(currentTimeMillis() - start);
        final long transactionsPerSecond = transferCount / elapsedSeconds;
        out.println("processed " + transferCount + " in " + elapsedSeconds
                + " seconds: " + transactionsPerSecond
                + " transactions per second.");

        final int finalCentsInTheSystem = api.countCentsInTheSystem();
        assertEquals(centsInTheSystem, finalCentsInTheSystem);
    }

    private static void machineRun(final int bankAccount,
            final List<Integer> merchantAccounts,
            final List<Integer> consumerAccounts) throws InterruptedException {
        // 10 threads that each run 3x ...
        final Thread[] threads = new Thread[10];
        for (int thread = 0; thread < 10; thread++) {
            threads[thread] = new Thread(new Runnable() {
                @Override
                public void run() {
                    threadRun(bankAccount, merchantAccounts, consumerAccounts);
                }
            });
            threads[thread].start();
        }
        for (int thread = 0; thread < 10; thread++) {
            threads[thread].join();
        }
    }

    private static void threadRun(final int bankAccount,
            final List<Integer> merchantAccounts,
            final List<Integer> consumerAccounts) {
        for (int iteration = 0; iteration < 3; iteration++) {

            // XXX wrong level, needs to be up one level
            final int newMerchantAccount = createOneMerchantAccount();
            final Collection<Integer> newConsumerAccounts = create1000ConsumerAccounts();
            seedConsumerAccountsFromBank(bankAccount, newConsumerAccounts);

            // put the newcomers into the pool
            merchantAccounts.add(newMerchantAccount);
            consumerAccounts.addAll(newConsumerAccounts);

            transferFromConsumersToMerchants(consumerAccounts,
                    merchantAccounts);

            out.println("merchants: " + merchantAccounts.size()
                    + ", consumers: " + consumerAccounts.size()
                    + ", transactions: " + transferCount);
        }
    }

    private static int createOneBankAccount(final int centsInTheSystem) {
        final int bankAccount = api.createAccount(centsInTheSystem);
        assertEquals(0, bankAccount);

        return bankAccount;
    }

    private static int createOneMerchantAccount() {
        final int merchantAccountId = api.createAccount(0);
        assertNotEquals(0, merchantAccountId);
        return merchantAccountId;
    }

    private static Collection<Integer> create1000ConsumerAccounts() {
        final Collection<Integer> consumerAccounts = new ArrayList<Integer>();
        for (int i = 0; i < 1000; i++) {
            final int consumerAccountId = api.createAccount(0);
            assertNotEquals(0, consumerAccountId);
            consumerAccounts.add(consumerAccountId);
        }

        return consumerAccounts;
    }

    private static void seedConsumerAccountsFromBank(final int bankAccount,
            final Collection<Integer> consumerAccounts) {
        for (Integer consumerAccount : consumerAccounts) {
            final int transferId = api.transfer(bankAccount, consumerAccount,
                    10);
            final Transaction transaction = api.getTransfer(transferId);

            assertEquals(bankAccount, transaction.from);
            assertEquals(consumerAccount.intValue(), transaction.to);
            assertEquals(10, transaction.amount);
            assertTrue(CONFIRMED == transaction.status
                    || INSUFFICIENT_FUNDS == transaction.status);

            register(transaction);
        }
    }

    private static void transferFromConsumersToMerchants(
            final List<Integer> consumerAccounts,
            final List<Integer> merchantAccounts) {
        final Random random = new Random();

        for (int i = 0; i < 12000; i++) {
            final int consumerAccount = consumerAccounts
                    .get(random.nextInt(consumerAccounts.size()));
            final int merchantAccount = merchantAccounts
                    .get(random.nextInt(merchantAccounts.size()));
            final int transferId = api.transfer(consumerAccount,
                    merchantAccount, 1);
            final Transaction transaction = api.getTransfer(transferId);

            assertNotEquals(0, transaction.transactionId);
            assertEquals(consumerAccount, transaction.from);
            assertEquals(merchantAccount, transaction.to);
            assertEquals(1, transaction.amount);

            register(transaction);
        }
    }
}
