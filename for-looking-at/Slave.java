package com.chess_ix.ticket2match.tests.load;

/**
 * A slave to the master loader.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class Slave extends Loader {

    /**
     * Start a new slave.
     * 
     * @param args
     *            The cluster member IP addresses.
     * @throws InterruptedException
     *             When something went wrong.
     */
    public static void main(final String[] args) throws InterruptedException {
        final Slave slave = new Slave(args);
        slave.startHazelcast();

        for (;;) {
            slave.awaitWorkpackage();
            slave.buyTickets();
            slave.done();
        }
    }
    private Slave(final String[] ips) {
        super(ips);
    }
}
