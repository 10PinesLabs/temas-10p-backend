package convention.rest.api.tos;

import ar.com.kfgodel.appbyconvention.tos.PersistableToSupport;
import convention.persistent.TemaDeMinuta;
import convention.persistent.TemaDeReunion;
import net.sf.kfgodel.bean2bean.annotations.CopyFromAndTo;

import java.util.List;

/**
 * Created by fede on 07/07/17.
 */
public class TemaDeMinutaTo extends PersistableToSupport {

    @CopyFromAndTo(TemaDeMinuta.minuta_FIELD)
    private Long idDeMinuta;

    @CopyFromAndTo(TemaDeMinuta.tema_FIELD)
    private TemaTo tema;

    @CopyFromAndTo(TemaDeMinuta.conclusion_FIELD)
    private String conclusion;

    public TemaTo getTema() {
        return tema;
    }

    public void setTema(TemaTo tema) {
        this.tema = tema;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Long getIdDeMinuta() {
        return idDeMinuta;
    }

    public void setIdDeMinuta(Long idDeMinuta) {
        this.idDeMinuta = idDeMinuta;
    }
}