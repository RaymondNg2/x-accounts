package com.ximedes;

/**
 * The transaction DTO.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Transaction {
    public Transaction(final int transactionId, final int from, final int to,
            final int amount, final Status status) {
        super();

        this.transactionId = transactionId;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.status = status;
    }

    public final int transactionId;
    public int from;
    public int to;
    public int amount;
    public Status status;
}
