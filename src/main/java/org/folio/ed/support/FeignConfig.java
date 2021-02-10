package org.folio.ed.support;

import feign.Client;
import feign.Logger;
import org.folio.ed.client.EnrichHeadersClient;
import org.folio.ed.client.logger.SensitiveDataProtectionLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Client enrichHeadersClient() {
      return new EnrichHeadersClient();
    }

    @Bean
    public Logger feignLogger() {
      return new SensitiveDataProtectionLogger();
    }

}
