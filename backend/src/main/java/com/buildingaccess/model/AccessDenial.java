package com.buildingaccess.model;

import com.buildingaccess.model.enums.DenialReasonType;
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
@Table(name = "access_denials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessDenial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String enteredCode;

    private String personName;

    private LocalDateTime attemptTime;

    @Enumerated(EnumType.STRING)
    private DenialReasonType reasonType;

    /** Slobodan tekst — obavezan samo za MANUAL_DENIAL (validacija u service sloju). */
    private String reasonNote;

    /** Nullable — pokušaj sa nepostojećim kodom nema vezanu propusnicu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_pass_id")
    private GatePass gatePass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = false)
    private User processedBy;
}
