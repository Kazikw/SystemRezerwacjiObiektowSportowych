package com.github.kazikw.boisgo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "facility_blockades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FacilityBlockade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "start_date", nullable = false)
    @ToString.Include
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @ToString.Include
    private LocalDate endDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private User createdByAdmin;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}