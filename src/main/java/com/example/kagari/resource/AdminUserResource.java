package com.example.kagari.resource;

import java.util.List;
import java.util.UUID;

import com.example.kagari.models.TenantUser;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/users")
public class AdminUserResource {

    @Inject
    Template userList;

    @Inject
    Template userDetail;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("admin")
    public TemplateInstance getUserList() {
        List<TenantUser> users = TenantUser.listAll();
        return userList.data("users", users);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUserDetail(@PathParam("id") UUID id) {
        TenantUser user = TenantUser.findById(id);
        return userDetail.data("user", user);
    }

    // @POST
    // @Path("/{id}")
    // public
}
