package org.folio.ed.integration;

import static java.util.function.Function.identity;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_XML;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.folio.ed.TestBase;
import org.folio.rs.domain.dto.AsrItem;
import org.folio.rs.domain.dto.AsrItems;
import org.folio.rs.domain.dto.AsrRequest;
import org.folio.rs.domain.dto.AsrRequests;
import org.folio.rs.domain.dto.UpdateAsrItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import lombok.extern.log4j.Log4j2;

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
    log.info("===== Get items: successful =====");

    ResponseEntity<AsrItems> response = get(lookupNewAsrItem + "/de17bad7-2a30-4f1c-bee5-f653ded15629", AsrItems.class);

    assertThat(response.getBody(), notNullValue());
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders()
      .getContentType(), is(APPLICATION_XML));

    List<AsrItem> asrItems = response.getBody()
      .getAsrItems();
    assertThat(asrItems, hasSize(1));

    var asrItem = asrItems.get(0);

    // Verify asrItem
    assertThat(asrItem.getAuthor(), is("Barnes, Adrian"));
    assertThat(asrItem.getTitle(), is("Nod"));
    assertThat(asrItem.getItemNumber(), is("697685458679"));
    assertThat(asrItem.getCallNumber(), is("some-callnumber"));

    // Verify set accessioned by barcode
    Map<String, ServeEvent> requests = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    ServeEvent setAccessionEvent = requests.get("/remote-storage/accessions/barcode/697685458679");
    assertThat(setAccessionEvent, notNullValue());

    await().atMost(1, TimeUnit.SECONDS)
      .untilAsserted(() -> assertThat(wireMockServer.getAllServeEvents(), hasSize(2)));

    assertThat(setAccessionEvent.getResponse()
      .getStatus(), is(204));

  }

  @Test
  void getNewAsrItemsErrorTest() {
    HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
        () -> get(lookupNewAsrItem + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa", String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  void getAsrRequestsTest() {
    log.info("===== Get requests: successful =====");

    ResponseEntity<AsrRequests> response = get(lookupAsrRequests + "/de17bad7-2a30-4f1c-bee5-f653ded15629", AsrRequests.class);

    assertThat(response.getBody(), notNullValue());
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders()
      .getContentType(), is(APPLICATION_XML));

    List<AsrRequest> asrRequests = response.getBody()
      .getAsrRequests();
    assertThat(asrRequests, hasSize(1));

    var asrRequest = asrRequests.get(0);

    // Verify AsrRequest
    assertThat(asrRequest.getHoldId(), is("hold_id"));
    assertThat(asrRequest.getItemBarcode(), is("697685458679"));
    assertThat(asrRequest.getAuthor(), is("Some Author"));
    assertThat(asrRequest.getTitle(), is("Some title"));
    assertThat(asrRequest.getCallNumber(), is("+1-111-222"));
    assertThat(asrRequest.getPatronBarcode(), is("987654321"));
    assertThat(asrRequest.getPatronName(), is("Some Patron Name"));
    assertThat(asrRequest.getPickupLocation(), is("pickup_location"));
    assertThat(asrRequest.getRequestStatus(), is("Request-Status"));
    assertThat(asrRequest.getRequestNote(), is("Request_Note"));
    assertThat(asrRequest.getRequestDate(), is(Timestamp.valueOf(LocalDateTime.parse("2021-02-03T06:31:35.550"))));

    // Verify set retrieval by barcode
    Map<String, ServeEvent> requests = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    ServeEvent setRetrievalEvent = requests.get("/remote-storage/retrievals/barcode/697685458679");
    assertThat(setRetrievalEvent, notNullValue());

    await().atMost(1, TimeUnit.SECONDS)
      .untilAsserted(() -> assertThat(wireMockServer.getAllServeEvents(), hasSize(2)));

    assertThat(setRetrievalEvent.getResponse()
      .getStatus(), is(204));
  }

  @Test
  void getAsrRequestsErrorTest() {
    log.info("===== Get requests: Internal Server Error =====");

    HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
        () -> get(lookupAsrRequests + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa", String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  void postAsrItemUpdateTest() {
    log.info("===== Post item update (check-in): successful =====");

    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("123456789");
    ResponseEntity<String> responseEntity = post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629", updateAsrItem,
        String.class);
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // Verify call to mod-remote-storage
    assertThat(serveEvents.size(), is(1));
    ServeEvent checkInServeEvent = serveEvents.get("/remote-storage/de17bad7-2a30-4f1c-bee5-f653ded15629/checkInItem");
    assertThat(checkInServeEvent.getRequest()
      .getBodyAsString(), containsString("{\"itemBarcode\":\"123456789\"}"));

  }

  @Test
  void postAsrItemUpdateErrorTest() {
    log.info("===== Post item update (check-in): Internal Server Error =====");

    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("error-barcode");

    HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
        () -> post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629", updateAsrItem, String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

}
