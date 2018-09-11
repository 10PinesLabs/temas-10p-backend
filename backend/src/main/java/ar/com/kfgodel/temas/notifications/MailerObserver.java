package ar.com.kfgodel.temas.notifications;

import convention.persistent.ActionItem;

import java.util.List;

public abstract class MailerObserver {

    public static MailerObserver create(){
        return new ActionItemMailSender(MailerConfiguration.getMailer());
    }

    public abstract void onSetResponsables(ActionItem actionItem);

    public abstract void notificar(List<ActionItem> oldActionItem, List<ActionItem> newActionItem);

}
