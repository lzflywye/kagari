package com.example.kagari.resource;

import java.util.List;
import java.util.UUID;

import com.example.kagari.models.Service;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/services")
public class AdminServiceResource {
    @Inject
    Template adminServiceList;

    @Inject
    Template adminServiceDetail;

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
}
