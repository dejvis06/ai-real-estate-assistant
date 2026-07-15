package com.reservation.reservation.infrastructure.persistence.entity;

import com.reservation.reservation.domain.model.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
public class ReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reservationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 200)
    private String customerName;

    @Column(nullable = false, length = 200)
    private String customerEmail;

    @Column(length = 50)
    private String customerPhone;

    private LocalDateTime viewingDateTime;
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String agentNotes;

    @Column(columnDefinition = "TEXT")
    private String customerMessage;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReservationNumber() { return reservationNumber; }
    public void setReservationNumber(String reservationNumber) { this.reservationNumber = reservationNumber; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public LocalDateTime getViewingDateTime() { return viewingDateTime; }
    public void setViewingDateTime(LocalDateTime viewingDateTime) { this.viewingDateTime = viewingDateTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getAgentNotes() { return agentNotes; }
    public void setAgentNotes(String agentNotes) { this.agentNotes = agentNotes; }
    public String getCustomerMessage() { return customerMessage; }
    public void setCustomerMessage(String customerMessage) { this.customerMessage = customerMessage; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
