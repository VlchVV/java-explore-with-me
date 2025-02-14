package ru.practicum.ewm.stats.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@AllArgsConstructor
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false, length = 15)
    private String ip;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
