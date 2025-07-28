package com.example.kagari.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class UserTenant extends PanacheEntity {
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    public User user;

    @ManyToOne
    public Tenant tenant;

    public static UserTenant findByUsername(String username) {
        User user = User.find("username", username).firstResult();
        if (user != null) {
            return findById(user.id); // AuthUser の ID で User を検索
        }
        return null;
    }
}
