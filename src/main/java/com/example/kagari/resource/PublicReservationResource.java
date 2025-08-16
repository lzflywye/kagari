package com.example.kagari.resource;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.kagari.models.Reservation;
import com.example.kagari.models.Service;
import com.fasterxml.uuid.Generators;
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

@Path("/services")
@Produces(MediaType.TEXT_HTML)
public class PublicReservationResource {

    @Inject
    Template customerInfoForm;
    @Inject
    Template reservationConfirm;
    @Inject
    Template reservationMessage;
    @Inject
    Template serviceList;
    @Inject
    Template timeSlotSelection;

    private final LocalTime OPEN_TIME = LocalTime.of(10, 0, 0);
    private final LocalTime CLOSED_TIME = LocalTime.of(18, 0, 0);
    private final int SLOT_DURATION_MINUTES = 60;
    private final int CAPACITY = 5;
    private final Set<DayOfWeek> REGULARLY_CLOSED = Set.of(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY);

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    public TemplateInstance getServiceList() {

        List<Service> services = Service.listAll();
        return serviceList.data("services", services);
    }

    @GET
    @Path("/{serviceId}/select-time")
    public TemplateInstance showTimeSlotSelection(@jakarta.ws.rs.PathParam("serviceId") UUID serviceId) {
        Service service = Service.findById(serviceId);
        if (service == null) {
            return reservationMessage.data("message", "指定されたサービスが見つかりません。");
        }

        List<LocalDate> dates = new ArrayList<>();
        List<String> columnHeaders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = today.plusDays(i);
            dates.add(currentDate);
            columnHeaders.add(currentDate.format(DateTimeFormatter.ofPattern("d")));
        }

        List<LocalTime> times = new ArrayList<>();
        LocalTime currentTime = OPEN_TIME;
        while (currentTime.plusMinutes(SLOT_DURATION_MINUTES).isBefore(CLOSED_TIME.plusMinutes(1))) {
            times.add(currentTime);
            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
        }

        Map<LocalDate, Map<LocalTime, Long>> reservedCountsByDateTime = new HashMap<>();
        for (LocalDate date : dates) {
            List<Reservation> reservationsForDate = Reservation
                    .find("service = ?1 AND reservedDate = ?2 AND status IN ('pending', 'confirmed', 'completed')",
                            service,
                            date)
                    .list();
            Map<LocalTime, Long> countsForTime = reservationsForDate.stream()
                    .collect(Collectors.groupingBy(res -> res.startTime, Collectors.counting()));
            reservedCountsByDateTime.put(date, countsForTime);
        }

        List<ReservationRow> rows = new ArrayList<>();
        boolean hasAnyAvailableSlots = false;

        for (LocalTime timeHeader : times) {
            List<ReservationCell> rowCells = new ArrayList<>();
            for (LocalDate dateHeader : dates) {
                boolean isAvailable;

                DayOfWeek dayOfWeek = dateHeader.getDayOfWeek();
                boolean isDayOff = REGULARLY_CLOSED.contains(dayOfWeek);

                boolean isWithningOperatingHours = !(timeHeader.isBefore(OPEN_TIME)
                        || timeHeader.plusMinutes(SLOT_DURATION_MINUTES).isAfter(CLOSED_TIME.plusMinutes(1)));

                if (isDayOff || !isWithningOperatingHours) {
                    isAvailable = false;
                } else {
                    long reservedCountInSlot = reservedCountsByDateTime.getOrDefault(dateHeader, new HashMap<>())
                            .getOrDefault(timeHeader, 0L);

                    if (reservedCountInSlot < CAPACITY) {
                        isAvailable = true;
                        hasAnyAvailableSlots = true;
                    } else {
                        isAvailable = false;
                    }
                }
                rowCells.add(new ReservationCell(dateHeader, timeHeader, isAvailable));
            }
            rows.add(new ReservationRow(timeHeader, rowCells));
        }

        ReservationTableData reservationTableData = new ReservationTableData(columnHeaders, rows);

