package com.litesoftwares.coingecko.domain.Exchanges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.litesoftwares.coingecko.domain.Shared.Ticker;
import java.util.List;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class ExchangeById extends Exchanges {
    @JsonProperty("tickers")
    private List<Ticker> tickers;

    @JsonProperty("status_updates")
    private List<Object> statusUpdates;
}
