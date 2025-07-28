package com.example.kagari.resource;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class GreetingResource {

    private final SecurityIdentity securityIdentity;

    public GreetingResource(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    @GET
    @Path("/public")
    @Produces(MediaType.TEXT_PLAIN)
    public String publicHello() {
        return "Hello from public endpoint!";
    }

    @GET
    @Path("/secured")
    @Authenticated
    @Produces(MediaType.TEXT_PLAIN)
    public String securedHello() {
        return "Hello from secured endpoint, " + securityIdentity.getPrincipal().getName() + "!";
    }

    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_PLAIN)
    public String adminHello() {
        return "Hello from admin endpoint, " + securityIdentity.getPrincipal().getName() + "!";
    }
}
