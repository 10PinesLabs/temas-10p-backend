package convention.rest.api.tos;

import ar.com.kfgodel.appbyconvention.tos.PersistableToSupport;
import convention.persistent.Reunion;
import net.sf.kfgodel.bean2bean.annotations.CopyFrom;

import java.util.List;

/**
 * Esta clase representa el TO de una reunion para ser editada
 * Created by kfgodel on 08/10/16.
 */
public class ReunionTo extends PersistableToSupport {

  @CopyFrom(Reunion.fecha_FIELD)
  private String fecha;

  @CopyFrom(Reunion.temasPropuestos_FIELD)
  private List<TemaTo> temasPropuestos;

  public String getFecha() {
    return fecha;
  }

  public void setFecha(String fecha) {
    this.fecha = fecha;
  }

  public List<TemaTo> getTemasPropuestos() {
    return temasPropuestos;
  }

  public void setTemasPropuestos(List<TemaTo> temasPropuestos) {
    this.temasPropuestos = temasPropuestos;
  }
}
