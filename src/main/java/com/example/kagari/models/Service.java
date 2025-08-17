package com.example.kagari.models;

import java.math.BigDecimal;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class Service extends PanacheEntityBase {
    @Id
    public UUID id;
    public String name;
    public BigDecimal price;
    @Column(columnDefinition = "TEXT")
    public String description;
    @Column(name = "is_active")
    public boolean isActive;
}
