package ru.ifmo.rain.serdiukov.integration.exchange.interfaces.dto;

import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperationType;

@Data
public class StockOperationRequestDTO {
    private final @NotNull @NonNull StockOperationType type;
    private final @NotNull @NonNull String userLogin;
    private final @NotNull @NonNull String stockIndex;
    private final long amount;
}
