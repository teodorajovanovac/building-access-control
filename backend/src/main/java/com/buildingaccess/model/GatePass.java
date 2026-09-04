package com.buildingaccess.model;

import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.GatePassType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gate_passes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatePass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String guestName;

    private String guestPhone;

    private String guestEmail;

    private String reason;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private int maxEntries;

    private int usedEntries = 0;

    @Enumerated(EnumType.STRING)
    private GatePassType type;

    @Enumerated(EnumType.STRING)
    private GatePassStatus status = GatePassStatus.ACTIVE;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /** Denormalizovano iz createdBy.apartment u trenutku kreiranja (v. plan, SK7 upit). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @OneToMany(mappedBy = "gatePass")
    @OrderBy("entryTime DESC")
    private List<EntryLog> entryLogs = new ArrayList<>();

    @OneToMany(mappedBy = "gatePass")
    @OrderBy("changedAt DESC")
    private List<PassStatusHistory> statusHistory = new ArrayList<>();
}
