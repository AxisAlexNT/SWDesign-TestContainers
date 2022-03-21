package ru.ifmo.rain.serdiukov.integration.exchange.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    @JsonProperty("id")
    private long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("index")
    private String index;
    @JsonProperty("price")
    private long price;
    @JsonProperty("availableAmount")
    private long availableAmount;
}
