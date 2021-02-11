package org.folio.ed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableFeignClients
@EnableCaching
public class EdgeDematicApplication {
  public static void main(String[] args) {
    SpringApplication.run(EdgeDematicApplication.class, args);
  }
}
