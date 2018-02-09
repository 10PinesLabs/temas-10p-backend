package ar.com.kfgodel.temas.config;

import ar.com.kfgodel.appbyconvention.operation.api.ApplicationOperation;
import ar.com.kfgodel.dependencies.api.DependencyInjector;
import ar.com.kfgodel.diamond.api.types.reference.ReferenceOf;
import ar.com.kfgodel.orm.api.config.DbCoordinates;
import ar.com.kfgodel.orm.api.operations.basic.Save;
import ar.com.kfgodel.orm.impl.config.ImmutableDbCoordinates;
import ar.com.kfgodel.temas.application.auth.BackofficeCallbackAuthenticator;
import ar.com.kfgodel.temas.application.auth.BackofficeCallbackAuthenticatorForAll;
import ar.com.kfgodel.temas.filters.users.UserCount;
import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import convention.persistent.Usuario;
import convention.rest.api.UserResource;
import convention.rest.api.tos.BackofficeUserTo;
import convention.rest.api.tos.UserTo;

import ar.com.kfgodel.temas.filters.users.FindAllUsersOrderedByName;
import javax.inject.Inject;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * This type represents the configuration for a development environment
 * <p>
 * Created by kfgodel on 12/03/16.
 */
public class DevelopmentConfig implements TemasConfiguration {


  public static final int DEFAULT_PORT = 9090;

  public static DevelopmentConfig create() {
    DevelopmentConfig config = new DevelopmentConfig();
    return config;
  }

  @Override
  public DbCoordinates getDatabaseCoordinates() {
    return ImmutableDbCoordinates.createDeductingDialect("jdbc:h2:file:./db/h2", "sa", "");
  }

  @Override
  public int getHttpPort() {
    try {
      return Integer.parseInt(System.getenv("PORT"));
    } catch (NumberFormatException e) {
      return DEFAULT_PORT;
    }
  }

  @Override
  public Function<WebCredential, Optional<Object>> autenticador() {
    return BackofficeCallbackAuthenticatorForAll.create(getInjector());
  }

  protected List<UserTo> getUsers() {
    return ApplicationOperation.createFor(getInjector()).
            insideASession().
            applying(FindAllUsersOrderedByName.create())
            .convertTo(new ReferenceOf<List<UserTo>>() {
            }.getReferencedType());
  }
}

