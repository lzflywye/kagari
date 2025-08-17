package com.example.kagari.resource;

import com.example.kagari.models.User;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Authenticated
@Path("/")
public class AuthResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    Template logout;

    @Inject
    Template changePassword;

    @Inject
    Template changePasswordSuccess;

    @GET
    @Path("/logout")
    public TemplateInstance logoutPage() {
        return logout.instance();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        if (securityIdentity.isAnonymous()) {
            throw new UnauthorizedException("Not authenticated");
        }
        FormAuthenticationMechanism.logout(securityIdentity);
        return Response.seeOther(UriBuilder.fromPath("/login.html").build()).build();
    }

    @GET
    @Path("/change-password")
    @Consumes(MediaType.TEXT_HTML)
    public TemplateInstance getChangePasswordPage(@QueryParam("error") String error) {
        return changePassword.data("error", error);
    }

    @GET
    @Path("/change-password-success")
    @Consumes(MediaType.TEXT_HTML)
    public TemplateInstance getChangePasswordSuccessPage() {
        return changePasswordSuccess.instance();
    }

    @POST
    @Path("/change-password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response changePassword(
            @FormParam("currentPassword") String currentPassword,
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword) {

        User user = User.find("username", securityIdentity.getPrincipal().getName()).firstResult();
        if (user == null) {
            return Response.seeOther(
                    UriBuilder.fromPath("/change-password").queryParam("error", "incorrect_password").build())
                    .build();
        }

        if (!BcryptUtil.matches(currentPassword, user.password)) {
            return Response.seeOther(
                    UriBuilder.fromPath("/change-password").queryParam("error", "incorrect_password").build())
                    .build();
        }

        if (!verifyPassword(newPassword)) {
            return Response.seeOther(UriBuilder.fromPath("/change-password")
                    .queryParam("error", "invalid_password").build()).build();
        }

        if (!newPassword.equals(confirmPassword)) {
            return Response.seeOther(UriBuilder.fromPath("/change-password")
                    .queryParam("error", "password_mismatch").build()).build();
        }

        user.password = BcryptUtil.bcryptHash(newPassword);
        user.persist();

        return Response.seeOther(UriBuilder.fromPath("/change-password-success").build()).build();
    }

    private boolean verifyPassword(String password) {
        return password.length() >= 8 && password.length() <= 255
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*");
    }
}
