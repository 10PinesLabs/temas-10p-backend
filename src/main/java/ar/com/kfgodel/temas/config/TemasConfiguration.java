package ar.com.kfgodel.temas.config;

import ar.com.kfgodel.orm.api.config.DbCoordinates;

/**
 * This type represents the configuration data for the application
 * Created by kfgodel on 12/03/16.
 */
public interface TemasConfiguration {

  /**
   * @return The database information to connect to it
   */
  DbCoordinates getDatabaseCoordinates();

  /**
   * @return The port number to use for the web server
   */
  int getHttpPort();
}
