package com.chess_ix.ticket2match.tests.load;

import java.io.Serializable;

/**
 * A package of work for a slave.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class WorkPackage implements Serializable {
    private static final long serialVersionUID = 2746687409609203466L;

    final int threads, iterations;

    WorkPackage(int threads, int iterations) {
        super();

        this.threads = threads;
        this.iterations = iterations;
    }
}
