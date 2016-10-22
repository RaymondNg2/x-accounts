package com.ximedes.http;

import static com.ximedes.Status.CONFIRMED;
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

    /**
     * A test case.
     */
    @Test
    public void testThatFrom0IsParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"from\":\"0\",\"to\":\"337\",\"amount\":10}");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(0, transaction.from);
        assertEquals(337, transaction.to);
        assertEquals(10, transaction.amount);
    }

    /**
     * A test case.
     */
    @Test
    public void testThatFullTransferParsed() {
        final StringBuilder json = new StringBuilder(
                "{\"transactionId\":\"51\",\"from\":\"0\",\"to\":\"9\",\"amount\":10,\"status\":\"CONFIRMED\"}");

        final Transaction transaction = UhmParser.uhmParseJsonTransfer(json);
        assertEquals(51, transaction.transactionId);
        assertEquals(0, transaction.from);
        assertEquals(9, transaction.to);
        assertEquals(10, transaction.amount);
        assertEquals(CONFIRMED, transaction.status);
    }
}
