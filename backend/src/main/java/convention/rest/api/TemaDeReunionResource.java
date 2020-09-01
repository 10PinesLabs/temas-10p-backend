package convention.rest.api;

import ar.com.kfgodel.dependencies.api.DependencyInjector;
import ar.com.kfgodel.temas.annotations.PATCH;
import convention.persistent.*;
import convention.rest.api.tos.TemaDeMinutaTo;
import convention.rest.api.tos.TemaDeReunionTo;
import convention.rest.api.tos.TemaEnCreacionTo;
import convention.services.MinutaService;
import convention.services.TemaDeMinutaService;
import convention.services.TemaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

/**
 * Esta clase representa el recurso rest para modificar temas
 * Created by kfgodel on 03/03/15.
 */
@Produces("application/json")
@Consumes("application/json")
public class TemaDeReunionResource {

    @Inject
    TemaService temaService;

    @Inject
    MinutaService minutaService;

    @Inject
    TemaDeMinutaService temaDeMinutaService;

    private ResourceHelper resourceHelper;

    @POST
    public TemaDeReunionTo create(TemaEnCreacionTo newState, @Context SecurityContext securityContext) {
        TemaDeReunionConDescripcion temaCreado = getResourceHelper().convertir(newState, TemaDeReunionConDescripcion.class);
        validarTemaDeReunionConDescripcion(temaCreado);
        temaCreado.setUltimoModificador(getResourceHelper().usuarioActual(securityContext));
        TemaDeReunion nuevoTema = temaService.save(temaCreado);
        return getResourceHelper().convertir(nuevoTema, TemaDeReunionTo.class);
    }

    @PUT
    @Path("/{resourceId}")
    public TemaDeReunionTo update(TemaDeReunionTo newState, @PathParam("resourceId") Long id, @Context SecurityContext securityContext) {
        TemaDeReunionConDescripcion temaCreado = getResourceHelper().convertir(newState, TemaDeReunionConDescripcion.class);
        validarTemaDeReunionConDescripcion(temaCreado);
        temaCreado.setUltimoModificador(getResourceHelper().usuarioActual(securityContext));
        TemaDeReunion nuevoTema = temaService.update(temaCreado);
        return getResourceHelper().convertir(nuevoTema, TemaDeReunionTo.class);
    }

    @GET
    @Path("/{resourceId}")
    public TemaDeReunionTo getSingle(@PathParam("resourceId") Long id) {
        return getResourceHelper().convertir(temaService.get(id), TemaDeReunionTo.class);
    }

    @GET
    @Path("votar/{resourceId}")
    public TemaDeReunionTo votar(@PathParam("resourceId") Long id, @Context SecurityContext securityContext) {

        Usuario usuarioActual = getResourceHelper().usuarioActual(securityContext);

        TemaDeReunion temaVotado = temaService.updateAndMapping(id,
            temaDeReunion -> votarTema(usuarioActual, temaDeReunion));
        return getResourceHelper().convertir(temaVotado, TemaDeReunionTo.class);
    }

    public TemaDeReunion votarTema(Usuario usuarioActual, TemaDeReunion temaDeReunion) {

        long cantidadDeVotos = temaDeReunion.getInteresados().stream()
            .filter(usuario ->
                usuario.getId().equals(usuarioActual.getId())).count();
        if (cantidadDeVotos >= 3) {
            throw new WebApplicationException("excede la cantidad de votos permitidos", Response.Status.CONFLICT);
        }
        try {
            temaDeReunion.agregarInteresado(usuarioActual);
        } catch (Exception exception) {
            throw new WebApplicationException(TemaDeReunion.ERROR_AGREGAR_INTERESADO, Response.Status.CONFLICT);
        }
        return temaDeReunion;
    }

    @GET
    @Path("desvotar/{resourceId}")
    public TemaDeReunionTo desvotar(@PathParam("resourceId") Long id, @Context SecurityContext securityContext) {

        Usuario usuarioActual = getResourceHelper().usuarioActual(securityContext);

        TemaDeReunion temaVotado = temaService.updateAndMapping(id,
            temaDeReunion -> desvotarTema(usuarioActual, temaDeReunion)
        );
        return getResourceHelper().convertir(temaVotado, TemaDeReunionTo.class);
    }

