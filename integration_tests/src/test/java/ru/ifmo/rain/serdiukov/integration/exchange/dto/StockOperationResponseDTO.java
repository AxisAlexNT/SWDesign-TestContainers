package ru.ifmo.rain.serdiukov.integration.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperation;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOperationResponseDTO {
    @JsonProperty("operation")
    private @NotNull @NonNull StockOperation operation;
    @JsonProperty("timestamp")
    private @NotNull @NonNull Instant timestamp;
}
