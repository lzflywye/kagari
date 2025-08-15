package com.example.kagari.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.example.kagari.models.Reservation;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/reservations")
@Authenticated
public class AdminReservationResource {

    @Inject
    Template reservationDetail;
    @Inject
    Template reservationList;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listAll() {

        List<Reservation> reservations = Reservation.listAll();

        return reservationList.data("reservations", reservations);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getReservationDetails(@PathParam("id") UUID id) {

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return reservationList.data("error", "Reservation not found.");
        }

        return reservationDetail.data("reservation", reservation);
    }

    @POST
    @Path("/{id}/confirm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response confirmReservation(@PathParam("id") UUID id) {

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        reservation.status = "confirmed";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }

    @POST
    @Path("/{id}/complete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response completeReservation(@PathParam("id") UUID id) {

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        reservation.status = "completed";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }

    @POST
    @Path("/{id}/cancel")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response cancellReservation(@PathParam("id") UUID id) {

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        reservation.status = "cancelled";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }
}
