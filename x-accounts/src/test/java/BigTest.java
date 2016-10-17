import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.Status.INSUFFICIENT_FUNDS;
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
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.ximedes.API;
import com.ximedes.Transaction;
import com.ximedes.http.HttpUrlConnectionApiClient;

// XXX test that calls return within 25ms (ON AMAZON)

/**
 * The big test scenario that is described on
 * https://www.ximedes.com/testing-the-sva-challange-application/
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class BigTest {
    // for local, functional testing
    // private final API api = new Simpleton();

    // testing over HTTP using the Uni-rest client
    // private final API api = new HttpApiClient();

    // testing over HTTP using the bare Java HTTPUrlConnection client
    private final API api = new HttpUrlConnectionApiClient();

    /**
     * A test case.
     * 
     * @throws Exception
     *             When the test failed.
     */
    @Test
    public void big() throws Exception {
        // Some system properties are useful to tweak the network stack. This is
        // relevant for the HTTPUrlConnection class, but does not hurt for any
        // of the other code.
        //
        // The only relevant tweaking I found make a different is
        // http.maxConnections. That reduces the number of actual network
        // connections being created and helps avoid spurious
        // "java.net.SocketException: Invalid argument" messages.
        //
        // Best start the JVM with:
        // -Djava.net.preferIPv4Stack=true
        // -Dhttp.keepalive=true
        // -Dhttp.maxConnections=110
        final Map<Object, Object> properties = System.getProperties();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String key = (String) entry.getKey();
            if (key.startsWith("http.") || key.startsWith("java.net.")) {
                out.println(entry.getKey() + "=" + entry.getValue());
            }
        }
        out.println();

        // @SuppressWarnings("unused")
        // final HttpApiServer httpApiServer = new HttpApiServer();

        final long start = currentTimeMillis();

        // 1 instance, 300.000 consumer accounts, 10 cents each
        final int centsInTheSystem = 300 * 1000 * 10;
        final int bankAccount = createOneBankAccount(centsInTheSystem);

        // 1 * 10 * 10 * 3 = 300 instances
        final List<Integer> merchantAccounts = synchronizedList(
                new ArrayList<Integer>());

        // 1000 * 10 * 10 * 3 = 300.000 instances
        final List<Integer> consumerAccounts = synchronizedList(
                new ArrayList<Integer>());

        final List<Transaction> transactions = synchronizedList(
                new ArrayList<Transaction>());

        final Thread[] machines = new Thread[10];
        // 10 machines each run ...
        for (int machine = 0; machine < 10; machine++) {
            machines[machine] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        machineRun(bankAccount, merchantAccounts,
                                consumerAccounts, transactions);
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
        assertEquals(300000 + 3600000, transactions.size());
        assertEquals(300000 + 3600000 - 1,
                transactions.get(transactions.size() - 1).transactionId);

        final long elapsedSeconds = MILLISECONDS
                .toSeconds(currentTimeMillis() - start);
        final long transactionsPerSecond = transactions.size() / elapsedSeconds;
        out.println("processed " + transactions.size() + " in " + elapsedSeconds
                + " seconds: " + transactionsPerSecond
                + " transactions per second.");

        final int finalCentsInTheSystem = api.countCentsInTheSystem();
        assertEquals(centsInTheSystem, finalCentsInTheSystem);
    }

    private void machineRun(final int bankAccount,
            final List<Integer> merchantAccounts,
            final List<Integer> consumerAccounts,
            final List<Transaction> transactions) throws InterruptedException {
        // 10 threads that each run 3x ...
        final Thread[] threads = new Thread[10];
        for (int thread = 0; thread < 10; thread++) {
            threads[thread] = new Thread(new Runnable() {
                @Override
                public void run() {
                    threadRun(bankAccount, merchantAccounts, consumerAccounts,
                            transactions);
                }
            });
            threads[thread].start();
        }
        for (int thread = 0; thread < 10; thread++) {
            threads[thread].join();
        }

    }

    private void threadRun(final int bankAccount,
            final List<Integer> merchantAccounts,
            final List<Integer> consumerAccounts,
            final List<Transaction> transactions) {
        for (int iteration = 0; iteration < 3; iteration++) {

            // XXX wrong level, needs to be up one level
            final int newMerchantAccount = createOneMerchantAccount();
            final Collection<Integer> newConsumerAccounts = create1000ConsumerAccounts();
            seedConsumerAccountsFromBank(bankAccount, newConsumerAccounts,
                    transactions);

            // put the newcomers into the pool
            merchantAccounts.add(newMerchantAccount);
            consumerAccounts.addAll(newConsumerAccounts);

            transferFromConsumersToMerchants(consumerAccounts, merchantAccounts,
                    transactions);

            out.println("merchants: " + merchantAccounts.size()
                    + ", consumers: " + consumerAccounts.size()
                    + ", transactions: " + transactions.size());
        }
    }

    private int createOneBankAccount(final int centsInTheSystem) {
        final int bankAccount = api.createAccount(centsInTheSystem);
        assertEquals(0, bankAccount);

        return bankAccount;
    }

    private int createOneMerchantAccount() {
        final int merchantAccountId = api.createAccount(0);
        assertNotEquals(0, merchantAccountId);
        return merchantAccountId;
    }

    private Collection<Integer> create1000ConsumerAccounts() {
        final Collection<Integer> consumerAccounts = new ArrayList<Integer>();
        for (int i = 0; i < 1000; i++) {
            final int consumerAccountId = api.createAccount(0);
            assertNotEquals(0, consumerAccountId);
            consumerAccounts.add(consumerAccountId);
        }

        return consumerAccounts;
    }

    private void seedConsumerAccountsFromBank(final int bankAccount,
            final Collection<Integer> consumerAccounts,
            final Collection<Transaction> transactions) {
        for (Integer consumerAccount : consumerAccounts) {
            final Transaction transaction = api.transfer(bankAccount,
                    consumerAccount, 10);

            assertEquals(bankAccount, transaction.from);
            assertEquals(consumerAccount.intValue(), transaction.to);
            assertEquals(10, transaction.amount);
            assertTrue(CONFIRMED == transaction.status
                    || INSUFFICIENT_FUNDS == transaction.status);

            transactions.add(transaction);
        }
    }

    private void transferFromConsumersToMerchants(
            final List<Integer> consumerAccounts,
            final List<Integer> merchantAccounts,
            final Collection<Transaction> transactions) {
        final Random random = new Random();

        for (int i = 0; i < 12000; i++) {
            final int consumerAccount = consumerAccounts
                    .get(random.nextInt(consumerAccounts.size()));
            final int merchantAccount = merchantAccounts
                    .get(random.nextInt(merchantAccounts.size()));
            final Transaction transaction = api.transfer(consumerAccount,
                    merchantAccount, 1);

            assertNotEquals(0, transaction.transactionId);
            assertEquals(consumerAccount, transaction.from);
            assertEquals(merchantAccount, transaction.to);
            assertEquals(1, transaction.amount);

            transactions.add(transaction);
        }
    }
}
