package ru.ifmo.rain.serdiukov.integration.exchange.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    @Column(name = "portfolio", nullable = false)
    private Map<Stock, Long> portfolio;

    @Column(name = "balance", nullable = false)
    private long balance;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
