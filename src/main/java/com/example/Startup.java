package com.example;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import com.example.kagari.models.User;

import io.quarkus.runtime.StartupEvent;

@Singleton
public class Startup {
    @Transactional
    public void loadUsers(@Observes StartupEvent evt) {
        // User.deleteAll();
        if (User.count() == 0) {
            User.add("admin", "admin", "admin");
            User.add("alice", "alice", "user");
        }
    }
}
