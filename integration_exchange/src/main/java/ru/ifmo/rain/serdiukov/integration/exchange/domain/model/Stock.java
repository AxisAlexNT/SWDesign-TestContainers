package ru.ifmo.rain.serdiukov.integration.exchange.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "index", nullable = false, unique = true)
    private String index;

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "availableAmount", nullable = false)
    private long availableAmount;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Stock stock = (Stock) o;
        return id == stock.id;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
