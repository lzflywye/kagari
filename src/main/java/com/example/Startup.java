package com.example;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        if (User.count() == 0) {
            // Reservation.deleteAll();
            // ServiceEntity.deleteAll();
            // User.deleteAll();
            // Tenant.deleteAll();

            User.add("admin", "admin", "admin");
            User.add("user", "user", "user");

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

            User user = User.find("username = ?1", "user").firstResult();
            UserTenant userTenant = new UserTenant();
            userTenant.user = user;
            userTenant.tenant = testTenant;
            userTenant.persist();

            ServiceEntity testService = new ServiceEntity();
            testService.name = "Test Service";
            testService.description = "";
            testService.price = new BigDecimal(1000);
            testService.is_active = true;
            testService.tenant = testTenant;
            testService.persist();

            Reservation testReservation = new Reservation();
            testReservation.service = testService;
            testReservation.customerName = "Test Customer";
            testReservation.customerPhone = "XXXXXXXXXXX";
            testReservation.reservedDate = LocalDate.now().plusDays(2);
            testReservation.startTime = LocalTime.of(14, 0);
            testReservation.status = "pending";
            testReservation.comments = "";
            testReservation.persist();
        }
    }
}
