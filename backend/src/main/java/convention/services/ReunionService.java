package convention.services;

import ar.com.kfgodel.temas.acciones.CrearProximaReunion;
import ar.com.kfgodel.temas.acciones.UsarReunionExistente;
import ar.com.kfgodel.temas.filters.reuniones.AllReunionesUltimaPrimero;
import ar.com.kfgodel.temas.filters.reuniones.ProximaReunion;
import ar.com.kfgodel.temas.filters.reuniones.UltimaReunion;
import convention.persistent.Minuta;
import convention.persistent.Reunion;

import javax.inject.Inject;
import java.util.List;


/**
 * Created by sandro on 21/06/17.
 */
public class ReunionService extends Service<Reunion> {
  @Inject
  MinutaService minutaService;

  @Inject
  TemaGeneralService temaGeneralService;

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
    Minuta minuta = minutaService.getFromReunion(id);
    minutaService.delete(minuta.getId());
    super.delete(id);
  }

  public Reunion getUltimaReunion() {
    return getAll(UltimaReunion.create()).stream().findFirst().get();
  }

  public Reunion cargarActionItemsDeLaUltimaMinutaSiExisteElTema(Reunion nuevaReunion){
    Minuta ultimaMinuta = minutaService.getUltimaMinuta();
    nuevaReunion.cargarSiExisteElTemaParaRepasarActionItemsDe(ultimaMinuta);
    return nuevaReunion;
  }
}
