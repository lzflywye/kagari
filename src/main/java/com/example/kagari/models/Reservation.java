package com.example.kagari.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation extends PanacheEntityBase {
    @Id
    public UUID id;
    @ManyToOne
    @JoinColumn(name = "tenant_service_id")
    public TenantService tenantService;
    @Column(name = "reserved_date")
    public LocalDate reservedDate;
    @Column(name = "start_time")
    public LocalTime startTime;
    @Column(name = "customer_name")
    public String customerName;
    @Column(name = "customer_phone")
    public String customerPhone;
    @Column(columnDefinition = "TEXT")
    public String description;
    public String status;
}
