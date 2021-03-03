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

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.folio.ed.TestBase;
import org.folio.edge.core.utils.ApiKeyUtils;
import org.folio.ed.domain.dto.AsrItems;
import org.folio.ed.domain.dto.AsrRequests;
import org.folio.ed.domain.dto.UpdateAsrItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmsIntegrationTest extends TestBase {

  private static final String LOOKUP_NEW_ASR_ITEM = "http://localhost:%s/asrService/asr/lookupNewAsrItems";
  private static final String LOOKUP_ASR_REQUESTS = "http://localhost:%s/asrService/asr/lookupAsrRequests";
  private static final String UPDATE_ASR_STATUS_RETRIEVED = "http://localhost:%s/asrService/asr/updateASRItemStatusBeingRetrieved";
  private static final String UPDATE_ASR_STATUS_AVAILABLE = "http://localhost:%s/asrService/asr/updateASRItemStatusAvailable";

  private static final String APIKEY = ApiKeyUtils.generateApiKey("stagingDirector", TEST_TENANT, TEST_USER);

  private String lookupNewAsrItem, lookupAsrRequests, updateAsrStatusBeingRetrieved, updateAsrStatusAvailable;

  @Autowired
  private MappingJackson2XmlHttpMessageConverter converter;

  @BeforeEach
  void prepareUrl() {
    lookupNewAsrItem = String.format(LOOKUP_NEW_ASR_ITEM, edgeDematicPort);
    lookupAsrRequests = String.format(LOOKUP_ASR_REQUESTS, edgeDematicPort);
    updateAsrStatusBeingRetrieved = String.format(UPDATE_ASR_STATUS_RETRIEVED, edgeDematicPort);
    updateAsrStatusAvailable = String.format(UPDATE_ASR_STATUS_AVAILABLE, edgeDematicPort);
  }

  @Test
  void getNewAsrItemsTest() throws JsonProcessingException {
    log.info("===== Get items: successful (edge API key in the query parameter) =====");

    var response = get(lookupNewAsrItem + "/de17bad7-2a30-4f1c-bee5-f653ded15629?apikey=" + APIKEY,
      getEmptyHeaders(), String.class);

    assertThat(response.getBody(), notNullValue());
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders()
      .getContentType(), is(APPLICATION_XML));

    log.info("Response: " + response.getBody());

    var asrItems = converter.getObjectMapper()
      .readValue(response.getBody(), AsrItems.class)
      .getAsrItems();
    assertThat(asrItems, hasSize(1));

    var asrItem = asrItems.get(0);

    // Verify asrItem
    assertThat(asrItem.getAuthor(), is("Barnes, Adrian"));
    assertThat(asrItem.getTitle(), is("Nod"));
    assertThat(asrItem.getItemNumber(), is("697685458679"));
    assertThat(asrItem.getCallNumber(), is("some-callnumber"));

    // Verify set accessioned by barcode
    var requests = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    var setAccessionEvent = requests.get("/remote-storage/accessions/barcode/697685458679");
    assertThat(setAccessionEvent, notNullValue());

    await().atMost(1, TimeUnit.SECONDS)
      .untilAsserted(() -> assertThat(wireMockServer.getAllServeEvents(), hasSize(3)));

    assertThat(setAccessionEvent.getResponse()
      .getStatus(), is(204));
  }

  @Test
  void getNewAsrItemsErrorTest() {
    log.info("===== Get items: internal server error =====");
    var headers = getEmptyHeaders();
    var exception = assertThrows(HttpServerErrorException.class,
      () -> get(lookupNewAsrItem + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa?apikey=" + APIKEY, headers, String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  void getAsrRequestsTest() throws JsonProcessingException {
    log.info("===== Get requests: successful (edge API key in the query parameter) =====");

    var response = get(lookupAsrRequests + "/de17bad7-2a30-4f1c-bee5-f653ded15629?apikey=" + APIKEY,
      getEmptyHeaders(), String.class);

    assertThat(response.getBody(), notNullValue());
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders()
      .getContentType(), is(APPLICATION_XML));

    log.info("Response: " + response.getBody());

    var asrRequests = converter.getObjectMapper()
      .readValue(response.getBody(), AsrRequests.class)
      .getAsrRequests();
    assertThat(asrRequests, hasSize(1));

    var asrRequest = asrRequests.get(0);

    // Verify AsrRequest
    assertThat(asrRequest.getHoldId(), is("hold_id"));
    assertThat(asrRequest.getItemBarcode(), is("697685458679"));
    assertThat(asrRequest.getAuthor(), is("Some Author"));
    assertThat(asrRequest.getTitle(), is("Some title"));
    assertThat(asrRequest.getCallNumber(), is("some call number"));
    assertThat(asrRequest.getPatronBarcode(), is("987654321"));
    assertThat(asrRequest.getPatronName(), is("Some Patron Name"));
    assertThat(asrRequest.getPickupLocation(), is("pickup_location"));
    assertThat(asrRequest.getRequestStatus(), is("Request-Status"));
    assertThat(asrRequest.getRequestNote(), is("Request_Note"));
    assertThat(asrRequest.getRequestDate(), is("2021-02-21 17:29:09.0"));

    // Verify set retrieval by barcode
    var requests = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    var setRetrievalEvent = requests.get("/remote-storage/retrievals/barcode/697685458679");
    assertThat(setRetrievalEvent, notNullValue());

    await().atMost(1, TimeUnit.SECONDS)
      .untilAsserted(() -> assertThat(wireMockServer.getAllServeEvents(), hasSize(3)));

    assertThat(setRetrievalEvent.getResponse()
      .getStatus(), is(204));
  }

  @Test
  void getAsrRequestsErrorTest() {
    log.info("===== Get requests: Internal Server Error =====");

    var headers = getEmptyHeaders();
    var exception = assertThrows(HttpServerErrorException.class,
      () -> get(lookupAsrRequests + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa?apikey=" + APIKEY, headers, String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  void postAsrItemChekInTest() {
    log.info("===== Post item update (check-in): successful (edge API key in the headers) =====");

    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("697685458679");
    var headers = getEmptyHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(APIKEY));
    var responseEntity = post(updateAsrStatusBeingRetrieved + "/de17bad7-2a30-4f1c-bee5-f653ded15629", headers,
      updateAsrItem, String.class);
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

    var serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // Verify call to mod-remote-storage
    assertThat(serveEvents.size(), is(2));
    var checkInServeEvent = serveEvents.get("/remote-storage/retrieve/de17bad7-2a30-4f1c-bee5-f653ded15629/checkInItem");
    assertThat(checkInServeEvent.getRequest()
      .getBodyAsString(), containsString("{\"itemBarcode\":\"697685458679\"}"));

  }

  @Test
  void postAsrItemReturnTest() {
    log.info("===== Post item update (return): successful (edge API key in the headers) =====");

    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("697685458679");
    var headers = getEmptyHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(APIKEY));
    var responseEntity = post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629", headers,
      updateAsrItem, String.class);
    assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

    var serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // Verify call to mod-remote-storage
    assertThat(serveEvents.size(), is(2));
    var checkInServeEvent = serveEvents.get("/remote-storage/return/de17bad7-2a30-4f1c-bee5-f653ded15629");
    assertThat(checkInServeEvent.getRequest()
      .getBodyAsString(), containsString("{\"itemBarcode\":\"697685458679\"}"));

  }

  @Test
  void postAsrItemUpdateErrorTest() {
    log.info("===== Post item update (check-in): Internal Server Error =====");

    var updateAsrItem = new UpdateAsrItem();
    updateAsrItem.setItemBarcode("error-barcode");
    var headers = getEmptyHeaders();

    HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
      () -> post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629?apikey=" + APIKEY, headers, updateAsrItem,
        String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  void emptyApiKeyGetAsrItemsTest() {
    log.info("===== Get items: empty API key =====");

    var headers = getEmptyHeaders();

    var getException = assertThrows(HttpClientErrorException.class,
      () -> get(lookupNewAsrItem + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa", headers, String.class));
    assertThat(getException.getStatusCode(), is(HttpStatus.FORBIDDEN));
    assertThat(getException.getMessage(), is("403 : [Edge API key not found in the request]"));
  }

  @Test
  void emptyApiKeyPostStatusTest() {
    log.info("===== Post item update (check-in): empty API key =====");

    var headers = getEmptyHeaders();
    var asrItem = new UpdateAsrItem();

    var postException = assertThrows(HttpClientErrorException.class,
      () -> post(updateAsrStatusAvailable + "/de17bad7-2a30-4f1c-bee5-f653ded15629", headers, asrItem, String.class));
    assertThat(postException.getStatusCode(), is(HttpStatus.FORBIDDEN));
    assertThat(postException.getMessage(), is("403 : [Edge API key not found in the request]"));
  }

  @Test
  void invalidApiKeyTest() {
    log.info("===== Get items: invalid API key =====");

    var headers = getEmptyHeaders();
    var invalidApiKey = ApiKeyUtils.generateApiKey("stagingDirector", "invalid_tenant", "invalid_tenant");
    var exception = assertThrows(HttpClientErrorException.class,
      () -> get(lookupNewAsrItem + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa?apikey=" + invalidApiKey, headers, String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.FORBIDDEN));
    assertThat(exception.getMessage(), is("403 : [Cannot get system connection properties for: invalid_tenant]"));
  }

  @Test
  void malformedApiKeyTest() {
    log.info("===== Get items: malformed API key =====");
    var headers = getEmptyHeaders();
    var exception = assertThrows(HttpClientErrorException.class,
      () -> get(lookupNewAsrItem + "/c7310e5e-c4be-4d8f-943c-faaa35679aaa?apikey=1", headers, String.class));
    assertThat(exception.getStatusCode(), is(HttpStatus.FORBIDDEN));
    assertThat(exception.getMessage(), is("403 : [Malformed edge api key: 1]"));
  }

  private HttpHeaders getEmptyHeaders() {
    return new HttpHeaders();
  }
}
