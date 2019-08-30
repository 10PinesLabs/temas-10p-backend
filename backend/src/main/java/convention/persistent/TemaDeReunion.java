package convention.persistent;

import ar.com.kfgodel.temas.exceptions.TemaDeReunionException;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Esta clase representa uno de los temas a tratar en un reunion de roots
 * Created by kfgodel on 21/08/16.
 */
@Entity
public abstract class TemaDeReunion extends Tema {

    public static final String reunion_FIELD = "reunion";
    public static final String prioridad_FIELD = "prioridad";
    public static final String interesados_FIELD = "interesados";
    public static final String obligatoriedad_FIELD = "obligatoriedad";
    public static final String temaGenerador_FIELD = "temaGenerador";
    public static final String primeraPropuesta_FIELD = "primeraPropuesta";
    public static final String ERROR_AGREGAR_INTERESADO = "No se puede agregar un interesado a un tema obligatorio";
    @ManyToOne
    private Reunion reunion;
    private Integer prioridad;
    @Fetch(org.hibernate.annotations.FetchMode.SELECT)
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Usuario> interesados;
    @Enumerated(EnumType.STRING)
    private ObligatoriedadDeTema obligatoriedad;
    @ManyToOne
    private TemaGeneral temaGenerador;
    @ManyToOne
    private TemaDeReunion primeraPropuesta;

    public ObligatoriedadDeTema getObligatoriedad() {
        return obligatoriedad;
    }

    public void setObligatoriedad(ObligatoriedadDeTema unaObligatoriedad) {
        this.obligatoriedad = unaObligatoriedad;
    }

    public Reunion getReunion() {
        return reunion;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
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
        if (this.puedeSerVotado())
            this.getInteresados().add(votante);
        else
            throw new TemaDeReunionException(TemaDeReunion.ERROR_AGREGAR_INTERESADO);
    }

    public void quitarInteresado(Usuario votante) {
        this.getInteresados().remove(votante);
    }

    public int getCantidadDeVotos() {
        return getInteresados().size();
    }


    public TemaDeReunion copy() {
        TemaDeReunion copia = createCopy();
        copia.setInteresados(this.getInteresados());
        copia.setPersistenceVersion(this.getPersistenceVersion());
        copia.setMomentoDeUltimaModificacion(this.getMomentoDeUltimaModificacion());
        copia.setMomentoDeCreacion(this.getMomentoDeCreacion());
        copia.setId(this.getId());
        copia.setTitulo(this.getTitulo());
        copia.setDescripcion(this.getDescripcion());
        copia.setReunion(this.getReunion());
        copia.setPrioridad(this.getPrioridad());
        copia.setAutor(this.getAutor());
        copia.setDuracion(this.getDuracion());
        copia.setObligatoriedad(this.getObligatoriedad());
        copia.setUltimoModificador(this.getUltimoModificador());
        copia.setPrimeraPropuesta(this.getPrimeraPropuesta());
        return copia;
    }

    protected abstract TemaDeReunion createCopy();


    public Boolean tieneMayorPrioridadQue(TemaDeReunion otroTema) {
        Integer prioridad = getObligatoriedad().prioridad();
        Integer otraPrioridad = otroTema.getObligatoriedad().prioridad();

        Integer cantidadDeVotos = this.getCantidadDeVotos();
        Integer otraCantidadDeVotos = otroTema.getCantidadDeVotos();

        if (prioridad.equals(otraPrioridad)
                && getObligatoriedad().permiteRecibirVotos()
                && cantidadDeVotos != otraCantidadDeVotos)
            return cantidadDeVotos > otraCantidadDeVotos;

        if (otroTema.fueGeneradoPorUnTemaGeneral() ^ this.fueGeneradoPorUnTemaGeneral()) //^ -> XOR
            return this.fueGeneradoPorUnTemaGeneral();

        if (prioridad.equals(otraPrioridad))
            return otroTema.seCreoDespuesDe(this);

        return prioridad < otraPrioridad;
    }

    protected Boolean seCreoDespuesDe(TemaDeReunion otroTema) {
        return this.getMomentoDeCreacion().isAfter(otroTema.getMomentoDeCreacion());
    }

    public void ocultarVotosPara(Long userId) {
        this.setInteresados(this.getInteresados()
                .stream()
                .filter(usuario -> usuario.getId().equals(userId))
                .collect(Collectors.toList()));
    }

    public Boolean puedeSerVotado() {
        return getObligatoriedad().permiteRecibirVotos();
    }

    public Boolean fueGeneradoPorUnTemaGeneral() {
        return this.getObligatoriedad().equals(ObligatoriedadDeTema.OBLIGATORIO) && getTemaGenerador().isPresent();
    }

    public Boolean fueModificado() {
        TemaGeneral temaGenerador = getTemaGenerador().orElseThrow(() -> new TemaDeReunionException("no tiene tema generador"));
        return sonDiferentes(temaGenerador, Tema::getDescripcion)
                || sonDiferentes(temaGenerador, Tema::getTitulo)
                || sonDiferentes(temaGenerador, Tema::getDuracion)
                || getObligatoriedad() != ObligatoriedadDeTema.OBLIGATORIO;
    }

    private boolean sonDiferentes(TemaGeneral temaGenerador, Function<Tema, Object> f) {
        return !Objects.equals(f.apply(this), f.apply(temaGenerador));
    }

    public Optional<TemaGeneral> getTemaGenerador() {
        return Optional.ofNullable(temaGenerador);
    }

    public void setTemaGenerador(TemaGeneral temaGenerador) {
        this.temaGenerador = temaGenerador;
    }

    public Boolean esParaProponerPinosARoot() {
        return false;
    }

    public Boolean esParaRepasarActionItems() {
        return false;
    }

    public void setPrimeraPropuesta(TemaDeReunion unTemaDeReunion) {
        primeraPropuesta = unTemaDeReunion;
    }

    public TemaDeReunion getPrimeraPropuesta() {
        return Optional.ofNullable(primeraPropuesta).orElse(this);
    }

    public Boolean esRePropuesta() {
        return !Objects.equals(getPrimeraPropuesta(), this);
    }

    public Boolean reProponeElMismoTemaQue(TemaDeReunion otroTema) {
        return Objects.equals(getPrimeraPropuesta(), otroTema.getPrimeraPropuesta());
    }

    public LocalDate getFechaDePrimeraPropuesta() {
        return getPrimeraPropuesta().getFechaDeReunion();
    }

    private LocalDate getFechaDeReunion() {
        return getReunion().getFecha();
    }
}
