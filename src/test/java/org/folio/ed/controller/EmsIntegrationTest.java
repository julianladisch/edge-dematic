package org.folio.ed.controller;

import lombok.extern.log4j.Log4j2;


import org.folio.rs.domain.dto.UpdateAsrItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Log4j2
public class EmsIntegrationTest extends TestBase {

  private static final String LOOKUP_NEW_ASR_ITEM = "http://localhost:%s/asrService/asr/lookupNewAsrItems";
  private static final String LOOKUP_ASR_REQUESTS = "http://localhost:%s/asrService/asr/lookupAsrRequests";
  private static final String UPDATE_ASR_STATUS_AVAILABLE = "http://localhost:%s/asrService/asr/updateAsrItemStatusAvailable";

  private String lookupNewAsrItem, lookupAsrRequests, updateAsrStatusAvailable;



  @BeforeEach
  void prepareUrl() {
    lookupNewAsrItem = String.format(LOOKUP_NEW_ASR_ITEM, okapiPort);
    lookupAsrRequests = String.format(LOOKUP_ASR_REQUESTS, okapiPort);
    updateAsrStatusAvailable = String.format(UPDATE_ASR_STATUS_AVAILABLE, okapiPort);
  }


  @Test
  void getNewAsrItemsTest() {
    ResponseEntity<String> responseEntity = get(lookupNewAsrItem + "/de17bad7-2a30-4f1c-bee5-f653ded15629", String.class);
    log.info(responseEntity.getBody());
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  void getAsrRequestsTest() {
    ResponseEntity<String> responseEntity = get(lookupAsrRequests + "/de17bad7-2a30-4f1c-bee5-f653ded15629", String.class);
    log.info(responseEntity.getBody());
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  void postAsrItemUpdateTest() {
    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("123456789");
    ResponseEntity<String> responseEntity = post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629", updateAsrItem, String.class);
    log.info(responseEntity.getBody());
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
  }

}
