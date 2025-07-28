package com.example.kagari.resource;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.example.kagari.models.Reservation;
import com.example.kagari.models.ServiceEntity;
import com.example.kagari.models.Tenant;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/public/reservations")
@Produces(MediaType.TEXT_HTML)
public class PublicReservationResource {

    @Inject
    Template tenantList;
    @Inject
    Template serviceList;
    @Inject
    Template reservationForm;
    @Inject
    Template reservationConfirm;
    @Inject
    Template reservationMessage;

    private final int SLOT_DURATION_MINUTES = 60;

    private static final Logger LOG = Logger.getLogger(PublicReservationResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showTenantList() {
        LOG.info("tenantList template instance: " + tenantList);
        List<Tenant> tenants = Tenant.listAll();
        return tenantList.data("tenants", tenants);
    }

    @GET
    @Path("/tenant/{tenantId}/services")
    public TemplateInstance showServiceList(@jakarta.ws.rs.PathParam("tenantId") Long tenantId) {
        Tenant tenant = Tenant.findById(tenantId);
        if (tenant == null) {
            return reservationMessage.data("message", "指定されたテナントが見つかりません。");
        }
        List<ServiceEntity> services = ServiceEntity.list("tenant.id", tenantId);
        return serviceList
                .data("tenant", tenant)
                .data("services", services);
    }

    @GET
    @Path("/service/{serviceId}/slots")
    public TemplateInstance showAvailableSlots(@jakarta.ws.rs.PathParam("serviceId") Long serviceId) {
        ServiceEntity service = ServiceEntity.findById(serviceId);
        if (service == null) {
            return reservationMessage.data("message", "指定されたサービスが見つかりません。");
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            dates.add(today.plusDays(i));
        }

        List<SlotAvailability> slotAvailabilities = new ArrayList<>();
        for (LocalDate date : dates) {
            List<LocalTime> availableTimes = getAvailableTimeSlots(service, date);
            slotAvailabilities.add(new SlotAvailability(date, availableTimes));
        }

        return reservationForm
                .data("service", service)
                .data("slotAvailabilities", slotAvailabilities)
                .data("today", today);
    }

    @POST
    @Path("/review")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance reviewReservation(
            @FormParam("serviceId") Long serviceId,
            @FormParam("reservedDate") String reservedDateStr,
            @FormParam("startTime") String startTimeStr,
            @FormParam("customerName") String customerName,
            @FormParam("customerPhone") String customerPhone,
            @FormParam("comments") String comments) {

        ServiceEntity service = ServiceEntity.findById(serviceId);
        if (service == null) {
            return reservationMessage.data("message", "指定されたサービスが見つかりません。");
        }

        LocalDate reservedDate = LocalDate.parse(reservedDateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);

        if (!isSlotAvailable(service, reservedDate, startTime)) {
            return reservationMessage.data("message", "指定した時間に空きがなくなったため、ご予約が完了できませんでした。");
        }

        return reservationConfirm
                .data("service", service)
                .data("reservedDate", reservedDate)
                .data("startTime", startTime)
                .data("customerName", customerName)
                .data("customerPhone", customerPhone)
                .data("comments", comments);
    }

    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response confirmReservation(
            @FormParam("serviceId") Long serviceId,
            @FormParam("reservedDate") String reservedDateStr,
            @FormParam("startTime") String startTimeStr,
            @FormParam("customerName") String customerName,
            @FormParam("customerPhone") String customerPhone,
            @FormParam("customerEmail") String customerEmail,
            @FormParam("comments") String comments) {

        ServiceEntity service = ServiceEntity.findById(serviceId);
        if (service == null) {
            return Response.seeOther(URI.create("/public/reservations/error?message=ServiceNotFound")).build();
        }

        LocalDate reservedDate = LocalDate.parse(reservedDateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);

        if (!isSlotAvailable(service, reservedDate, startTime)) {
            return Response.seeOther(URI.create("/public/reservations/error?message=SlotNotAvailable")).build();
        }

        Reservation reservation = new Reservation();
        reservation.service = service;
        reservation.reservedDate = reservedDate;
        reservation.startTime = startTime;
        reservation.customerName = customerName;
        reservation.customerPhone = customerPhone;
        reservation.comments = comments;
        reservation.status = "pending";

        reservation.persist();

        return Response.seeOther(URI.create("/public/reservations/success")).build();
    }

    @GET
    @Path("/success")
    public TemplateInstance reservationSuccess() {
        return reservationMessage.data("message", "ご予約が完了しました。");
    }

    @GET
    @Path("/error")
    public TemplateInstance errorPage(@QueryParam("message") String message) {
        String info;
        if (message == null) {
            info = "不明なエラーが発生しました。";
        } else if (message.equals("SlotNotAvailable")) {
            info = "指定した時間に空きがなくなったため、ご予約が完了できませんでした。";
        } else if (message.equals("ServiceNotFound")) {
            info = "指定されたサービスが見つかりません。";
        } else {
            info = message;
        }
        return reservationMessage.data("message", info);
    }

    private List<LocalTime> getAvailableTimeSlots(ServiceEntity service, LocalDate date) {
        Tenant tenant = service.tenant;
        if (tenant == null) {
            return List.of();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (tenant.getRegularlyClosedAsEnum().contains(dayOfWeek)) {
            return List.of();
        }

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime current = tenant.openTime;
        LocalTime endOfDay = tenant.closeTime;

        while (current.plusMinutes(SLOT_DURATION_MINUTES).isBefore(endOfDay.plusMinutes(1))) {
            allPossibleSlots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        List<Reservation> existingReservations = Reservation
                .find("service = ?1 AND reservedDate = ?2 AND status IN ('pending', 'confirmed')", service, date)
                .list();

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slot : allPossibleSlots) {
            int reservedCountInSlot = 0;
            for (Reservation res : existingReservations) {
                if (res.startTime.equals(slot)) {
                    reservedCountInSlot++;
                }
            }

            if (reservedCountInSlot < tenant.capacityPerSlot) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }

    private boolean isSlotAvailable(ServiceEntity service, LocalDate date, LocalTime startTime) {
        Tenant tenant = service.tenant;

        if (tenant == null) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (tenant.getRegularlyClosedAsEnum().contains(dayOfWeek)) {
            return false;
        }

        if (startTime.isBefore(tenant.openTime)
                || startTime.plusMinutes(SLOT_DURATION_MINUTES).isAfter(tenant.closeTime.plusMinutes(1))) {
            return false;
        }

        long reservedCountInSlot = Reservation.find(
                "service = ?1 AND reservedDate = ?2 AND startTime = ?3 AND status IN ('pending', 'confirmed')",
                service, date, startTime).count();

        return reservedCountInSlot < tenant.capacityPerSlot;
    }

    public static class SlotAvailability {
        public LocalDate date;
        public List<LocalTime> availableTimes;

        public SlotAvailability(LocalDate date, List<LocalTime> availableTimes) {
            this.date = date;
            this.availableTimes = availableTimes;
        }
    }
}
