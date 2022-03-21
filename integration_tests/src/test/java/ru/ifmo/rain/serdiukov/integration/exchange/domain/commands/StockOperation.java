package ru.ifmo.rain.serdiukov.integration.exchange.domain.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOperation {
    @JsonProperty("type")
    private StockOperationType type;
    @JsonProperty("user")
    private User user;
    @JsonProperty("stock")
    private Stock stock;
    @JsonProperty("amount")
    private long amount;
}
