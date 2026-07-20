package com.github.kazikw.boisgo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserver_id", nullable = false)
    private User reserver;

    @Column(nullable = false)
    @ToString.Include
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    @ToString.Include
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "required_participants", nullable = false)
    @Builder.Default
    private int requiredParticipants = 1;

    @Column(name = "allow_join", nullable = false)
    @Builder.Default
    private boolean allowJoin = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reservation_participants",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();
}