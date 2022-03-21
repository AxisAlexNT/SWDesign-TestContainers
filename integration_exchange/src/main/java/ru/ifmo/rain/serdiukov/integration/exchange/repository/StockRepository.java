package ru.ifmo.rain.serdiukov.integration.exchange.repository;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @NotNull @NonNull Optional<Stock> findByIndex(final @NotNull @NonNull String index);
}
