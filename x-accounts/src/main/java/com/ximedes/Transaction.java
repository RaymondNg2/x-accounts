package com.ximedes;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The transaction DTO.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Transaction {
    private static final long start = currentTimeMillis();

    public Transaction(final int transactionId, final int from, final int to,
            final int amount, final Status status) {
        super();

        this.transactionId = transactionId;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.status = status;

//        final long elapsedSeconds = MILLISECONDS.toSeconds(currentTimeMillis()
//                - start);
//        final long transactionsPerSecond = elapsedSeconds == 0L ? 0L
//                : (transactionId / elapsedSeconds);
//
//        if ((transactionId % 100000) == 0) {
//            out.println("TX[" + transactionId + "] " + amount + ": " + from
//                    + " -> " + to + ".");
//            out.println(transactionsPerSecond + " transactions per second.");
//        }
    }

    public final int transactionId;
    public final int from;
    public final int to;
    public final int amount;
    public final Status status;
}
