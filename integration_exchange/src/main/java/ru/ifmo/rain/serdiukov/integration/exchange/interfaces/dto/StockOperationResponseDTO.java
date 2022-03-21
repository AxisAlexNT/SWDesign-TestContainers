package ru.ifmo.rain.serdiukov.integration.exchange.interfaces.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperation;

import java.time.Instant;

@Data
@Builder
public class StockOperationResponseDTO {
    private final @NotNull @NonNull StockOperation operation;
    private final @NotNull @NonNull Instant timestamp;
}
