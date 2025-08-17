package com.example.kagari.resource;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/services")
public class AdminServiceResource {
    @Inject
    Template adminServiceList;

    @Inject
    Template adminServiceDetail;

    @Inject
    Template adminServiceAdd;

    @Inject
    Template adminServiceEdit;

    @Inject
    Template adminServiceConfirm;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getServiceList() {
        List<Service> services = Service.listAll();
        return adminServiceList.data("services", services);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getServiceDetail(@PathParam("id") UUID id) {
        Service service = Service.findById(id);
        return adminServiceDetail.data("service", service);
    }

    @GET
    @Path("/add")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getServiceAdd() {
        return adminServiceAdd.instance();
    }

    @POST
    @Path("/confirm")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance postServiceAddConfirm(@FormParam("name") String name, @FormParam("price") String price,
            @FormParam("description") String description) {

        return adminServiceConfirm.data("name", name).data("price", price).data("description", description)
                .data("back_url", "/admin/services/add").data("submit_url", "/admin/services/save");
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response postServiceCreate(@FormParam("name") String name, @FormParam("price") String price,
            @FormParam("description") String description) {

        try {
            Service service = new Service();
            service.id = Generators.timeBasedEpochGenerator().generate();
            service.name = name;
            service.price = new BigDecimal(price);
            service.description = description;
            service.persist();

            return Response.seeOther(URI.create("/admin/services")).build();
        } catch (NumberFormatException error) {
            return Response.seeOther(URI.create("/admin/services/add")).build();
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getServiceEdit(@PathParam("id") UUID id) {

        Service service = Service.findById(id);
        return adminServiceEdit.data("service", service);
    }

    @POST
    @Path("/{id}/confirm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance postServiceEditConfirm(@PathParam("id") UUID id, @FormParam("name") String name,
            @FormParam("price") String price,
            @FormParam("description") String description) {

        return adminServiceConfirm.data("name", name).data("price", price).data("description", description)
                .data("back_url", String.format("/admin/services/%s/edit", id))
                .data("submit_url", String.format("/admin/services/%s/update", id));
    }

    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response postServiceUpdate(@PathParam("id") UUID id, @FormParam("name") String name,
            @FormParam("price") String price,
            @FormParam("description") String description) {

        try {
            Service service = Service.findById(id);
            service.name = name;
            service.price = new BigDecimal(price);
            service.description = description;
            service.persist();

            return Response.seeOther(URI.create("/admin/services")).build();
        } catch (NumberFormatException error) {
            return Response.seeOther(URI.create("/admin/services")).build();
        }
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    public Response postServiceDelete(@PathParam("id") UUID id) {

        Service service = Service.findById(id);
        service.delete();

        return Response.seeOther(URI.create("/admin/services")).build();
    }
}
