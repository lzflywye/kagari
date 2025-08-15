package com.example.kagari.resource;

import java.net.URI;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class AuthResource {

    @Inject
    CurrentIdentityAssociation identity;

    @POST
    @Path("/logout")
    public Response logout() {
        if (identity.getIdentity().isAnonymous()) {
            throw new UnauthorizedException("Not authenticated");
        }
        FormAuthenticationMechanism.logout(identity.getIdentity());
        return Response.seeOther(URI.create("/login.html")).build();
    }
}