    public TemaDeReunion desvotarTema(Usuario usuarioActual, TemaDeReunion temaDeReunion) {
        long cantidadDeVotos = temaDeReunion.getInteresados().stream()
            .filter(usuario ->
                usuario.getId().equals(usuarioActual.getId())).count();
        if (cantidadDeVotos <= 0) {
            throw new WebApplicationException("el usuario no tiene votos en el tema", Response.Status.CONFLICT);
        }
        temaDeReunion.quitarInteresado(usuarioActual);
        return temaDeReunion;
    }

    @DELETE
    @Path("/{resourceId}")
    public void delete(@PathParam("resourceId") Long id) {
        TemaDeReunion tema = temaService.get(id);
        temaService.convertirRePropuestasAPropuestasOriginales(id);
        temaService.delete(tema);
    }

    @PATCH
    @Path("/{resourceId}/temaDeMinuta")
    public void patchTemaDeMinuta(@PathParam("resourceId") Long id, TemaDeMinutaTo patchRequest) {
        Boolean fueTratado = Optional.ofNullable(patchRequest.getFueTratado())
            .orElseThrow(() -> new WebApplicationException("El request es inválido", Response.Status.BAD_REQUEST));
        TemaDeReunion temaDeReunion = temaService.get(id);
        Minuta minuta = minutaService.getForReunion(temaDeReunion.getReunion().getId())
            .orElseThrow(() -> new WebApplicationException("La reunión no tiene minuta", Response.Status.NOT_FOUND));
        minuta.getTemas().stream()
            .filter(tema -> tema.getTema().equals(temaDeReunion))
            .findFirst()
            .ifPresent(temaDeMinuta -> {
                temaDeMinuta.setFueTratado(fueTratado);
                temaDeMinutaService.update(temaDeMinuta);
            });
    }

    public static TemaDeReunionResource create(DependencyInjector appInjector) {
        TemaDeReunionResource temaDeReunionResource = new TemaDeReunionResource();
        temaDeReunionResource.resourceHelper = ResourceHelper.create(appInjector);
        temaDeReunionResource.getResourceHelper().bindAppInjectorTo(TemaDeReunionResource.class, temaDeReunionResource);
        temaDeReunionResource.temaService = appInjector.createInjected(TemaService.class);
        return temaDeReunionResource;
    }

    public ResourceHelper getResourceHelper() {
        return resourceHelper;
    }

    private void validarTemaDeReunionConDescripcion(TemaDeReunionConDescripcion nuevoTema) {
        verificarQueNoTieneTituloDeTemaParaProponerPinosARoot(nuevoTema);
        verificarQueNoReProponeUnaRePropuesta(nuevoTema);
        verificarQueNoHayOtroTemaEnLaReunionQueTrataLaMismaPropuesta(nuevoTema);
    }

    private void verificarQueNoTieneTituloDeTemaParaProponerPinosARoot(TemaDeReunionConDescripcion unTemaDeReunion) {
        if (unTemaDeReunion.getTitulo().equals(TemaParaProponerPinosARoot.TITULO)) {
            throw new WebApplicationException("No puede haber 2 temas de proponer pino a roots", Response.Status.CONFLICT);
        }
    }

    private void verificarQueNoReProponeUnaRePropuesta(TemaDeReunionConDescripcion unTemaDeReunion) {
        unTemaDeReunion.propuestaOriginal().ifPresent(propuestaOriginal -> {
            if (propuestaOriginal.getEsRePropuesta()) {
                throw new WebApplicationException("No se puede volver a proponer una re-propuesta", Response.Status.BAD_REQUEST);
            }
        });
    }

    private void verificarQueNoHayOtroTemaEnLaReunionQueTrataLaMismaPropuesta(TemaDeReunionConDescripcion unTemaDeReunion) {
        if (unTemaDeReunion.getReunion().tieneOtroTemaQueTrataLaMismaPropuestaQue(unTemaDeReunion)) {
            throw new WebApplicationException("No se puede volver a proponer el mismo tema más de una vez", Response.Status.CONFLICT);
        }
    }
}
