package com.chess_ix.ticket2match.tests;

import static com.chess_ix.ticket2match.entities.BlockAvailable.fromJson;
import static com.chess_ix.ticket2match.entities.Seat.blockSeatJson;
import static com.chess_ix.ticket2match.entities.Seat.priceSeatJson;
import static com.chess_ix.ticket2match.utilities.LanguageUtils.sneakyThrow;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.chess_ix.ticket2match.api.API;
import com.chess_ix.ticket2match.entities.Block;
import com.chess_ix.ticket2match.entities.BlockAvailable;
import com.chess_ix.ticket2match.entities.Pricelist;
import com.chess_ix.ticket2match.entities.Seat;
import com.chess_ix.ticket2match.entities.Stadium;
import com.chess_ix.ticket2match.entities.Ticket;

/**
 * The HTTP based implementation of the API, used for integration testing.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class HttpAPI implements API {
    private boolean trace = true;

    public static final String STADIUM_UUID = "36260d26-5a12-11e4-bdfd-e4fa0bf9330f";

    /**
     * Disable tracing to get more speed.
     */
    public void setNoTrace() {
        trace = false;
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getStadiums()
     */
    @Override
    public Collection<Stadium> getStadiums() {
        // note that we send a bogus stadium UUID
        final JSONArray blocksJson = getArray("/stadiums");
        final Collection<Stadium> stadiums = new ArrayList<>();
        if (blocksJson != null) {
            for (final Object json : blocksJson) {
                stadiums.add(Stadium.fromJson((JSONObject) json));
            }
        }
        return stadiums;
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getStadium(String)
     */
    @Override
    public Stadium getStadium(final String uuid) {
        final JSONObject json = get("/stadiums/" + uuid);
        return Stadium.fromJson(json);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#addStadium(Stadium)
     */
    @Override
    public Stadium addStadium(final Stadium stadium) {
        final JSONObject json = post("/stadiums", stadium.toJson());
        return Stadium.fromJson(json);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getPricelists()
     */
    @Override
    public Collection<Pricelist> getPricelists() {
        final JSONArray pricelistJson = getArray("/pricelists");
        final Collection<Pricelist> pricelists = new ArrayList<>(
                pricelistJson.size());

        for (final Object json : pricelistJson) {
            pricelists.add(Pricelist.fromJson((JSONObject) json));
        }
        return pricelists;
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getPricelist(String)
     */
    @Override
    public Pricelist getPricelist(final String uuid) {
        final JSONObject json = get("/pricelists/" + uuid);
        return Pricelist.fromJson(json);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#addPricelist(Pricelist)
     */
    @Override
    public void addPricelist(final Pricelist pricelist) {
        post("/pricelists", pricelist.toJson());
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#addBlock(Block)
     */
    @Override
    public Block addBlock(final Block block) {
        final JSONObject json = post("/stadiums/" + STADIUM_UUID + "/block",
                block.toJson());
        return Block.fromJson(json);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getBlocks()
     */
    @Override
    public Collection<BlockAvailable> getBlocks() {
        try {
            sleep(1000L);
        } catch (InterruptedException e) {
            sneakyThrow(e);
        }

        return fromJson(getAvailability());
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getBlock(String)
     */
    @Override
    public Block getBlock(final String uuid) {
        // note that we send a bogus stadium UUID
        final JSONObject json = get("/stadiums/" + STADIUM_UUID + "/block/"
                + uuid);
        return Block.fromJson(json);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#blockSeat(String, int, int)
     */
    @Override
    public Seat blockSeat(final String blockUuid, final int row, final int seat) {
        final JSONObject json = post("/stadiums/" + STADIUM_UUID + "/block/"
                + blockUuid + "/row/" + row + "/seat/" + seat, blockSeatJson());
        return Seat.fromJson(json, blockUuid);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#priceSeat(String, int, int,
     *      String)
     */
    @Override
    public Seat priceSeat(final String blockUuid, final int row,
            final int seat, final String pricelistUuid) {
        final JSONObject json = post("/stadiums/" + STADIUM_UUID + "/block/"
                + blockUuid + "/row/" + row + "/seat/" + seat,
                priceSeatJson(pricelistUuid));
        return Seat.fromJson(json, blockUuid);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getAvailability()
     */
    @Override
    public byte[] getAvailability() {
        return fetchBytes("GET", "/available/" + STADIUM_UUID, null);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#buySeats(String, String, int, int,
     *      int)
     */
    @Override
    public Collection<Ticket> buySeats(final String ticketUuid,
            final String blockUuid, final int adults, final int kids,
            final int seniors) {
        final JSONArray ticketsJson = postArray("/tickets/buy/" + STADIUM_UUID
                + "/" + blockUuid,
                Ticket.requestJson(ticketUuid, adults, kids, seniors));

        final Collection<Ticket> tickets = new ArrayList<>(ticketsJson.size());
        for (final Object json : ticketsJson) {
            tickets.add(Ticket.fromJson((JSONObject) json));
        }

        // if (tickets.size() < 1) {
        // throw new IllegalStateException("NOT SEATS AVAILABLE");
        // }
        return tickets;
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#countTickets()
     */
    @Override
    public int countTickets() {
        final JSONObject json = get("/tickets/count");
        return json.getInteger("number-of-tickets");
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#countSeats()
     */
    @Override
    public int countSeats() {
        final JSONObject json = get("/tickets/count");
        return json.getInteger("number-of-seats");
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#getTickets()
     */
    @Override
    public Collection<Ticket> getTickets() {
        final JSONArray ticketsJson = getArray("/tickets/" + STADIUM_UUID);

        final Collection<Ticket> tickets = new ArrayList<>();
        for (final Object json : ticketsJson) {
            tickets.add(Ticket.fromJson((JSONObject) json));
        }
        return tickets;
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#start()
     */
    @Override
    public void start() {
        post("/start/" + STADIUM_UUID);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#stop()
     */
    @Override
    public void stop() {
        post("/stop/" + STADIUM_UUID);
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#reset()
     */
    @Override
    public void reset() {
        post("/reset");
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#shutdown()
     */
    @Override
    public void shutdown() {
        // nothing to shut down...
    }

    /**
     * @see com.chess_ix.ticket2match.api.API#testCrash(String, int)
     */
    @Override
    public void testCrash(final String blockUuid, final int seatsLostInCrash) {
        final JSONObject json = new JSONObject();
        json.put("block", blockUuid);
        json.put("seatsLostInCrash", seatsLostInCrash);
        post("/test/crash", json);
    }

    /* the methods below implement the methods above. */

    private final Random randomHost = new Random();
    private final JSONParser jsonParser = new JSONParser();

    /**
     * Pick a random node to send a call to.
     * 
     * @return The IP address of the selected node as a string.
     */
    private String randomNode() {
        if (randomHost.nextInt(2) == 0) {
            return "http://54.77.184.153:9080/rest/rest";
        }
        return "http://54.77.224.47:9080/rest/rest";
    }

    private JSONObject get(final String path) {
        return (JSONObject) fetch("GET", path, null);
    }

    private JSONObject post(final String path) {
        return (JSONObject) fetch("POST", path, null);
    }

    private JSONObject post(final String path, final JSONObject body) {
        return (JSONObject) fetch("POST", path, body);
    }

    private JSONArray getArray(final String path) {
        return (JSONArray) fetch("GET", path, null);
    }

    private JSONArray postArray(final String path, final JSONObject body) {
        return (JSONArray) fetch("POST", path, body);
    }

    private Object fetch(final String method, final String path,
            final JSONObject body) {
        try {
            final byte[] bytes = fetchBytes(method, path, body);
            return jsonParser.parse(new InputStreamReader(
                    new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

    private static final int BUFSIZE = 16 * 1024; // 16k

    private byte[] fetchBytes(final String method, final String path,
            final JSONObject body) {

        try {
            final long start = currentTimeMillis();

            final URL url = new URL(randomNode() + path);
            if (trace) {
                out.println(method + " " + url);
            }

            final HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod(method);

            if (body != null) {
                connection.setRequestProperty("Content-Type",
                        "text/json; charset=utf-8");

                if (trace) {
                    out.println("--> " + body);
                }
                final byte[] bytes = body.toString().getBytes(UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length",
                        Integer.toString(bytes.length));
                connection.getOutputStream().write(bytes);
                connection.getOutputStream().flush();
            }

            if (trace) {
                out.println("<-- " + (currentTimeMillis() - start)
                        + " ms HTTP " + connection.getResponseCode() + ": "
                        + connection.getResponseMessage());
            }

            final InputStream is;
            if ("gzip".equals(connection.getContentEncoding())) {
                is = new GZIPInputStream(new BufferedInputStream(
                        connection.getInputStream(), BUFSIZE));
            } else {
                is = new BufferedInputStream(connection.getInputStream(),
                        BUFSIZE);
            }

            final ByteArrayOutputStream bytes = new ByteArrayOutputStream(
                    BUFSIZE);
            final byte[] buffer = new byte[BUFSIZE];
            int bytesRead = 0;
            do {
                bytesRead = is.read(buffer);
                if (bytesRead != -1) {
                    bytes.write(buffer, 0, bytesRead);
                }
            } while (bytesRead != -1);
            is.close();

            if (trace) {
                out.println("<-- " + (currentTimeMillis() - start) + " ms "
                        + bytes);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }
}
