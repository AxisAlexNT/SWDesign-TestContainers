package ru.ifmo.rain.serdiukov.integration.exchange.repository;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @NotNull @NonNull Optional<User> findByLogin(final @NotNull @NonNull String login);
}
