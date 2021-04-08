package org.folio.ed.security;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.folio.ed.TestBase;
import org.folio.ed.domain.entity.ConnectionSystemParameters;
import org.folio.ed.error.AuthorizationException;
import org.folio.ed.service.SecurityManagerService;
import org.folio.edge.core.utils.ApiKeyUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SecurityManagerServiceTest extends TestBase {

  public static final String OKAPI_TOKEN = "AAA-BBB-CCC-DDD";
  public static final String USER_PASSWORD = "password";

  @Autowired
  SecurityManagerService sms;

  @Test
  void testGetTenants() {
    log.info("=== Test: Get tenants ===");
    var tenants = sms.getStagingDirectorTenantsUserMap();
    assertThat(tenants, hasSize(1));
  }

  @Test
  void testGetConnectionSystemParametersByTenant() {
    log.info("=== Test: Get connection system parameters by tenantId ===");
    var connectionSystemParameters = sms.getStagingDirectorConnectionParameters(TEST_TENANT);
    verifyConnectionSystemParameters(connectionSystemParameters);
    verifyLoginCall();
  }

  @Test
  void testGetConnectionSystemParametersByToken() {
    log.info("=== Test: Get connection system parameters by edge token ===");
    var edgeApiKey = ApiKeyUtils.generateApiKey("stagingDirector", TEST_TENANT, TEST_USER);
    var connectionSystemParameters = sms.getOkapiConnectionParameters(edgeApiKey);
    verifyConnectionSystemParameters(connectionSystemParameters);
    verifyLoginCall();
  }

  @Test
  void testInvalidTenant() {
    log.info("=== Test: Get connection system parameters by invalid tenant ===");
    AuthorizationException exception = assertThrows(AuthorizationException.class,
        () -> sms.getStagingDirectorConnectionParameters("invalid-tenant"));
    assertThat(exception.getMessage(), is("Cannot get system connection properties for: invalid-tenant"));
  }

  @Test
  void testInvalidToken() {
    log.info("=== Test: Get connection system parameters by invalid edge token ===");
    var edgeApiKey = ApiKeyUtils.generateApiKey("stagingDirector", "invalid-tenant", "invalid-tenant");
    AuthorizationException exception = assertThrows(AuthorizationException.class,
        () -> sms.getOkapiConnectionParameters(edgeApiKey));
    assertThat(exception.getMessage(), is("Cannot get system connection properties for: invalid-tenant"));
  }

  @Test
  void testMalformedToken() {
    log.info("=== Test: Get connection system parameters by invalid malformed token ===");
    AuthorizationException exception = assertThrows(AuthorizationException.class, () -> sms.getOkapiConnectionParameters("1"));
    assertThat(exception.getMessage(), is("Malformed edge api key: 1"));
  }

  private void verifyLoginCall() {
    List<String> paths = wireMockServer.getAllServeEvents()
      .stream()
      .map(e -> e.getRequest()
        .getUrl())
      .collect(toList());
    assertThat(paths, hasSize(1));
    assertThat(paths, Matchers.contains("/authn/login"));
  }

  private void verifyConnectionSystemParameters(ConnectionSystemParameters connectionSystemParameters) {
    assertThat(connectionSystemParameters.getTenantId(), is(TEST_TENANT));
    assertThat(connectionSystemParameters.getUsername(), is(TEST_USER));
    assertThat(connectionSystemParameters.getPassword(), is(USER_PASSWORD));
    assertThat(connectionSystemParameters.getOkapiToken(), is(OKAPI_TOKEN));
  }
}
