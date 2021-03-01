package org.folio.ed.security;

import static org.folio.ed.service.SecurityManagerService.STAGING_DIRECTOR_CLIENT_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.folio.edge.core.security.AwsParamStore;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TenantAwareAWSParamStoreTest {

  @Mock
  AWSSimpleSystemsManagement ssm;

  @InjectMocks
  TenantAwareAWSParamStore secureStore;

  @BeforeEach
  void setUp() {
    Properties props = new Properties();
    props.put(AwsParamStore.PROP_REGION, "us-east-1");
    secureStore = new TenantAwareAWSParamStore(props);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetTenants() {
    log.info("=== Test: Get tenants ===");

    String value = "test_tenant_1, test_user";
    String key = STAGING_DIRECTOR_CLIENT_NAME + "_tenants";

    GetParameterRequest req = (new GetParameterRequest()).withName(key)
      .withWithDecryption(true);
    GetParameterResult resp = new GetParameterResult().withParameter(new Parameter().withName(key)
      .withValue(value));
    when(ssm.getParameter(req)).thenReturn(resp);

    var tenants = secureStore.getTenants();
    assertTrue(tenants.isPresent());
    assertThat(tenants.get(), Matchers.equalTo(value));

  }
}
