package org.folio.ed.security;

import static org.folio.ed.security.TenantAwareAWSParamStore.DEFAULT_AWS_KEY_PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.folio.edge.core.security.AwsParamStore;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
  void testGetTenantsIfStagingDirectorTenantsValueEmpty() {
    log.info("=== Test: Get tenants if staging director tenants value is empty ===");

    var value = "test_tenant_1, test_user";
    var resp = new GetParameterResult().withParameter(new Parameter().withName("parameterName")
      .withValue(value));
    when(ssm.getParameter(isA(GetParameterRequest.class))).thenReturn(resp);

    var argumentRequest = ArgumentCaptor.forClass(GetParameterRequest.class);
    var tenants = secureStore.getTenants(null);
    verify(ssm).getParameter(argumentRequest.capture());

    assertEquals(DEFAULT_AWS_KEY_PARAMETER, argumentRequest.getValue().getName());
    assertTrue(tenants.isPresent());
    assertThat(tenants.get(), Matchers.equalTo(value));
  }

  @Test
  void testGetTenantsIfStagingDirectorTenantsValueNotEmpty() {
    log.info("=== Test: Get tenants if staging director tenants value is not empty ===");

    var value = "test_tenant_1, test_user";
    var resp = new GetParameterResult().withParameter(new Parameter().withName("parameterName")
      .withValue(value));
    when(ssm.getParameter(isA(GetParameterRequest.class))).thenReturn(resp);

    var argumentRequest = ArgumentCaptor.forClass(GetParameterRequest.class);
    var tenants = secureStore.getTenants("stagingDirectorTenants");
    verify(ssm).getParameter(argumentRequest.capture());

    assertEquals("stagingDirectorTenants", argumentRequest.getValue().getName());
    assertTrue(tenants.isPresent());
    assertThat(tenants.get(), Matchers.equalTo(value));
  }
}
