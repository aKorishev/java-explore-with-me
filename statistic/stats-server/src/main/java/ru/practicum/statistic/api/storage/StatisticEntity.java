package ru.practicum.statistic.api.storage;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "Statistics")
@Data
public class StatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistic_id")
    private long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private Timestamp timestamp;
}
