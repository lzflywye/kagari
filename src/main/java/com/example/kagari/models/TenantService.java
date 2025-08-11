package com.example.kagari.models;

import java.math.BigDecimal;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant_services")
public class TenantService extends PanacheEntityBase {
    @Id
    public UUID id;
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    public Tenant tenant;
    public String name;
    @Column(columnDefinition = "TEXT")
    public String description;
    public BigDecimal price;
    @Column(name = "is_active")
    public boolean isActive;
}
