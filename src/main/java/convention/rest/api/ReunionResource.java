package convention.rest.api;

import ar.com.kfgodel.appbyconvention.operation.api.ApplicationOperation;
import ar.com.kfgodel.dependencies.api.DependencyInjector;
import ar.com.kfgodel.diamond.api.types.reference.ReferenceOf;
import ar.com.kfgodel.orm.api.operations.basic.Delete;
import ar.com.kfgodel.orm.api.operations.basic.FindById;
import ar.com.kfgodel.orm.api.operations.basic.Save;
import ar.com.kfgodel.temas.acciones.CrearProximaReunion;
import ar.com.kfgodel.temas.acciones.UsarExistente;
import ar.com.kfgodel.temas.filters.reuniones.AllReunionesUltimaPrimero;
import ar.com.kfgodel.temas.filters.reuniones.ProximaReunion;
import ar.com.kfgodel.webbyconvention.impl.auth.adapters.JettyIdentityAdapter;
import com.sun.org.apache.regexp.internal.RE;
import convention.persistent.Reunion;
import convention.persistent.StatusDeReunion;
import convention.persistent.TemaDeReunion;
import convention.rest.api.tos.ReunionTo;
import convention.services.ReunionService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This type is the resource API for users
 * Created by kfgodel on 03/03/15.
 */
@Produces("application/json")
@Consumes("application/json")
public class ReunionResource extends Resource {

  @Inject
  private ReunionService reunionService;

  private static final Type LISTA_DE_REUNIONES_TO = new ReferenceOf<List<ReunionTo>>() {
  }.getReferencedType();

    private static final Type LISTA_DE_REUNIONES = new ReferenceOf<List<Reunion>>() {
    }.getReferencedType();
    private Reunion filtrarVotosDeReunionPendiente(Reunion reunion, Long userId){

        if(reunion.getStatus() == StatusDeReunion.PENDIENTE) {
            Reunion nuevaReunion = reunion.copy();
            List<TemaDeReunion> listaDeTemasNuevos = reunion.getTemasPropuestos().stream().
                    map(temaDeReunion ->
                            temaDeReunion.copy()).collect(Collectors.toList());
            listaDeTemasNuevos.forEach(temaDeReunion -> temaDeReunion.ocultarVotosPara(userId));
            nuevaReunion.setTemasPropuestos(listaDeTemasNuevos);
            return nuevaReunion;
        }
        else{
            return reunion;
        }
    }
  @GET
  @Path("proxima")
  public ReunionTo getProxima() {
    return conversor(reunionService.getProxima(),Reunion.class,ReunionTo.class);

  }

  @GET
  @Path("cerrar/{resourceId}")
  public ReunionTo cerrar(@PathParam("resourceId") Long id) {
    Reunion reunionCerrada=reunionService.update(id,
            reunion ->{reunion.cerrarVotacion();
                        return reunion;});
      return conversor(reunionCerrada,Reunion.class,ReunionTo.class);
  }

  @GET
  @Path("reabrir/{resourceId}")
  public ReunionTo reabrir(@PathParam("resourceId") Long id) {
    Reunion reunionAbierta=reunionService.update(id,
            reunion -> {reunion.reabrirVotacion();
                        return reunion;});
   return conversor(reunionAbierta,Reunion.class,ReunionTo.class);
  }

  @GET
  public List<ReunionTo> getAll(@Context SecurityContext securityContext) {
      Long userId = idDeUsuarioActual(securityContext);
      List<Reunion> reuniones=reunionService.getAll();
      List<Reunion> reunionesFiltradas= reuniones.stream()
      .map(reunion -> filtrarVotosDeReunionPendiente(reunion,userId)).collect(Collectors.toList());
        return conversor(reunionesFiltradas,LISTA_DE_REUNIONES,LISTA_DE_REUNIONES_TO);

    }

  @POST
  public ReunionTo create(ReunionTo reunionNueva) {

      Reunion reunionCreada=reunionService.save(conversor(reunionNueva,ReunionTo.class,Reunion.class));
      return conversor(reunionCreada,Reunion.class,ReunionTo.class);
  }

  @GET
  @Path("/{resourceId}")
  public ReunionTo getSingle(@PathParam("resourceId") Long id, @Context SecurityContext securityContext) {
        Long userId = idDeUsuarioActual(securityContext);
       Reunion reunionFiltrada= reunionService.get(id,reunion -> filtrarVotosDeReunionPendiente(reunion,userId));
      return conversor(reunionFiltrada,Reunion.class,ReunionTo.class);

  }


  @PUT
  @Path("/{resourceId}")
  public ReunionTo update(ReunionTo newState, @PathParam("resourceId") Long id) {
    Reunion reunionActualizada= reunionService.update(conversor(newState,ReunionTo.class,Reunion.class));
      return conversor(reunionActualizada,Reunion.class,ReunionTo.class);
  }

  @DELETE
  @Path("/{resourceId}")
  public void delete(@PathParam("resourceId") Long id) {
    reunionService.delete(id);
  }

  public static ReunionResource create(DependencyInjector appInjector) {
    ReunionResource resource = new ReunionResource();
    resource.appInjector = appInjector;
    resource.reunionService = resource.appInjector.createInjected(ReunionService.class);
    return resource;
  }


}
