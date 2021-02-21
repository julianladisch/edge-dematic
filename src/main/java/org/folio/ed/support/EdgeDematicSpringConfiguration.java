package org.folio.ed.support;

import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EdgeDematicSpringConfiguration {

  @Bean
  public FolioSpringLiquibase folioSpringLiquibase() {
    return new FolioSpringLiquibase();
  }

}
