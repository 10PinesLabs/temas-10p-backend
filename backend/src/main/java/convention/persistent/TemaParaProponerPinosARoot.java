package convention.persistent;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TemaParaProponerPinosARoot extends TemaDeReunion {

    public static final String PINO_YA_PROPUESTO_ERROR_MSG = "el pino ya fue propuesto";
    public static final String TITULO = "Proponer pinos a root";

    public static final String propuestas_FIELD = "propuestas";
    public static final DuracionDeTema DURACION = DuracionDeTema.CORTO;
    public static final ObligatoriedadDeTema OBLIGATORIEDAD = ObligatoriedadDeTema.NO_OBLIGATORIO;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PropuestaDePinoARoot> propuestas = new ArrayList<>();

    public static TemaParaProponerPinosARoot create(Usuario unAutor) {
        TemaParaProponerPinosARoot tema = new TemaParaProponerPinosARoot();
        tema.setAutor(unAutor);
        return tema;
    }

    public List<PropuestaDePinoARoot> propuestas() {
        return propuestas;
    }

    public void agregarPropuesta(PropuestaDePinoARoot unaPropuesta) {
        verificarQueNoHayaSidoPropuesto(unaPropuesta.pino());
        propuestas.add(unaPropuesta);
    }

    private void verificarQueNoHayaSidoPropuesto(String unPino) {
        if (fuePropuesto(unPino)) {
            throw new RuntimeException(PINO_YA_PROPUESTO_ERROR_MSG);
        }
    }

    private Boolean fuePropuesto(String unPino) {
        return propuestas().stream().anyMatch(propuesta -> propuesta.pino().equals(unPino));
    }

    public String getTitulo() {
        return TITULO;
    }

    public ObligatoriedadDeTema getObligatoriedad() {
        return OBLIGATORIEDAD;
    }

    @Override
    public TemaDeReunion copy() {
        TemaParaProponerPinosARoot copia = (TemaParaProponerPinosARoot) super.copy();
        propuestas().forEach(copia::agregarPropuesta);
        return copia;
    }

    @Override
    protected TemaDeReunion createCopy() {
        return new TemaParaProponerPinosARoot();
    }

    public DuracionDeTema getDuracion() {
        return DURACION;
    }

    public Boolean esParaProponerPinosARoot() {
        return true;
    }
}