        return timeSlotSelection
                .data("service", service)
                .data("reservationTableData", reservationTableData)
                .data("hasAnyAvailableSlots", hasAnyAvailableSlots);
    }

    @POST
    @Path("/customer-info")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance showCustomerInfoForm(
            @FormParam("serviceId") UUID serviceId,
            @FormParam("reservedDate") String reservedDateStr,
            @FormParam("startTime") String startTimeStr,
            @FormParam("customerName") String customerName,
            @FormParam("customerPhone") String customerPhone,
            @FormParam("description") String description) {

        Service service = Service.findById(serviceId);
        if (service == null) {
            return reservationMessage.data("message", "指定されたサービスが見つかりません。");
        }

        LocalDate reservedDate = LocalDate.parse(reservedDateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);

        if (!isSlotAvailable(service, reservedDate, startTime)) {
            return reservationMessage.data("message", "選択された時間枠は既に埋まっているか、利用できません。お手数ですが、もう一度時間を選択してください。");
        }

        return customerInfoForm
                .data("service", service)
                .data("reservedDate", reservedDate.toString())
                .data("startTime", startTime.toString())
                .data("customerName", customerName)
                .data("customerPhone", customerPhone)
                .data("description", description);
    }

    @POST
    @Path("/review")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance reviewReservation(
            @FormParam("serviceId") UUID serviceId,
            @FormParam("reservedDate") String reservedDateStr,
            @FormParam("startTime") String startTimeStr,
            @FormParam("customerName") String customerName,
            @FormParam("customerPhone") String customerPhone,
            @FormParam("description") String description) {

        Service service = Service.findById(serviceId);
        if (service == null) {
            return reservationMessage.data("message", "指定されたサービスが見つかりません。");
        }

        LocalDate reservedDate = LocalDate.parse(reservedDateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);

        if (!isSlotAvailable(service, reservedDate, startTime)) {
            return reservationMessage.data("message", "選択された時間枠は既に埋まっているか、利用できません。お手数ですが、もう一度時間を選択してください。");
        }

        return reservationConfirm
                .data("service", service)
                .data("reservedDate", reservedDate.toString())
                .data("startTime", startTime.toString())
                .data("customerName", customerName)
                .data("customerPhone", customerPhone)
                .data("description", description);
    }

    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response confirmReservation(
            @FormParam("serviceId") UUID serviceId,
            @FormParam("reservedDate") String reservedDateStr,
            @FormParam("startTime") String startTimeStr,
            @FormParam("customerName") String customerName,
            @FormParam("customerPhone") String customerPhone,
            @FormParam("customerEmail") String customerEmail,
            @FormParam("description") String description) {

        Service service = Service.findById(serviceId);
        if (service == null) {
            return Response.seeOther(URI.create("/services/error?message=ServiceNotFound")).build();
        }

        LocalDate reservedDate = LocalDate.parse(reservedDateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);

        if (!isSlotAvailable(service, reservedDate, startTime)) {
            return Response.seeOther(URI.create("/services/error?message=SlotNotAvailable")).build();
        }

        Reservation reservation = new Reservation();
        reservation.id = Generators.timeBasedEpochGenerator().generate();
        reservation.service = service;
        reservation.reservedDate = reservedDate;
        reservation.startTime = startTime;
        reservation.customerName = customerName;
        reservation.customerPhone = customerPhone;
        reservation.description = description;
        reservation.status = "pending";

        reservation.persist();

        return Response.seeOther(URI.create("/services/success")).build();
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

    private boolean isSlotAvailable(Service service, LocalDate date, LocalTime startTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (REGULARLY_CLOSED.contains(dayOfWeek)) {
            return false;
        }

        if (startTime.isBefore(OPEN_TIME)
                || startTime.plusMinutes(SLOT_DURATION_MINUTES).isAfter(CLOSED_TIME.plusMinutes(1))) {
            return false;
        }

        long reservedCountInSlot = Reservation.find(
                "service = ?1 AND reservedDate = ?2 AND startTime = ?3 AND status IN ('pending', 'confirmed', 'completed')",
                service, date, startTime).count();

        return reservedCountInSlot < CAPACITY;
    }

    public static class SlotAvailability {
        public LocalDate date;
        public List<LocalTime> availableTimes;

        public SlotAvailability(LocalDate date, List<LocalTime> availableTimes) {
            this.date = date;
            this.availableTimes = availableTimes;
        }
    }

    public static class ReservationCell {
        public String date;
        public String time;
        public boolean isAvailable;

        public ReservationCell(LocalDate date, LocalTime time, boolean isAvailable) {
            this.date = date.toString();
            this.time = time.toString();
            this.isAvailable = isAvailable;
        }
    }

    public static class ReservationRow {
        public String rowHeaderTime;
        public List<ReservationCell> cells;

        public ReservationRow(LocalTime rowHeaderTime, List<ReservationCell> cells) {
            this.rowHeaderTime = rowHeaderTime.toString();
            this.cells = cells;
        }
    }

    public static class ReservationTableData {
        public List<String> columnHeaders;
        public List<ReservationRow> rows;

        public ReservationTableData(List<String> columnHeaders, List<ReservationRow> rows) {
            this.columnHeaders = columnHeaders;
            this.rows = rows;
        }
    }
}
