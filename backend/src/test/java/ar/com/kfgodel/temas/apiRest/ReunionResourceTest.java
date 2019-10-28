package ar.com.kfgodel.temas.apiRest;

import convention.persistent.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReunionResourceTest extends ResourceTest {

    @Test
    public void testProponerPinoComoRootAgregaUnTemaALaReunion() throws IOException {
        Long idReunion = persistentHelper.crearUnaReunion().getId();

        HttpResponse response = makeJsonPostRequest("reuniones/" + idReunion + "/propuestas", jsonDeUnaPropuesta());

        Reunion reunionActualizada = reunionService.get(idReunion);
        assertThatResponseStatusCodeIs(response, HttpStatus.SC_OK);
        assertThat(reunionActualizada.getTemasPropuestos()).hasSize(1);
    }

    @Test
    public void testProponerPinoComoRootAgregaUnaPropuestaParaElPino() throws IOException {
        Long idReunion = persistentHelper.crearUnaReunion().getId();

        String unNombre = "nombre";
        makeJsonPostRequest("reuniones/" + idReunion + "/propuestas", jsonDeUnaPropuestaPara(unNombre));

        Reunion reunionActualizada = reunionService.get(idReunion);
        TemaParaProponerPinosARoot temaParaProponerPinos = (TemaParaProponerPinosARoot) reunionActualizada.getTemasPropuestos().get(0);
        List<PropuestaDePinoARoot> propuestasDePino = temaParaProponerPinos.propuestas();

        assertThat(propuestasDePino).hasSize(1);
        assertThat(propuestasDePino).anyMatch(propuesta -> Objects.equals(propuesta.pino(), unNombre));
    }

    @Test
    public void testProponerPinoComoRootResponseConLaReunionActualizada() throws IOException {
        Long idReunion = persistentHelper.crearUnaReunion().getId();

        HttpResponse response = makeJsonPostRequest("reuniones/" + idReunion + "/propuestas", jsonDeUnaPropuesta());

        String responseBody = getResponseBody(response);
        JSONObject responseJson = new JSONObject(responseBody);

        assertThat(responseJson.get("id")).isEqualTo(idReunion.intValue());
        assertThat(responseJson.getJSONArray("temasPropuestos").length()).isEqualTo(1);
    }

    @Test
    public void testGetDeReunionDistingueLosTiposDeTema() throws IOException {
        Reunion unaReunion = persistentHelper.crearUnaReunionConTemasMinuteada();

        HttpResponse response = makeGetRequest("reuniones/" + unaReunion.getId());

        JSONObject responseJson = new JSONObject(getResponseBody(response));
        JSONObject jsonDelTema = responseJson.getJSONArray("temasPropuestos").getJSONObject(0);
        assertThat(jsonDelTema.has("tipo")).isTrue();
    }

    @Test
    public void testGetDeReunionTieneAlTemaParaProponerPinosConLasPropuestas() throws IOException {
        Usuario unSponsor = unUsuarioPersistido();
        Reunion unaReunion = helper.unaReunion();
        String unPino = helper.unPino();
        unaReunion.proponerPinoComoRoot(unPino, unSponsor);
        reunionService.save(unaReunion);

        Long idReunion = unaReunion.getId();
        HttpResponse response = makeGetRequest("reuniones/" + idReunion);

        JSONObject responseJson = new JSONObject(getResponseBody(response));
        JSONObject jsonDelTema = responseJson.getJSONArray("temasPropuestos").getJSONObject(0);
        JSONObject jsonDeLaPropuesta = jsonDelTema.getJSONArray("propuestas").getJSONObject(0);
        assertThat(jsonDeLaPropuesta.getString("pino")).isEqualTo(unPino);
    }

    @Test
    public void alCerrarReunionSeAgreganLosActionItemsSiElTemaExiste() throws IOException {
        Reunion reunionAnterior = persistentHelper.crearUnaReunionConTemasMinuteada();
        ActionItem actionItem = persistentHelper.crearActionItem();
        persistentHelper.crearMinutaConActionItem(reunionAnterior, actionItem);

        Reunion proximaReunion = persistentHelper.crearUnaReunionConTemaParaRellenarActionItems();

        HttpResponse response = makeGetRequest("reuniones/cerrar/" + proximaReunion.getId());

        JSONObject responseJson = new JSONObject(getResponseBody(response));
        JSONObject jsonDelTema = responseJson.getJSONArray("temasPropuestos").getJSONObject(0);
        String tipoDeLaPropuesta = jsonDelTema.getString("tipo");
        JSONObject temaParaRepasar = jsonDelTema.getJSONArray("temasParaRepasar").getJSONObject(0);
        JSONObject actionItemParaRepasar = temaParaRepasar.getJSONArray("actionItems").getJSONObject(0);

        assertThat(tipoDeLaPropuesta).isEqualTo("repasarActionItems");
        assertThat(actionItemParaRepasar.getString("descripcion")).isEqualTo(actionItem.getDescripcion());
    }

    @Test
    public void alCerrarReunionSeSiguenPudiendoLeerLasReuniones() throws IOException {
        Reunion reunionAnterior = persistentHelper.crearUnaReunionConTemasMinuteada();
        ActionItem actionItem = persistentHelper.crearActionItem();
        persistentHelper.crearMinutaConActionItem(reunionAnterior, actionItem);
        Reunion proximaReunion = persistentHelper.crearUnaReunionConTemaParaRellenarActionItems();

        proximaReunion.cerrarVotacion();
        reunionService.gestionarTemasParaLaMinuta(proximaReunion);
        reunionService.save(proximaReunion);

        HttpResponse reuniones = makeGetRequest("reuniones/");
        assertThatResponseStatusCodeIs(reuniones, HttpStatus.SC_OK);
    }


    @Test
    public void alCerrarReunionNoSeAgreganLosActionItemsSiElTemaNoExiste() throws IOException {
        Reunion reunionAnterior = persistentHelper.crearUnaReunionConTemasMinuteada();
        ActionItem actionItem = persistentHelper.crearActionItem();
        persistentHelper.crearMinutaConActionItem(reunionAnterior, actionItem);

        Reunion proximaReunion = persistentHelper.crearUnaReunion();

        Long idReunion = proximaReunion.getId();
        HttpResponse response = makeGetRequest("reuniones/cerrar/" + idReunion);

        JSONObject responseJson = new JSONObject(getResponseBody(response));
        JSONArray temasPropuestos = responseJson.getJSONArray("temasPropuestos");

        assertThat(temasPropuestos.length()).isEqualTo(0);
    }

    @Test
    public void testCuandoSeCreaLaProximaReunionSeLeAgreganLosTemasGenerales() throws IOException {
        TemaGeneral unTemaGeneral = temaGeneralService.save(helper.unTemaGeneral());

        makeGetRequest("reuniones/proxima");

        Reunion proximaReunion = reunionService.getAll().get(0);
        TemaDeReunion unTemaDeReunion = proximaReunion.getTemasPropuestos().get(0);
        assertThat(unTemaDeReunion.getTemaGenerador().get()).isEqualTo(unTemaGeneral);
    }

    @Test
    public void testElGetDeReunionTieneLosIdsDePropuestaOriginalCorrectos() throws IOException {
        TemaDeReunion unaPropuestaOriginal = temaService.save(helper.unTemaDeReunion());
        Reunion unaReunion = reunionService.save(helper.unaReunion());
        temaService.save(helper.unaRePropuestaDeParaReunion(unaPropuestaOriginal, unaReunion));

        HttpResponse response = makeGetRequest("reuniones/" + unaReunion.getId());

        JSONObject jsonResponse = new JSONObject(getResponseBody(response));
        JSONObject jsonDeLaRePropuesta = jsonResponse.getJSONArray("temasPropuestos").getJSONObject(0);
        assertThat(jsonDeLaRePropuesta.getLong("idDePropuestaOriginal")).isEqualTo(unaPropuestaOriginal.getId());
    }

    @Test
    public void testCuandoSeEliminaUnaReunionConUnaPropuestaOriginalSusRePropuestasDejanDeSerRePropuestas() throws IOException {
        Reunion unaReunion = reunionService.save(helper.unaReunion());
        TemaDeReunion unaPropuestaOriginal = temaService.save(
                helper.unTemaDeReunion(unaReunion));
        TemaDeReunion unTema = temaService.save(helper.unaRePropuestaDeParaReunion(
                unaPropuestaOriginal, reunionService.save(helper.unaReunion())));

        HttpResponse response = makeDeleteRequest("reuniones/" + unaReunion.getId());

        assertThatResponseStatusCodeIs(response, HttpStatus.SC_NO_CONTENT);
        assertThat(temaService.getAll()).doesNotContain(unaPropuestaOriginal);
        assertThat(temaService.get(unTema.getId()).getEsRePropuesta()).isFalse();
    }

    private Usuario unUsuarioPersistido() {
        return usuarioService.getAll().get(0);
    }

    private String jsonDeUnaPropuesta() {
        return jsonDeUnaPropuestaPara(helper.unPino());
    }

    private String jsonDeUnaPropuestaPara(String unNombre) {
        return new JSONObject().put("pino", unNombre).toString();
    }
}
