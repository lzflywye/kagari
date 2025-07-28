package com.example;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.kagari.models.Reservation;
import com.example.kagari.models.ServiceEntity;
import com.example.kagari.models.Tenant;
import com.example.kagari.models.User;
import com.example.kagari.models.UserTenant;

import io.quarkus.runtime.StartupEvent;

@Singleton
public class Startup {
    @Transactional
    public void loadUsers(@Observes StartupEvent evt) {
        Reservation.deleteAll();
        ServiceEntity.deleteAll();
        User.deleteAll();
        Tenant.deleteAll();

        User.add("admin", "admin", "admin");
        User.add("alice", "alice", "user");

        Tenant testTenant = new Tenant();
        testTenant.name = "Test Tenant";
        testTenant.adress = "Test City";
        testTenant.phoneNumber = "XXX-XXXX-XXXX";
        testTenant.email = "test@example.com";
        testTenant.openTime = LocalTime.of(10, 0);
        testTenant.closeTime = LocalTime.of(18, 0);
        testTenant.capacityPerSlot = 1;
        testTenant.regularlyClosed = "SATURDAY, SUNDAY";
        testTenant.description = "";
        testTenant.persist();

        User alice = User.find("username = ?1", "alice").firstResult();
        UserTenant aliceTenant = new UserTenant();
        aliceTenant.user = alice;
        aliceTenant.tenant = testTenant;
        aliceTenant.persist();

        ServiceEntity testService = new ServiceEntity();
        testService.name = "Test Service";
        testService.description = "";
        testService.price = new BigDecimal(1000);
        testService.is_active = true;
        testService.tenant = testTenant;
        testService.persist();
    }
}
