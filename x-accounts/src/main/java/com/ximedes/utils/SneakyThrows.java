package com.ximedes.utils;

/**
 * Sneaky throws.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SneakyThrows {
    /**
     * Sneakily throw a checked exception so that the compiler thinks it is
     * unchecked. Taken from Project Lombok,
     * http://projectlombok.org/features/SneakyThrows.html
     * 
     * @param t
     *            The exception to throw as unchecked.
     * @return Bogus return value to allow us to write "return ..." in
     *         functions.
     */
    public static RuntimeException sneakyThrow(final Throwable t) {
        if (t == null) {
            throw new NullPointerException("exception t may not be null");
        }

        SneakyThrows.<RuntimeException> sneakyThrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(final Throwable t)
            throws T {
        throw (T) t;
    }
}
