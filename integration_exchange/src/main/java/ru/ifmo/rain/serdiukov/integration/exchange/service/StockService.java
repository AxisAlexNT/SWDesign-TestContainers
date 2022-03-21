package ru.ifmo.rain.serdiukov.integration.exchange.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.repository.StockRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    public @NotNull @NonNull Optional<Stock> findByIndex(final @NotNull @NonNull String index) {
        return stockRepository.findByIndex(index);
    }

    private void validateStock(final @NotNull @NonNull Stock stock) {
        if (stock.getAvailableAmount() < 0) {
            throw new IllegalArgumentException("Stock amount might not be negative");
        }

        if (stock.getPrice() <= 0) {
            throw new IllegalArgumentException("Stock price must be positive");
        }
    }

    public @NotNull @NonNull Stock createStock(final @NotNull @NonNull Stock stock) {
        if (stockRepository.findByIndex(stock.getIndex()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Stock with index %s is already present",
                            stock.getIndex()
                    )
            );
        } else {
            validateStock(stock);
            return stockRepository.save(stock);
        }
    }

    public Stock updateStockPrice(final @NotNull @NonNull String stockIndex, final long newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("Stock price should be positive");
        }
        final @NotNull @NonNull Stock stock = findByIndex(stockIndex).orElseThrow(() -> new NoSuchElementException("Cannot find stock with index = " + stockIndex));
        stock.setPrice(newPrice);
        return stockRepository.save(stock);
    }

    public Stock increaseStockAmount(final @NotNull @NonNull String stockIndex, final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Cannot decrease stock amount");
        }
        final @NotNull @NonNull Stock stock = findByIndex(stockIndex).orElseThrow(() -> new NoSuchElementException("Cannot find stock with index = " + stockIndex));
        stock.setAvailableAmount(stock.getAvailableAmount() + amount);
        return stockRepository.save(stock);
    }
}
