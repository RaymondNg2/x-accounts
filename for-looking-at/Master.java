package com.chess_ix.ticket2match.tests.load;

import static com.chess_ix.ticket2match.tests.HttpAPI.STADIUM_UUID;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

import com.chess_ix.ticket2match.entities.Block;
import com.chess_ix.ticket2match.entities.Pricelist;
import com.chess_ix.ticket2match.entities.Stadium;

/**
 * My own load tester. This is the master, that creates the stadium and doles
 * out the work to the slaves.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class Master extends Loader {

    /**
     * Start the master, creating a stadium and start selling tickets like
     * crazy.
     * 
     * @param args
     *            The cluster member IP addresses.
     * @throws InterruptedException
     *             When something went wrong.
     */
    public static void main(final String[] args) throws InterruptedException {
        final Master master = new Master(args);
        master.startHazelcast();
        master.awaitSlavesReady();

        final Thread slave = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (;;) {
                        master.awaitWorkpackage();
                        master.buyTickets();
                        master.done();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        slave.setDaemon(true);
        slave.start();

        for (int threads = 10; threads <= 100000; threads += 10000) {
            master.createStadium();

            final long start = master.sendWorkPackages(threads);
            final long ms = master.awaitAllSlaves(start);
            master.report(threads, ms);
        }
    }

    private Master(final String[] ips) {
        super(ips);
    }

    @Override
    void startHazelcast() {
        super.startHazelcast();

        work.clear();
        done.clear();
    }

    private void createStadium() throws InterruptedException {
        sleep(2000L);
        out.println();

        out.print("Creating stadium ... ");
        out.flush();

        api.reset();

        api.addStadium(new Stadium(STADIUM_UUID, "Goffert Stadion"));
        final String priceOneUuid = randomUUID().toString();
        api.addPricelist(new Pricelist(priceOneUuid, "price one", 1500, 1100,
                1200));
        final String priceTwoUuid = randomUUID().toString();
        api.addPricelist(new Pricelist(priceTwoUuid, "price two", 2500, 2100,
                2200));
        final String priceThreeUuid = randomUUID().toString();
        api.addPricelist(new Pricelist(priceThreeUuid, "price three", 2500,
                2100, 2200));

        for (int i = 0; i < 100; i++) {
            api.addBlock(new Block(randomUUID().toString(), "block_1_" + i, 30,
                    30, priceOneUuid));
        }

        final String blockedBlockUuid = randomUUID().toString();
        api.addBlock(new Block(blockedBlockUuid, "blocked", 2, 2, priceOneUuid));
        final String halfBlockedUuid = randomUUID().toString();
        api.addBlock(new Block(halfBlockedUuid, "half-blocked", 2, 2,
                priceOneUuid));
        final String priceyBlock = randomUUID().toString();
        api.addBlock(new Block(priceyBlock, "pricey seats", 10, 4, priceOneUuid));

        for (int i = 0; i < 50; i++) {
            api.addBlock(new Block(randomUUID().toString(), "block_2_" + i, 29,
                    29, priceOneUuid));
        }

//        api.blockSeat(blockedBlockUuid, 1, 1);
//        api.blockSeat(blockedBlockUuid, 1, 2);
//        api.blockSeat(blockedBlockUuid, 2, 1);
//        api.blockSeat(blockedBlockUuid, 2, 2);
//
//        api.blockSeat(halfBlockedUuid, 1, 1);
//        api.blockSeat(halfBlockedUuid, 2, 1);
//
//        api.priceSeat(priceyBlock, 3, 1, priceTwoUuid);
//        api.priceSeat(priceyBlock, 3, 2, priceTwoUuid);
//        api.priceSeat(priceyBlock, 3, 3, priceTwoUuid);
//        api.priceSeat(priceyBlock, 3, 4, priceTwoUuid);
//        api.priceSeat(priceyBlock, 4, 1, priceThreeUuid);
//        api.priceSeat(priceyBlock, 4, 2, priceThreeUuid);
//        api.priceSeat(priceyBlock, 4, 3, priceThreeUuid);
//        api.priceSeat(priceyBlock, 4, 4, priceThreeUuid);

        out.println("done.");
    }

    private void awaitSlavesReady() throws InterruptedException {
        sleep(2000L);
        out.println();

        for (final String ip : ips) {
            out.print("Waiting for " + ip + " ");
            out.flush();

            while (missingCusterMember(ip)) {
                sleep(1000L);
                out.print(".");
                out.flush();
            }

            out.println(" ready.");
        }
    }

    private long sendWorkPackages(final int threads)
            throws InterruptedException {
        work.clear();
        done.clear();
        api.start();

        // let the system coool off a bit...
        sleep(10000L);
        out.println();

        final long start = currentTimeMillis();
        for (final String ip : ips) {
            work.put(ip, new WorkPackage(threads / ips.length, 60000 / threads));
        }

        return start;
    }

    private long awaitAllSlaves(final long start) throws InterruptedException {
        sleep(2000L);
        out.println();

        for (final String ip : ips) {
            out.print("Waiting for " + ip + " ");
            out.flush();

            while (!done.contains(ip)) {
                sleep(1000L);
                out.print(".");
                out.flush();
            }

            out.println(" ready.");
        }
        final long ms = currentTimeMillis() - start;

        api.stop();
        done.clear();
        return ms;
    }

    private String report = "\n\nPerformance Report:";

    private void report(final int threads, final long ms)
            throws InterruptedException {
        sleep(2000L);
        out.println();

        report += "\n  Using " + threads + " threads, sold " + api.countSeats()
                + " seats and " + api.countTickets() + " tickets in " + ms
                + " ms. That is " + ((api.countSeats() * 1000L) / ms)
                + " seats per second, and "
                + ((api.countTickets() * 1000L) / ms) + " tickets per second.";
        out.println(report + "\n\n");
    }
}
