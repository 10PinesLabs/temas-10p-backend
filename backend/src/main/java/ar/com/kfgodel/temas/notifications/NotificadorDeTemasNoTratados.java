package ar.com.kfgodel.temas.notifications;

import ar.com.kfgodel.dependencies.api.DependencyInjector;
import ar.com.kfgodel.temas.acciones.CalculadorDeFechaDeNotificacion;
import com.google.inject.Inject;
import convention.persistent.Minuta;
import convention.persistent.TemaDeMinuta;
import convention.persistent.TemaDeReunion;
import convention.persistent.Usuario;
import convention.services.MinutaService;
import convention.services.TemaDeMinutaService;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotificadorDeTemasNoTratados {

    private static final String SENDER_ADDRESS = MailerConfiguration.getSenderAdress();
    private static final String SENDER_NAME = "Aviso Tema No Propuesto";

    @Inject
    private MinutaService minutaService;
    @Inject
    private TemaDeMinutaService temaDeMinutaService;

    private Clock clock;
    private Mailer mailer;

    public NotificadorDeTemasNoTratados() {
    }

    public static NotificadorDeTemasNoTratados create(DependencyInjector unInjector, Clock unClock, Mailer unMailer) {
        NotificadorDeTemasNoTratados notificador = unInjector.createInjected(NotificadorDeTemasNoTratados.class);
        notificador.setClock(unClock);
        notificador.setMailer(unMailer);
        return notificador;
    }

    public void notificar() {
        Minuta ultimaMinuta = minutaService.getUltimaMinuta().get();
        if (hayQueNotificar(ultimaMinuta)) {
            ultimaMinuta.getTemas().stream().filter(this::hayQueNotificarElTema)
                    .forEach(temaDeMinuta -> {
                        TemaDeReunion temaDeReunion = temaDeMinuta.getTema();
                        Usuario autorDelTemaDeReunion = temaDeReunion.getAutor();
                        Email email = EmailBuilder.startingBlank()
                                .from(SENDER_NAME, SENDER_ADDRESS)
                                .to(autorDelTemaDeReunion.getName(), autorDelTemaDeReunion.getMail())
                                .withSubject(getSubjectFor(temaDeReunion))
                                .withPlainText(getMessageFor(temaDeReunion))
                                .buildEmail();
                        mailer.sendMail(email, true);
                        temaDeMinuta.marcarComoNotificado();
                        temaDeMinutaService.save(temaDeMinuta);
                    });
        }
    }

    private Boolean hayQueNotificar(Minuta unaMinuta) {
        LocalDateTime momentoActual = LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        LocalDate fechaActual = momentoActual.toLocalDate();
        LocalDate fechaDeNotificacion = CalculadorDeFechaDeNotificacion.calcularParaTemasNoTratados(unaMinuta);

        return fechaActual.isEqual(fechaDeNotificacion);
    }

    private Boolean hayQueNotificarElTema(TemaDeMinuta temaDeMinuta) {
        return !temaDeMinuta.getFueTratado() && !temaDeMinuta.fueNotificado();
    }

    private void setMailer(Mailer unMailer) {
        mailer = unMailer;
    }

    private void setClock(Clock unClock) {
        clock = unClock;
    }

    private String getSubjectFor(TemaDeReunion temaDeReunion) {
        return String.format("Tu tema \"%s\" no fue tratado", temaDeReunion.getTitulo());
    }

    private String getMessageFor(TemaDeReunion temaDeReunion) {
        return String.format("Hola! El tema \"%s\" que presentaste en la roots pasada no fue tratado. Sentite libre de volver a proponerlo!",
                temaDeReunion.getTitulo());
    }
}