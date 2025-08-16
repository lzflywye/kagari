package com.example.kagari.resource;

import java.util.List;
import java.util.UUID;

import com.example.kagari.models.User;

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
@RolesAllowed("admin")
public class AdminUserResource {

    @Inject
    Template adminUserList;

    @Inject
    Template adminUserDetail;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAdminUserList() {
        List<User> users = User.listAll();
        return adminUserList.data("users", users);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUserDetail(@PathParam("id") UUID id) {
        User user = User.findById(id);
        return adminUserDetail.data("user", user);
    }

    // @POST
    // @Path("/{id}")
    // public
}
