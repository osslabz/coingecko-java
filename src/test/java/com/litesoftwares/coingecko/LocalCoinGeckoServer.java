package com.litesoftwares.coingecko;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.litesoftwares.coingecko.domain.ApiToken;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** A CoinGecko stand-in on a loopback port: answers each request with the next queued response and records it. */
public final class LocalCoinGeckoServer implements AutoCloseable {

    private final HttpServer server;
    private final BlockingQueue<CannedResponse> responses = new LinkedBlockingQueue<>();
    private final BlockingQueue<RecordedRequest> requests = new LinkedBlockingQueue<>();

    public LocalCoinGeckoServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    public void enqueue(int status, String body) {
        responses.add(new CannedResponse(status, body));
    }

    /** The next request the server received, or null if none arrives within five seconds. */
    public RecordedRequest takeRequest() throws InterruptedException {
        return requests.poll(5, TimeUnit.SECONDS);
    }

    public CoinGeckoApiService createService(CoinGeckoApi api, ApiToken apiToken) {
        return api.createService(CoinGeckoApiService.class, 5L, 5L, 5L, apiToken, baseUrl());
    }

    private String baseUrl() {
        InetSocketAddress address = server.getAddress();
        return "http://" + address.getHostString() + ":" + address.getPort() + "/api/v3/";
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        requests.add(new RecordedRequest(exchange.getRequestURI(), exchange.getRequestHeaders()));
        CannedResponse response = responses.poll();
        if (response == null) {
            response = new CannedResponse(500, "no response queued");
        }
        byte[] body = response.body().getBytes(UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // A length of 0 announces a chunked body; -1 announces none.
        exchange.sendResponseHeaders(response.status(), body.length == 0 ? -1 : body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    public record RecordedRequest(URI uri, Headers headers) {

        public String pathAndQuery() {
            return uri.getRawQuery() == null ? uri.getRawPath() : uri.getRawPath() + "?" + uri.getRawQuery();
        }

        public String header(String name) {
            return headers.getFirst(name);
        }
    }

    private record CannedResponse(int status, String body) {}
}
