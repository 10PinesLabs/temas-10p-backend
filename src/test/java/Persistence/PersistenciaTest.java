package Persistence;

import ar.com.kfgodel.temas.application.Application;
import ar.com.kfgodel.temas.filters.reuniones.AllReunionesUltimaPrimero;
import convention.persistent.*;
import convention.services.ReunionService;
import convention.services.TemaGeneralService;
import convention.services.TemaService;
import helpers.TestConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sandro on 19/06/17.
 */
public class PersistenciaTest {

    Application application;
    ReunionService reunionService;
    TemaService temaService;
    private TemaGeneralService temaGeneralService;

    @Before
    public void setUp(){
        startApplication();
        reunionService = application.getInjector().createInjected(ReunionService.class);
        temaService = application.getInjector().createInjected(TemaService.class);
        temaGeneralService = application.getInjector().createInjected(TemaGeneralService.class);

        application.getInjector().bindTo(ReunionService.class, reunionService);
        application.getInjector().bindTo(TemaService.class, temaService);
        application.getInjector().bindTo(TemaGeneralService.class, temaGeneralService);
    }
    @After
    public void drop(){
        application.stop();
    }
    @Test
    public void test01SePuedePersistirCorrectamenteUnaReunion(){
        Reunion nuevaReunion = new Reunion();

        int cantidadDeReunionesAnteriores = reunionService.getAll(AllReunionesUltimaPrimero.create()).size();

        reunionService.save(nuevaReunion);

        List<Reunion> reunionesPersistidas = reunionService.getAll(AllReunionesUltimaPrimero.create());

        Assert.assertEquals(cantidadDeReunionesAnteriores + 1, reunionesPersistidas.size());
    }

    @Test
    public void test02SePuedePersistirCorrectamenteUnTema(){
        TemaDeReunion nuevoTema = new TemaDeReunion();

        int cantidadDeTemasAnteriores = temaService.getAll().size();

        temaService.save(nuevoTema);

        List<TemaDeReunion> temasPersistidos = temaService.getAll();

        Assert.assertEquals(cantidadDeTemasAnteriores + 1, temasPersistidos.size());
    }

    @Test
    public void test03AlObtenerUnaReunionSeTraenSoloSusTemas(){
       Reunion reunion = new Reunion();
       TemaDeReunion temaDeLaReunion = new TemaDeReunion();
       TemaDeReunion temaDeOtraReunion = new TemaDeReunion();

       reunion = reunionService.save(reunion);

       temaDeLaReunion.setReunion(reunion);


       reunion.setTemasPropuestos(Arrays.asList(temaDeLaReunion));

       reunion = reunionService.save(reunion);


       Reunion reunionPersistida = reunionService.get(reunion.getId());

       Assert.assertEquals(1, reunionPersistida.getTemasPropuestos().size());

    }

    @Test
    public void test04LaObligatoriedadDeUnTemaSePersisteCorrectamente(){
        TemaDeReunion tema = new TemaDeReunion();
        tema.setObligatoriedad(ObligatoriedadDeTema.OBLIGATORIO);

        tema = temaService.save(tema);

        TemaDeReunion temaPersistido = temaService.get(tema.getId());
        Assert.assertEquals(ObligatoriedadDeTema.OBLIGATORIO, temaPersistido.getObligatoriedad());
    }

    @Test
    public void test05ElMomentoDeCreacionDeUnTemaSeCreaAlPersistirElTema(){
        TemaDeReunion tema = new TemaDeReunion();
        tema = temaService.save(tema);
        TemaDeReunion temaPersistido = temaService.get(tema.getId());
        Assert.assertFalse(temaPersistido.getMomentoDeCreacion() == null);
    }

    @Test
    public void test06SePuedePersistirCorrectamenteUnTemaGeneral(){
        TemaGeneral temaGeneral = new TemaGeneral();
        String titulo = "Tema General";
        temaGeneral.setTitulo(titulo);
        temaGeneral = temaGeneralService.save(temaGeneral);
        Assert.assertEquals(titulo, temaGeneralService.get(temaGeneral.getId()).getTitulo());
    }

    @Test
    public void test07AlGuardarUnaReunionSeAgreganLosTemasGeneralesCorrespondientes(){
        TemaGeneral temaGeneral = new TemaGeneral();
        temaGeneralService.save(temaGeneral);
        Reunion reunion = Reunion.create(LocalDate.of(2017, 06, 26));

        reunion = reunionService.save(reunion);

        Assert.assertEquals(1, reunionService.get(reunion.getId()).getTemasPropuestos().size());
    }

    @Test
    public void test08AlGuardarUnTemaGeneralElMismoSeAgregaATodasLasReunionesAbiertas(){
        Reunion reunionAbierta = Reunion.create(LocalDate.of(2017, 06, 26));
        reunionAbierta = reunionService.save(reunionAbierta);

        TemaGeneral temaGeneral = new TemaGeneral();
        temaGeneralService.save(temaGeneral);

        Assert.assertEquals(1, reunionService.get(reunionAbierta.getId()).getTemasPropuestos().size());
    }

    @Test
    public void test09AlGuardarUnaTemaGeneralElMismoNoSeAgregaALasReunionesCerradas(){
        Reunion reunionCerrada = Reunion.create(LocalDate.of(2017, 06, 26));
        reunionCerrada.setStatus(StatusDeReunion.CERRADA);
        reunionCerrada = reunionService.save(reunionCerrada);

        TemaGeneral temaGeneral = new TemaGeneral();
        temaGeneralService.save(temaGeneral);

        Assert.assertEquals(0, reunionService.get(reunionCerrada.getId()).getTemasPropuestos().size());
    }

    @Test
    public void test10SePuedePersistirSiUnTemaDeReunionFueGeneradoPorUnTemaGeneral(){
        Reunion reunion = new Reunion();
        reunionService.save(reunion);
        TemaGeneral temaGeneral = new TemaGeneral();
        TemaDeReunion temaDeReunion = temaGeneral.generarTemaPara(reunion);
        temaDeReunion = temaService.save(temaDeReunion);

        Assert.assertTrue(temaService.get(temaDeReunion.getId()).fueGeneradoPorUnTemaGeneral());
    }

    private void startApplication(){
        application = TestApplication.create(TestConfig.create());
        application.start();
    }
}
