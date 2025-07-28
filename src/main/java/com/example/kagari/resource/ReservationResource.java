package com.example.kagari.resource;

import java.util.List;

import com.example.kagari.models.Reservation;
import com.example.kagari.models.UserTenant;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/reservations")
@Produces(MediaType.TEXT_HTML)
public class ReservationResource {

    @Inject
    Template reservationList;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Authenticated
    public TemplateInstance listAll() {
        Long tenantId = getLoggedInUser().tenant.id;

        List<Reservation> reservations = Reservation.list("service.tenant.id",
                tenantId);
        // List<Reservation> reservations = Reservation.listAll();

        return reservationList.data("reservations", reservations);
    }

    @GET
    @Path("/{id}")
    @Authenticated
    public TemplateInstance getReservationDetails(@PathParam("id") Long id) {
        Long tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return reservationList.data("error", "Reservation not found.");
        }

        if (!reservation.service.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to this reservation.",
                    Response.Status.FORBIDDEN);
        }

        return reservationList.data("reservation", reservation);
    }

    @POST
    @Path("/{id}/confirm")
    @Transactional
    @Authenticated
    public Response confirmReservation(@PathParam("id") Long id) {
        Long tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!reservation.service.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to confirm this reservation.",
                    Response.Status.FORBIDDEN);
        }

        reservation.status = "confirmed";
        reservation.persist();
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/cancel")
    @Transactional
    @Authenticated
    public Response cancellReservation(@PathParam("id") Long id) {
        Long tenantId = getLoggedInUser().tenant.id;

        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!reservation.service.tenant.id.equals(tenantId)) {
            throw new WebApplicationException("Forbidden: You do not have access to confirm this reservation.",
                    Response.Status.FORBIDDEN);
        }

        reservation.status = "canceled";
        reservation.persist();
        return Response.ok().build();
    }

    private UserTenant getLoggedInUser() {
        if (securityIdentity == null || securityIdentity.getPrincipal() == null) {
            throw new WebApplicationException("Unauthorized: No security context available.",
                    Response.Status.UNAUTHORIZED);
        }
        String username = securityIdentity.getPrincipal().getName();
        UserTenant userTenant = UserTenant.findByUsername(username);
        if (userTenant == null || userTenant.tenant == null) {
            throw new WebApplicationException("Forbidden: User or tenant not found.",
                    Response.Status.FORBIDDEN);
        }
        return userTenant;
    }
}
