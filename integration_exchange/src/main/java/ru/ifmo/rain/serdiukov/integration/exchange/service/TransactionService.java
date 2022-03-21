package ru.ifmo.rain.serdiukov.integration.exchange.service;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperation;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperationType;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;
import ru.ifmo.rain.serdiukov.integration.exchange.interfaces.dto.StockOperationResponseDTO;
import ru.ifmo.rain.serdiukov.integration.exchange.repository.StockRepository;
import ru.ifmo.rain.serdiukov.integration.exchange.repository.UserRepository;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final @NotNull @NonNull UserRepository userRepository;
    private final @NotNull @NonNull StockRepository stockRepository;


    public @NotNull @NonNull StockOperationResponseDTO performStockOperation(final @NotNull @NonNull StockOperation operation) {
        final @NotNull @NonNull User user = operation.getUser();
        final @NotNull @NonNull Stock stock = operation.getStock();
        final long amount = operation.getAmount();
        final @NotNull @NonNull StockOperationType type = operation.getType();

        final StockOperationResult result = switch (type) {
            case BUY -> buyStock(user, stock, amount);
            case SELL -> sellStock(user, stock, amount);
            default -> throw new IllegalArgumentException("Unknown operation type: " + type.name());
        };

        return StockOperationResponseDTO
                .builder()
                .operation(StockOperation
                        .builder()
                        .type(type)
                        .amount(amount)
                        .user(result.getUser())
                        .stock(result.getStock())
                        .build()
                )
                .timestamp(Instant.now())
                .build();
    }

    private @NotNull @NonNull StockOperationResult buyStock(final @NotNull @NonNull User user, final @NotNull @NonNull Stock stock, final long requestedAmount) {
        if (requestedAmount <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
        if (stock.getPrice() * requestedAmount > user.getBalance()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot buy %d of stock %s because it requires %d money while only %d is available for user %s",
                    requestedAmount,
                    stock.getIndex(),
                    stock.getPrice() * requestedAmount,
                    user.getBalance(),
                    user.getLogin()
            ));
        }
        if (stock.getAvailableAmount() < requestedAmount) {
            throw new IllegalArgumentException(String.format(
                    "Requested amount of stock %s is not available for purchase, you can request at most %d",
                    stock.getIndex(),
                    stock.getAvailableAmount()
            ));
        }
        stock.setAvailableAmount(stock.getAvailableAmount() - requestedAmount);
        user.getPortfolio().compute(stock, (s, oldAmount) -> (oldAmount != null) ? (oldAmount + requestedAmount) : requestedAmount);
        user.setBalance(user.getBalance() - stock.getPrice() * requestedAmount);
        return new StockOperationResult(userRepository.save(user), stockRepository.save(stock));
    }

    private @NotNull @NonNull StockOperationResult sellStock(final @NotNull @NonNull User user, final @NotNull @NonNull Stock stock, final long requestedSellingAmount) {
        if (requestedSellingAmount <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
        final long availableAmount;
        {
            final Long availableAmountValue = user.getPortfolio().get(stock);
            if (availableAmountValue == null) {
                throw new IllegalArgumentException(String.format(
                        "Cannot sell stock %s because user %s does not own it",
                        stock.getIndex(),
                        user.getLogin()
                ));
            }
            availableAmount = availableAmountValue;
        }

        if (availableAmount < requestedSellingAmount) {
            throw new IllegalArgumentException(String.format(
                    "Cannot sell %d of stock %s because user %s owns only %d of it",
                    requestedSellingAmount,
                    stock.getIndex(),
                    user.getLogin(),
                    availableAmount
            ));
        }

        user.getPortfolio().compute(stock, (s, a) -> {
            final long newAmount = Objects.requireNonNull(a, "Availability of user stock has changed??") - requestedSellingAmount;
            return newAmount > 0 ? newAmount : null;
        });
        user.setBalance(user.getBalance() + stock.getPrice() * requestedSellingAmount);
        stock.setAvailableAmount(stock.getAvailableAmount() + requestedSellingAmount);
        return new StockOperationResult(userRepository.save(user), stockRepository.save(stock));
    }


    @Data
    private static class StockOperationResult {
        final @NotNull @NonNull User user;
        final @NotNull @NonNull Stock stock;
    }

}
