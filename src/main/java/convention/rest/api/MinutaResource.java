package convention.rest.api;

import ar.com.kfgodel.dependencies.api.DependencyInjector;
import com.google.inject.Inject;
import convention.persistent.Minuta;
import convention.rest.api.tos.MinutaTo;
import convention.services.MinutaService;
import convention.services.ReunionService;

import javax.ws.rs.*;

/**
 * Created by sandro on 07/07/17.
 */
@Produces("application/json")
@Consumes("application/json")
public class MinutaResource extends Resource {

    @Inject
    private MinutaService minutaService;

    @Inject
    private ReunionService reunionService;

    @GET
    @Path("/{resourceId}")
    public MinutaTo getSingle(@PathParam("resourceId") Long id) {
        return convertir(minutaService.get(id), MinutaTo.class);
    }

    @GET
    @Path("reunion/{reunionId}")
    public MinutaTo getParaReunion(@PathParam("reunionId") Long id){
        Minuta minuta = minutaService.getFromReunion(id);
        return convertir(minuta, MinutaTo.class);
    }
    public static MinutaResource create(DependencyInjector appInjector) {
        MinutaResource resource = new MinutaResource();
        resource.appInjector = appInjector;
        resource.appInjector.bindTo(MinutaResource.class, resource);
        return resource;
    }
}
