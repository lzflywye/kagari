package com.example.kagari.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class Tenant extends PanacheEntityBase {
    @Id
    public UUID id;
    public String name;
    public String address;
    @Column(name = "phone_number")
    public String phoneNumber;
    public String email;
    @Column(name = "open_time")
    public LocalTime openTime;
    @Column(name = "close_time")
    public LocalTime closeTime;
    @Column(name = "capacity_per_slot")
    public int capacityPerSlot;
    @Column(name = "regularly_closed")
    public String regularlyClosed;
    @Column(columnDefinition = "TEXT")
    public String description;

    public List<DayOfWeek> getRegularlyClosedAsEnum() {
        if (regularlyClosed == null || regularlyClosed.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(regularlyClosed.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }
}
