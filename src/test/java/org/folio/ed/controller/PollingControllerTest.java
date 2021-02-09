package org.folio.ed.controller;

import lombok.extern.log4j.Log4j2;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Log4j2
public class PollingControllerTest extends TestBase {

  private static final String CONFIGURATIONS_URL = "http://localhost:%s/asrService/asr/lookupNewAsrItems";
  private static final String PROVIDERS_URL = "http://localhost:%s/remote-storage/providers/";
  private static final String TENANT_URL = "http://localhost:%s/_/tenant";

  private String configurationsUrl;



  @BeforeEach
  void prepareUrl() {
    configurationsUrl = String.format(CONFIGURATIONS_URL, okapiPort);
  }


  @Test
  void canGetAllConfigurations() {
    ResponseEntity<String> responseEntity = get(configurationsUrl + "/de17bad7-2a30-4f1c-bee5-f653ded15629", String.class);
    log.info(responseEntity.getBody());
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
//    assertThat(responseEntity.getBody().getTotalRecords(), is(1));
  }

}
