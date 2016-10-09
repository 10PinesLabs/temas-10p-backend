package convention.persistent;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase representa uno de los temas a tratar en un reunion de roots
 * Created by kfgodel on 21/08/16.
 */
@Entity
public class TemaDeReunion extends PersistableSupport {

  @ManyToOne
  private Reunion reunion;
  public static final String reunion_FIELD = "reunion";

  @ManyToOne
  private Usuario autor;
  public static final String autor_FIELD = "autor";

  @Column(length = 1024)
  private String titulo;
  public static final String titulo_FIELD = "titulo";

  @Lob
  private String descripcion;
  public static final String descripcion_FIELD = "descripcion";

  @ManyToMany
  private List<Usuario> interesados;
  public static final String interesados_FIELD = "interesados";

  public Usuario getAutor() {
    return autor;
  }

  public void setAutor(Usuario autor) {
    this.autor = autor;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Reunion getReunion() {
    return reunion;
  }

  public void setReunion(Reunion reunion) {
    this.reunion = reunion;
  }

  public List<Usuario> getInteresados() {
    if (interesados == null) {
      interesados = new ArrayList<>();
    }
    return interesados;
  }

  public void setInteresados(List<Usuario> interesados) {
    getInteresados().clear();
    if (interesados != null) {
      getInteresados().addAll(interesados);
    }
  }

  public void agregarInteresado(Usuario votante) {
    this.getInteresados().add(votante);
  }

  public void quitarInteresado(Usuario votante) {
    this.getInteresados().remove(votante);
  }
}
