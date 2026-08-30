package com.buildingaccess.model;

import com.buildingaccess.model.enums.PersonType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PersonType personType;

    /** Snapshot imena u trenutku evidentiranja (gost/stanar/osoblje). */
    private String personName;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @Builder.Default
    private boolean manualEntry = false;

    private String note;

    /** Samo za GUEST. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_pass_id")
    private GatePass gatePass;

    /** Samo za RESIDENT. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Samo za STAFF. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_badge_id")
    private StaffBadge staffBadge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    /** Obezbeđenje koje je obradilo/skeniralo dolazak. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = false)
    private User processedBy;
}
