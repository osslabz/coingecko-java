package com.litesoftwares.coingecko.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.litesoftwares.coingecko.CoinGeckoApi;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.LocalCoinGeckoServer;
import com.litesoftwares.coingecko.LocalCoinGeckoServer.RecordedRequest;
import com.litesoftwares.coingecko.constant.Currency;
import com.litesoftwares.coingecko.domain.ApiToken;
import com.litesoftwares.coingecko.domain.Coins.CoinFullData;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.domain.Coins.MarketChart;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CoinGeckoApiClientImplTest {

    private LocalCoinGeckoServer server;
    private CoinGeckoApiClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = new LocalCoinGeckoServer();
        client = clientFor(null);
    }

    @AfterEach
    void stopServer() {
        client.shutdown();
        server.close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void requestsTheEndpointWithItsParameters(Endpoint endpoint) throws InterruptedException {
        server.enqueue(200, endpoint.responseBody());

        assertNotNull(endpoint.call().apply(client));
        assertEquals(endpoint.pathAndQuery(), takeRequest().pathAndQuery());
    }

    @SuppressWarnings("deprecation") // the deprecated endpoints are still part of the client
    static Stream<Endpoint> endpoints() {
        return Stream.of(
                new Endpoint("ping", CoinGeckoApiClient::ping, "/api/v3/ping", "{}"),
                new Endpoint(
                        "price with defaults",
                        c -> c.getPrice("bitcoin", "usd"),
                        "/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_market_cap=false"
                                + "&include_24hr_vol=false&include_24hr_change=false&include_last_updated_at=false",
                        "{}"),
                new Endpoint(
                        "price in Czech koruna",
                        c -> c.getPrice("bitcoin", Currency.CZK),
                        "/api/v3/simple/price?ids=bitcoin&vs_currencies=czk&include_market_cap=false"
                                + "&include_24hr_vol=false&include_24hr_change=false&include_last_updated_at=false",
                        "{}"),
                new Endpoint(
                        "price with every option",
                        c -> c.getPrice("bitcoin", "usd", true, true, true, true),
                        "/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_market_cap=true"
                                + "&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true",
                        "{}"),
                new Endpoint(
                        "token price with defaults",
                        c -> c.getTokenPrice("ethereum", "0xabc", "usd"),
                        "/api/v3/simple/token_price/ethereum?contract_addresses=0xabc&vs_currencies=usd"
                                + "&include_market_cap=false&include_24hr_vol=false&include_24hr_change=false"
                                + "&include_last_updated_at=false",
                        "{}"),
                new Endpoint(
                        "token price with every option",
                        c -> c.getTokenPrice("ethereum", "0xabc", "usd", true, true, true, true),
                        "/api/v3/simple/token_price/ethereum?contract_addresses=0xabc&vs_currencies=usd"
                                + "&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true"
                                + "&include_last_updated_at=true",
                        "{}"),
                new Endpoint(
                        "supported vs currencies",
                        CoinGeckoApiClient::getSupportedVsCurrencies,
                        "/api/v3/simple/supported_vs_currencies",
                        "[]"),
                new Endpoint("coin list", CoinGeckoApiClient::getCoinList, "/api/v3/coins/list", "[]"),
                new Endpoint(
                        "coin markets with defaults",
                        c -> c.getCoinMarkets("usd"),
                        "/api/v3/coins/markets?vs_currency=usd&sparkline=false",
                        "[]"),
                new Endpoint(
                        "coin markets without category",
                        c -> c.getCoinMarkets("usd", "bitcoin", "market_cap_desc", 10, 2, true, "24h"),
                        "/api/v3/coins/markets?vs_currency=usd&ids=bitcoin&order=market_cap_desc&per_page=10"
                                + "&page=2&sparkline=true&price_change_percentage=24h",
                        "[]"),
                new Endpoint(
                        "coin markets with category",
                        c -> c.getCoinMarkets("usd", "bitcoin", "defi", "market_cap_desc", 10, 2, true, "24h"),
                        "/api/v3/coins/markets?vs_currency=usd&ids=bitcoin&category=defi&order=market_cap_desc"
                                + "&per_page=10&page=2&sparkline=true&price_change_percentage=24h",
                        "[]"),
                new Endpoint(
                        "coin with defaults",
                        c -> c.getCoinById("bitcoin"),
                        "/api/v3/coins/bitcoin?localization=true&tickers=true&market_data=true"
                                + "&community_data=true&developer_data=true&sparkline=false",
                        "{}"),
                new Endpoint(
                        "coin with every option",
                        c -> c.getCoinById("bitcoin", false, false, false, false, false, true),
                        "/api/v3/coins/bitcoin?localization=false&tickers=false&market_data=false"
                                + "&community_data=false&developer_data=false&sparkline=true",
                        "{}"),
                new Endpoint(
                        "coin tickers with defaults",
                        c -> c.getCoinTickerById("bitcoin"),
                        "/api/v3/coins/bitcoin/tickers",
                        "{}"),
                new Endpoint(
                        "coin tickers with every option",
                        c -> c.getCoinTickerById("bitcoin", "binance", 2, "volume_desc"),
                        "/api/v3/coins/bitcoin/tickers?exchange_ids=binance&page=2&order=volume_desc",
                        "{}"),
                new Endpoint(
                        "coin history with defaults",
                        c -> c.getCoinHistoryById("bitcoin", "30-12-2017"),
                        "/api/v3/coins/bitcoin/history?date=30-12-2017&localization=true",
                        "{}"),
                new Endpoint(
                        "coin history without localization",
                        c -> c.getCoinHistoryById("bitcoin", "30-12-2017", false),
                        "/api/v3/coins/bitcoin/history?date=30-12-2017&localization=false",
                        "{}"),
                new Endpoint(
                        "market chart",
                        c -> c.getCoinMarketChartById("bitcoin", "usd", 1),
                        "/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=1",
                        "{}"),
                new Endpoint(
                        "market chart with interval",
                        c -> c.getCoinMarketChartById("bitcoin", "usd", 1, "daily"),
                        "/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=1&interval=daily",
                        "{}"),
                new Endpoint(
                        "market chart range",
                        c -> c.getCoinMarketChartRangeById("bitcoin", "usd", "1392577232", "1422577232"),
                        "/api/v3/coins/bitcoin/market_chart/range?vs_currency=usd&from=1392577232&to=1422577232",
                        "{}"),
                new Endpoint(
                        "OHLC",
                        c -> c.getCoinOHLC("bitcoin", "usd", 7),
                        "/api/v3/coins/bitcoin/ohlc?vs_currency=usd&days=7",
                        "[]"),
                new Endpoint(
                        "coin status updates with defaults",
                        c -> c.getCoinStatusUpdateById("bitcoin"),
                        "/api/v3/coins/bitcoin/status_updates",
                        "{}"),
                new Endpoint(
                        "coin status updates with paging",
                        c -> c.getCoinStatusUpdateById("bitcoin", 10, 2),
                        "/api/v3/coins/bitcoin/status_updates?per_page=10&page=2",
                        "{}"),
                new Endpoint(
                        "coin by contract address",
                        c -> c.getCoinInfoByContractAddress("ethereum", "0xabc"),
                        "/api/v3/coins/ethereum/contract/0xabc",
                        "{}"),
                new Endpoint("asset platforms", CoinGeckoApiClient::getAssetPlatforms, "/api/v3/asset_platforms", "[]"),
                new Endpoint(
                        "exchanges with defaults",
                        CoinGeckoApiClient::getExchanges,
                        "/api/v3/exchanges?per_page=100&page=0",
                        "[]"),
                new Endpoint(
                        "exchanges with paging",
                        c -> c.getExchanges(10, 2),
                        "/api/v3/exchanges?per_page=10&page=2",
                        "[]"),
                new Endpoint("exchanges list", CoinGeckoApiClient::getExchangesList, "/api/v3/exchanges/list", "[]"),
                new Endpoint("exchange", c -> c.getExchangesById("binance"), "/api/v3/exchanges/binance", "{}"),
                new Endpoint(
                        "exchange tickers with defaults",
                        c -> c.getExchangesTickersById("binance"),
                        "/api/v3/exchanges/binance/tickers",
                        "{}"),
                new Endpoint(
                        "exchange tickers with every option",
                        c -> c.getExchangesTickersById("binance", "bitcoin", 2, "volume_desc"),
                        "/api/v3/exchanges/binance/tickers?coin_ids=bitcoin&page=2&order=volume_desc",
                        "{}"),
                new Endpoint(
                        "exchange status updates with defaults",
                        c -> c.getExchangesStatusUpdatesById("binance"),
                        "/api/v3/exchanges/binance/status_updates",
                        "{}"),
                new Endpoint(
                        "exchange status updates with paging",
                        c -> c.getExchangesStatusUpdatesById("binance", 10, 2),
                        "/api/v3/exchanges/binance/status_updates?per_page=10&page=2",
                        "{}"),
                new Endpoint(
                        "exchange volume chart",
                        c -> c.getExchangesVolumeChart("binance", 1),
                        "/api/v3/exchanges/binance/volume_chart?days=1",
                        "[]"),
                new Endpoint(
                        "status updates with defaults",
                        CoinGeckoApiClient::getStatusUpdates,
                        "/api/v3/status_updates",
                        "{}"),
                new Endpoint(
                        "status updates with every option",
                        c -> c.getStatusUpdates("general", "coin", 10, 2),
                        "/api/v3/status_updates?category=general&project_type=coin&per_page=10&page=2",
                        "{}"),
                new Endpoint("events with defaults", CoinGeckoApiClient::getEvents, "/api/v3/events", "{}"),
                new Endpoint(
                        "events with every option",
                        c -> c.getEvents("US", "Meetup", 2, true, "2019-01-01", "2019-12-31"),
                        "/api/v3/events?country_code=US&type=Meetup&page=2&upcoming_events_only=true"
                                + "&from_date=2019-01-01&to_date=2019-12-31",
                        "{}"),
                new Endpoint(
                        "event countries", CoinGeckoApiClient::getEventsCountries, "/api/v3/events/countries", "{}"),
                new Endpoint("event types", CoinGeckoApiClient::getEventsTypes, "/api/v3/events/types", "{}"),
                new Endpoint("exchange rates", CoinGeckoApiClient::getExchangeRates, "/api/v3/exchange_rates", "{}"),
                new Endpoint("trending", CoinGeckoApiClient::getTrending, "/api/v3/search/trending", "{}"),
                new Endpoint("search", c -> c.getSearchResult("bit coin"), "/api/v3/search?query=bit%20coin", "{}"),
                new Endpoint("global", CoinGeckoApiClient::getGlobal, "/api/v3/global", "{}"),
                new Endpoint(
                        "decentralized finance",
                        CoinGeckoApiClient::getDecentralizedFinanceDefi,
                        "/api/v3/global/decentralized_finance_defi",
                        "{}"));
    }

    @Test
    void decodesPrices() {
        server.enqueue(200, "{\"bitcoin\":{\"usd\":67187.34}}");

        Map<String, Map<String, Double>> prices = client.getPrice("bitcoin", "usd");

        assertEquals(Map.of("bitcoin", Map.of("usd", 67187.34)), prices);
    }

    @Test
    void decodesCoinMarkets() {
        server.enqueue(
                200,
                "[{\"id\":\"bitcoin\",\"symbol\":\"btc\",\"name\":\"Bitcoin\",\"current_price\":67187.34,"
                        + "\"market_cap_rank\":1,\"unknown_field\":true}]");

        List<CoinMarkets> markets = client.getCoinMarkets("usd");

        assertEquals(1, markets.size());
        CoinMarkets bitcoin = markets.get(0);
        assertEquals("bitcoin", bitcoin.getId());
        assertEquals("btc", bitcoin.getSymbol());
        assertEquals(new BigDecimal("67187.34"), bitcoin.getCurrentPrice());
        assertEquals(1, bitcoin.getMarketCapRank());
    }

    @Test
    void decodesACoinWithItsMarketData() {
        server.enqueue(
                200,
                "{\"id\":\"bitcoin\",\"name\":\"Bitcoin\",\"market_data\":{\"current_price\":{\"usd\":67187.34}}}");

        CoinFullData bitcoin = client.getCoinById("bitcoin");

        assertEquals("Bitcoin", bitcoin.getName());
        assertEquals(Map.of("usd", 67187.34), bitcoin.getMarketData().getCurrentPrice());
    }

    @Test
    void decodesAMarketChart() {
        server.enqueue(200, "{\"prices\":[[1711843200000,69702.3]],\"market_caps\":[],\"total_volumes\":[]}");

        MarketChart chart = client.getCoinMarketChartById("bitcoin", "usd", 1);

        assertEquals(List.of(List.of("1711843200000", "69702.3")), chart.getPrices());
    }

    @Test
    void sendsTheDemoKeyInItsHeader() throws InterruptedException {
        CoinGeckoApiClient demoClient = clientFor(ApiToken.demo("demo-key"));
        server.enqueue(200, "{}");

        demoClient.ping();

        assertEquals("demo-key", takeRequest().header("x-cg-demo-api-key"));
    }

    @Test
    void reportsAnErrorResponse() {
        server.enqueue(404, "{\"error\":\"coin not found\"}");

        CoinGeckoApiException exception =
                assertThrows(CoinGeckoApiException.class, () -> client.getCoinById("no-such-coin"));

        assertEquals(404, exception.getError().getCode());
        assertEquals("coin not found", exception.getError().getMessage());
    }

    private CoinGeckoApiClient clientFor(ApiToken apiToken) {
        CoinGeckoApi api = new CoinGeckoApi();
        return new CoinGeckoApiClientImpl(api, server.createService(api, apiToken));
    }

    private RecordedRequest takeRequest() throws InterruptedException {
        RecordedRequest request = server.takeRequest();
        assertNotNull(request, "the server received no request");
        return request;
    }

    record Endpoint(String name, Function<CoinGeckoApiClient, Object> call, String pathAndQuery, String responseBody) {

        @Override
        public String toString() {
            return name;
        }
    }
}
