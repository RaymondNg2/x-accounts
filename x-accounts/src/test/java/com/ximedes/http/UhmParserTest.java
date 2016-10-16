package com.ximedes.http;

import static com.ximedes.http.UhmParser.uhmParseJsonLastInteger;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ximedes.Transaction;

/**
 * Tests for my ... uhm ... parser. We mainly test happy cases. Checking
 * boundaries takes valueable time. ;-)
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class UhmParserTest {
    /**
     * A test case.
     */
    @Test
    public void testThatCompactJsonIsParsed() {
        final StringBuilder json = new StringBuilder("{\"overdraft\":123}");
        assertEquals(123, uhmParseJsonLastInteger(json));
    }

    /**
     * A test case.
     */
    @Test
    public void testThatAiryJsonIsParsed() {
        final StringBuilder json = new StringBuilder("{\"overdraft\": 123 }");
        assertEquals(123, uhmParseJsonLastInteger(json));
    }

    /**
     * A test case.
     */
    @Test
    public void testThatMultilineJsonIsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"overdraft\":\n 123\n}");
        assertEquals(123, uhmParseJsonLastInteger(json));
    }

    /**
     * A test case.
     */
    @Test
    public void testThatCompactUriIsParsed() {
        final String uri = "/account/123";
        assertEquals(123, UhmParser.uhmParseUriLastInteger(uri));
    }

    /**
     * A test case.
     */
    @Test
    public void testThatAiryUriIsParsed() {
        final String uri = "/account/123 ";
        assertEquals(123, UhmParser.uhmParseUriLastInteger(uri));
    }

    /**
     * A test case.
     */
    @Test
    public void testThatCompactTransactionIsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"from\":\"123\",\"to\":\"456\",\"amount\":789}");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(123, transaction.from);
        assertEquals(456, transaction.to);
        assertEquals(789, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatAiryTransactionIsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"from\": \"123\",\"to\":\" 456\",\"amount\":789 }");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(123, transaction.from);
        assertEquals(456, transaction.to);
        assertEquals(789, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatVeryAiryTransactionIsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"from\": \"123\" ,\"to\":\" 456\" ,\"amount\"  :  789 }");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(123, transaction.from);
        assertEquals(456, transaction.to);
        assertEquals(789, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatMultilineTransactionIsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"from\"\n: \"123\" ,\"to\":\"\n 456\" ,\"amount\"  :  789\n }");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(123, transaction.from);
        assertEquals(456, transaction.to);
        assertEquals(789, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatOrderMakesNoDifference() {
        final StringBuilder json = new StringBuilder(
                "{\"to\":\"\n 456\" ,\"amount\"  :  789\n ,\"from\"\n: \"123\"}");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(123, transaction.from);
        assertEquals(456, transaction.to);
        assertEquals(789, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatShortIdsAreParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"to\":\"4\",\"amount\":7,\"from\":\"1\"}");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(1, transaction.from);
        assertEquals(4, transaction.to);
        assertEquals(7, transaction.amount);
    }
}
