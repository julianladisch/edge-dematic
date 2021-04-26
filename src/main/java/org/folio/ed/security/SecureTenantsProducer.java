package org.folio.ed.security;

import lombok.extern.log4j.Log4j2;
import org.folio.edge.core.security.SecureStore;

import java.util.Optional;
import java.util.Properties;

@Log4j2
public class SecureTenantsProducer {

  private SecureTenantsProducer() {}

  public static Optional<String> getTenants(Properties secureStoreProps, SecureStore secureStore, String stagingDirectorTenants) {
    if (secureStore instanceof TenantAwareAWSParamStore) {
      var stringOptional = ((TenantAwareAWSParamStore) secureStore).getTenants(stagingDirectorTenants);
      if (stringOptional.isEmpty()) {
        log.warn("Tenants list not found in AWS Param store. Please create variable, which contains comma separated list of tenants");
      }
      return stringOptional;
    }
    return Optional.of((String) secureStoreProps.get("tenants"));
  }
}
