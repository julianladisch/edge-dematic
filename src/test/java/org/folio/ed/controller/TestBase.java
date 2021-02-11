package org.folio.ed.controller;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;

import lombok.extern.log4j.Log4j2;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@Log4j2
public class TestBase {
  public static final String METADATA = "metadata";
  private static Map<String, Collection<String>> headers;
  private static RestTemplate restTemplate;
  public static WireMockServer wireMockServer;
  public static String TEST_TENANT = "test_tenant";

  @LocalServerPort
  protected int okapiPort;

  public final static int WIRE_MOCK_PORT = SocketUtils.findAvailableTcpPort();

  @Autowired
  private TenantController tenantController;

  @Autowired
  private FolioModuleMetadata moduleMetadata;

  @BeforeEach
  void setUp() {
    headers = new HashMap<>();
    headers.put(XOkapiHeaders.URL, singletonList(getOkapiUrl()));
    headers.put(XOkapiHeaders.TENANT, singletonList(TEST_TENANT));
    FolioExecutionScopeExecutionContextManager
      .beginFolioExecutionContext(new DefaultFolioExecutionContext(moduleMetadata, headers));

    tenantController.postTenant(new TenantAttributes().moduleTo("edge-dematic"));

    wireMockServer.resetAll();
  }

  public static String getOkapiUrl() {
    return String.format("http://localhost:%s", WIRE_MOCK_PORT);
  }

  @AfterEach
  void eachTearDown() {
    tenantController.deleteTenant();
  }

  @BeforeAll
  static void testSetup() {
    restTemplate = new RestTemplate();

    wireMockServer = new WireMockServer(WIRE_MOCK_PORT);
    wireMockServer.start();
  }

  @AfterAll
  static void tearDown() {
    wireMockServer.stop();
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
    return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), clazz);
  }

  public <T> ResponseEntity<T> post(String url, Object entity, Class<T> clazz) {
    return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(entity), clazz);
  }

  public ResponseEntity<String> put(String url, Object entity) {
    return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(entity), String.class);
  }

  public ResponseEntity<String> delete(String url) {
    return restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
  }
}
