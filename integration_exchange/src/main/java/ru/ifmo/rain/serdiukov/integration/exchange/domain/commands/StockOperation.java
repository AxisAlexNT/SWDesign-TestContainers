package ru.ifmo.rain.serdiukov.integration.exchange.domain.commands;

import lombok.Builder;
import lombok.Data;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;

@Data
@Builder
public class StockOperation {
    private final StockOperationType type;
    private final User user;
    private final Stock stock;
    private final long amount;
}
