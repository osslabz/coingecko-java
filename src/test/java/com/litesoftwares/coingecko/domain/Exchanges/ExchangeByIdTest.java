package com.litesoftwares.coingecko.domain.Exchanges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExchangeByIdTest {

    @Test
    void exchangesWithDifferentIdsAreNotEqual() {
        ExchangeById binance = exchange("binance");
        ExchangeById kraken = exchange("kraken");

        assertNotEquals(binance, kraken);
    }

    @Test
    void exchangesWithTheSameFieldsAreEqual() {
        ExchangeById first = exchange("binance");
        ExchangeById second = exchange("binance");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void anExchangeByIdIsNotEqualToThePlainExchangeWithItsFields() {
        ExchangeById detailed = exchange("binance");
        Exchanges plain = new Exchanges();
        plain.setId("binance");

        assertNotEquals(detailed, plain);
        assertNotEquals(plain, detailed);
    }

    private static ExchangeById exchange(String id) {
        ExchangeById exchange = new ExchangeById();
        exchange.setId(id);
        exchange.setTickers(List.of());
        return exchange;
    }
}
