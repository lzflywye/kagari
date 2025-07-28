package com.example.kagari.models;

import java.time.LocalDate;
import java.time.LocalTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Reservation extends PanacheEntity {
    @ManyToOne
    public ServiceEntity service;
    public LocalDate reservedDate;
    public LocalTime startTime;
    public String customerName;
    public String customerPhone;
    public String comments;
    public String status;
}
