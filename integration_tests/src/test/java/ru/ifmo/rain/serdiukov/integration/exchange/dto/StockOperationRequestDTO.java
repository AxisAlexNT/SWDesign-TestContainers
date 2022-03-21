package ru.ifmo.rain.serdiukov.integration.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOperationRequestDTO {
    @JsonProperty("type")
    private StockOperationType type;
    @JsonProperty("userLogin")
    private String userLogin;
    @JsonProperty("stockIndex")
    private String stockIndex;
    @JsonProperty("amount")
    private long amount;
}
