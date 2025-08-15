package com.example.kagari.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.example.kagari.models.Reservation;
import com.example.kagari.models.TenantUser;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/reservations")
public class AdminReservationResource {

    @Inject
    Template reservationDetail;
    @Inject
    Template reservationList;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Authenticated
    public TemplateInstance listAll() {
        UUID tenantId = getLoggedInUser().tenant.id;

        List<Reservation> reservations = Reservation.list("tenantService.tenant.id",
                tenantId);

        return reservationList.data("reservations", reservations);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}")
    @Authenticated
    public TemplateInstance getReservationDetails(@PathParam("id") UUID id) {
        UUID tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return reservationList.data("error", "Reservation not found.");
        }

        if (!reservation.tenantService.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to this reservation.",
                    Response.Status.FORBIDDEN);
        }

        return reservationDetail.data("reservation", reservation);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/confirm")
    @Transactional
    @Authenticated
    public Response confirmReservation(@PathParam("id") UUID id) {
        UUID tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!reservation.tenantService.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to confirm this reservation.",
                    Response.Status.FORBIDDEN);
        }

        reservation.status = "confirmed";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/complete")
    @Transactional
    @Authenticated
    public Response completeReservation(@PathParam("id") UUID id) {
        UUID tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!reservation.tenantService.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to confirm this reservation.",
                    Response.Status.FORBIDDEN);
        }

        reservation.status = "completed";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/cancel")
    @Transactional
    @Authenticated
    public Response cancellReservation(@PathParam("id") UUID id) {
        UUID tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!reservation.tenantService.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to confirm this reservation.",
                    Response.Status.FORBIDDEN);
        }

        reservation.status = "cancelled";
        reservation.persist();
        return Response.seeOther(URI.create("/admin/reservations")).build();
    }

    private TenantUser getLoggedInUser() {
        if (securityIdentity == null || securityIdentity.getPrincipal() == null) {
            throw new WebApplicationException("Unauthorized: No security context available.",
                    Response.Status.UNAUTHORIZED);
        }
        String username = securityIdentity.getPrincipal().getName();
        TenantUser user = TenantUser.find("username", username).firstResult();
        if (user == null || user.tenant == null) {
            throw new WebApplicationException("Forbidden: User or tenant not found.",
                    Response.Status.FORBIDDEN);
        }
        return user;
    }
}
