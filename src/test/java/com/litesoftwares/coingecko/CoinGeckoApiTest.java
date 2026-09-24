package com.litesoftwares.coingecko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litesoftwares.coingecko.LocalCoinGeckoServer.RecordedRequest;
import com.litesoftwares.coingecko.domain.ApiToken;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import java.io.IOException;
import java.net.ConnectException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoinGeckoApiTest {

    private LocalCoinGeckoServer server;
    private CoinGeckoApi api;

    @BeforeEach
    void startServer() throws IOException {
        server = new LocalCoinGeckoServer();
        api = new CoinGeckoApi();
    }

    @AfterEach
    void stopServer() {
        api.shutdown();
        server.close();
    }

    @Test
    void returnsTheBodyOfASuccessfulResponse() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(200, "{\"gecko_says\":\"(V3) To the Moon!\"}");

        assertEquals("(V3) To the Moon!", api.executeSync(service.ping()).getGeckoSays());
    }

    @Test
    void servesFurtherCallsAfterTheFirst() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(200, "{\"gecko_says\":\"first\"}");
        server.enqueue(200, "{\"gecko_says\":\"second\"}");

        api.executeSync(service.ping());

        assertEquals("second", api.executeSync(service.ping()).getGeckoSays());
    }

    @Test
    void sendsTheDemoKeyInItsHeader() throws InterruptedException {
        CoinGeckoApiService service = server.createService(api, ApiToken.demo("demo-key"));
        server.enqueue(200, "{}");

        api.executeSync(service.ping());

        assertEquals("demo-key", takeRequest().header("x-cg-demo-api-key"));
    }

    @Test
    void sendsNoKeyWithoutAToken() throws InterruptedException {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(200, "{}");

        api.executeSync(service.ping());

        assertNull(takeRequest().header("x-cg-demo-api-key"));
    }

    @Test
    void targetsThePublicApiWithoutAToken() {
        CoinGeckoApiService service = api.createService(CoinGeckoApiService.class, 1L, 1L, 1L, null);

        assertEquals("https://api.coingecko.com/api/v3/ping", urlOf(service));
    }

    @Test
    void targetsThePublicApiWithADemoToken() {
        CoinGeckoApiService service =
                api.createService(CoinGeckoApiService.class, 1L, 1L, 1L, ApiToken.demo("demo-key"));

        assertEquals("https://api.coingecko.com/api/v3/ping", urlOf(service));
    }

    @Test
    void targetsTheProApiWithAProToken() {
        CoinGeckoApiService service = api.createService(CoinGeckoApiService.class, 1L, 1L, 1L, ApiToken.pro("pro-key"));

        assertEquals("https://pro-api.coingecko.com/api/v3/ping", urlOf(service));
    }

    @Test
    void reportsTheStatusAndMessageOfAnErrorResponse() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(404, "{\"error\":\"coin not found\"}");

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> api.executeSync(service.ping()));

        assertEquals(404, exception.getError().getCode());
        assertEquals("coin not found", exception.getError().getMessage());
        assertEquals("CoinGeckoApiError(code=404, message=coin not found)", exception.getMessage());
    }

    @Test
    void reportsRateLimitingAsCloudflareError1015() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(429, "<html><body>error code: 1015</body></html>");

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> api.executeSync(service.ping()));

        assertEquals(1015, exception.getError().getCode());
        assertEquals("Rate limited", exception.getError().getMessage());
    }

    @Test
    void reportsTheResponseWhenTheErrorBodyIsNotJson() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(503, "<html><body>Service Unavailable</body></html>");

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> api.executeSync(service.ping()));

        assertNull(exception.getError());
        assertTrue(exception.getMessage().contains("code=503"), exception.getMessage());
        assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }

    @Test
    void failsOnAMalformedBody() {
        CoinGeckoApiService service = server.createService(api, null);
        server.enqueue(200, "{\"gecko_says\":");

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> api.executeSync(service.ping()));

        assertNull(exception.getError());
        assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }

    @Test
    void failsWhenTheServerIsUnreachable() {
        CoinGeckoApiService service = server.createService(api, null);
        server.close();

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> api.executeSync(service.ping()));

        assertInstanceOf(ConnectException.class, exception.getCause());
    }

    private RecordedRequest takeRequest() throws InterruptedException {
        RecordedRequest request = server.takeRequest();
        assertNotNull(request, "the server received no request");
        return request;
    }

    private static String urlOf(CoinGeckoApiService service) {
        return service.ping().request().url().toString();
    }
}
