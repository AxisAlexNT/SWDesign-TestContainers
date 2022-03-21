package ru.ifmo.rain.serdiukov.integration.exchange.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;
import ru.ifmo.rain.serdiukov.integration.exchange.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public @NotNull @NonNull Optional<User> findByLogin(final @NotNull @NonNull String login) {
        return userRepository.findByLogin(login);
    }

    public void validateUser(final @NotNull @NonNull User user) {
        if (Strings.isBlank(user.getLogin())) {
            throw new IllegalArgumentException("Login cannot be blank");
        }
        if (Strings.isBlank(user.getName())) {
            throw new IllegalArgumentException("Name should be filled");
        }
    }

    public @NotNull @NonNull User registerUser(final @NotNull @NonNull User user) {
        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Login %s is already occupied",
                            user.getLogin()
                    )
            );
        } else {
            validateUser(user);
            return userRepository.save(user);
        }
    }

    public @NotNull @NonNull User topUpBalance(final @NotNull @NonNull String userLogin, final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
        final @NotNull @NonNull User user = findByLogin(userLogin).orElseThrow(() -> new NoSuchElementException("Cannot find user with login = " + userLogin));
        user.setBalance(user.getBalance() + amount);
        return userRepository.save(user);
    }

    public long getTotalUserActivesPrice(final @NotNull @NonNull String userLogin) {
        final @NotNull @NonNull User user = findByLogin(userLogin).orElseThrow(() -> new NoSuchElementException("Cannot find user with login = " + userLogin));
        final long totalStockPrice = user
                .getPortfolio()
                .entrySet()
                .stream()
                .mapToLong(e -> e.getKey().getPrice() * e.getValue())
                .reduce(Long::sum)
                .orElse(0L); // If no stock was owned by this user
        return user.getBalance() + totalStockPrice;
    }
}
