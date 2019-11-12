package convention.services;

import ar.com.kfgodel.temas.acciones.CrearProximaReunion;
import ar.com.kfgodel.temas.acciones.UsarReunionExistente;
import ar.com.kfgodel.temas.filters.reuniones.AllReunionesUltimaPrimero;
import ar.com.kfgodel.temas.filters.reuniones.ProximaReunion;
import ar.com.kfgodel.temas.filters.reuniones.UltimaReunion;
import convention.persistent.*;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Created by sandro on 21/06/17.
 */
public class ReunionService extends Service<Reunion> {
    @Inject
    MinutaService minutaService;

    @Inject
    TemaGeneralService temaGeneralService;

    @Inject
    private TemaService temaService;

    public ReunionService() {
        setClasePrincipal(Reunion.class);
    }

    public Reunion getProxima() {
        return createOperation()
                .insideATransaction()
                .applying(ProximaReunion.create())
                .applyingResultOf((existente) ->
                        existente.mapOptional(UsarReunionExistente::create)
                                .orElseGet(() -> CrearProximaReunion.create(this))).get();
    }

    public List<Reunion> getAll() {
        return getAll(AllReunionesUltimaPrimero.create());
    }

    @Override
    public void delete(Long id) {
        minutaService.getForReunion(id).ifPresent(minutaService::delete);
        Reunion reunion = get(id);
        reunion.getTemasPropuestos().stream()
                .filter(temaDeReunion -> !temaDeReunion.getEsRePropuesta())
                .forEach(temaDeReunion -> temaService.convertirRePropuestasAPropuestasOriginales(temaDeReunion.getId()));
        super.delete(id);
    }

    public Optional<Reunion> getUltimaReunion() {
        return getAll(UltimaReunion.create()).stream().findFirst();
    }

    public Reunion cargarActionItemsDeLaUltimaMinutaSiExisteElTema(Reunion nuevaReunion) {
        minutaService.getUltimaMinuta().ifPresent(nuevaReunion::cargarSiExisteElTemaParaRepasarActionItemsDe);
        return nuevaReunion;
    }

    public Reunion create(Reunion unaReunion) {
        unaReunion.agregarTemasGenerales(temaGeneralService.getAll());
        return save(unaReunion);
    }

    public List<TemaDeReunion> getTemasDeProximaReunion() {
        return minutaService.getUltimaMinuta().get().getReunion().getTemasPropuestos();
    }
}
