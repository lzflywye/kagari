package com.example.kagari.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Tenant extends PanacheEntity {
    public String name;
    public String adress;
    public String phoneNumber;
    public String email;
    public LocalTime openTime;
    public LocalTime closeTime;
    public int capacityPerSlot;
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
