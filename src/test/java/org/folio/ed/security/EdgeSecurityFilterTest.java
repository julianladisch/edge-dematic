package org.folio.ed.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.commons.lang.StringUtils;
import org.folio.ed.TestBase;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EdgeSecurityFilterTest extends TestBase {

  private static final String HEALTH_CHECK_ENDPOINT = "http://localhost:%s/admin/health";
  private static final String INFO_ENDPOINT = "http://localhost:%s/admin/info";

  @Test
  void testHealthCheck() {
    log.info("===== Verify health check endpoint =====");
    var response = get(String.format(HEALTH_CHECK_ENDPOINT, edgeDematicPort), getEmptyHeaders(), JsonNode.class);
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody()
      .get("status")
      .asText(), equalTo("UP"));
  }

  @Test
  void testInfo() {
    log.info("===== Verify info endpoint =====");
    var response = get(String.format(INFO_ENDPOINT, edgeDematicPort), getEmptyHeaders(), JsonNode.class);
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody()
      .asText(), equalTo(StringUtils.EMPTY));
  }
}
