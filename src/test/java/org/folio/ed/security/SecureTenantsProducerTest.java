package org.folio.ed.security;

import org.folio.edge.core.security.EphemeralStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class SecureTenantsProducerTest {

  @Test
  void testGetTenants() {
    var secureStoreProps = new Properties();
    secureStoreProps.put("tenants", "test_tenant");

    var stagingDirectorTenants = "stagingDirectorTenants";
    var mockSecureStore = Mockito.mock(EphemeralStore.class);
    var tenants = SecureTenantsProducer.getTenants(secureStoreProps, mockSecureStore, stagingDirectorTenants);
    assertEquals("test_tenant", tenants.get());

    var mockAwsSecureStore  = Mockito.mock(TenantAwareAWSParamStore.class);
    when(mockAwsSecureStore.getTenants(stagingDirectorTenants)).thenReturn(Optional.of("test_tenant_aws"));
    tenants = SecureTenantsProducer.getTenants(secureStoreProps, mockAwsSecureStore, stagingDirectorTenants);
    assertEquals("test_tenant_aws", tenants.get());
  }
}
