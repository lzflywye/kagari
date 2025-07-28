package com.example.kagari.models;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ServiceEntity extends PanacheEntity {
    @ManyToOne
    public Tenant tenant;
    public String name;
    public String description;
    public BigDecimal price;
    // public int duration_minutes;
    public boolean is_active;
}
