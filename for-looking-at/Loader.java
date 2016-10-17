package com.chess_ix.ticket2match.tests.load;

import static com.chess_ix.ticket2match.entities.BlockAvailable.fromJson;
import static com.hazelcast.core.Hazelcast.newHazelcastInstance;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.chess_ix.ticket2match.entities.BlockAvailable;
import com.chess_ix.ticket2match.tests.HttpAPI;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * The common stuff for the master and the slave.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
abstract class Loader {
    protected final String[] ips;

    private HazelcastInstance hazelcastInstance = null;

    private static final String WORK = "work";
    static Map<String, WorkPackage> work = null;
    private static final String DONE = "done";
    static Set<String> done = null;

    static final HttpAPI api = new HttpAPI();

    protected Loader(final String[] ips) {
        super();

        this.ips = ips;
    }

    void startHazelcast() {
        final JoinConfig joinConfig = new JoinConfig();
        joinConfig.setMulticastConfig(new MulticastConfig().setEnabled(false));

        final TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        for (final String ip : ips) {
            tcpIpConfig.addMember(ip);
        }
        joinConfig.setTcpIpConfig(tcpIpConfig);

        final MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);

        final NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setJoin(joinConfig);
        networkConfig.setPortAutoIncrement(false);

        final Config config = new Config();
        config.setNetworkConfig(networkConfig);
        hazelcastInstance = newHazelcastInstance(config);
        out.println("In the cluster as " + getMyIp() + ".");

        work = hazelcastInstance.getMap(WORK);
        done = hazelcastInstance.getSet(DONE);

        api.setNoTrace();
    }

    private String getMyIp() {
        return hazelcastInstance.getCluster().getLocalMember()
                .getInetSocketAddress().getAddress().getHostAddress();
    }

    boolean missingCusterMember(final String ip) {
        for (final Member member : hazelcastInstance.getCluster().getMembers()) {
            if (ip.equals(member.getInetSocketAddress().getAddress()
                    .getHostAddress())) {
                return false;
            }
        }

        return true;
    }

    // --- loader code...

    private WorkPackage workPackage = null;

    void awaitWorkpackage() throws InterruptedException {
        out.print("Awaiting work package ... ");
        out.flush();

        while (!work.containsKey(getMyIp())) {
            sleep(100L);
        }

        workPackage = work.remove(getMyIp());
        out.println("done, threads: " + workPackage.threads + ", iterations: "
                + workPackage.iterations);
    }

    void buyTickets() throws InterruptedException {
        final long start = currentTimeMillis();
        final Thread[] threads = new Thread[workPackage.threads];
        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(new Runnable() {
                private final Random random = new Random(currentThread()
                        .getId() * currentTimeMillis());

                @Override
                public void run() {
                    try {
                        for (int i = 0; i < workPackage.iterations; i++) {
                            final int seats;
                            // try to end up with about 2.5 average
                            switch (random.nextInt(5)) {
                            case 0:
                            case 1:
                                seats = 2;
                                break;
                            case 2:
                            case 3:
                                seats = 3;
                                break;
                            default:
                                seats = random.nextInt(14) + 1;
                            }
                            final List<BlockAvailable> blocks = fromJson(api
                                    .getAvailability());
                            if (blocks.size() > 0) {
                                final BlockAvailable block = blocks.get(random
                                        .nextInt(blocks.size()));

                                final String requestUuid = randomUUID()
                                        .toString();
                                api.buySeats(requestUuid, block.getUuid(),
                                        seats - 1, 1, 0);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        for (int t = 0; t < threads.length; t++) {
            threads[t].setDaemon(true);
            threads[t].start();
            out.print("+");
            out.flush();
        }
        out.println();
        for (int t = 0; t < threads.length; t++) {
            threads[t].join();
            out.print("-");
            out.flush();
        }
        out.println(" work package processed completely in "
                + (currentTimeMillis() - start) + " ms.");
    }

    void done() throws InterruptedException {
        done.add(getMyIp());
    }
}
