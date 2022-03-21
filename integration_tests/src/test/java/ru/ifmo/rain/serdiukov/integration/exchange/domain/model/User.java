package ru.ifmo.rain.serdiukov.integration.exchange.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import ru.ifmo.rain.serdiukov.integration.exchange.util.ArrayToMapDeserializer;

import java.util.Map;

@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @JsonProperty("id")
    private long id;
    @JsonProperty("login")
    private String login;
    @JsonProperty("name")
    private String name;
    @JsonProperty("portfolio")
    @JsonDeserialize(using = ArrayToMapDeserializer.class,
            keyAs = Stock.class, contentAs = Long.class)
    private Map<Stock, Long> portfolio;
    @JsonProperty("balance")
    private long balance;
}
