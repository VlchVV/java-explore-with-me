package ru.practicum.ewm.comments;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.events.Event;
import ru.practicum.ewm.users.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @Column(nullable = false)
    private LocalDateTime created;

    private LocalDateTime edited;
}


