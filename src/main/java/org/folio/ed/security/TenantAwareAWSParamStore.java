package org.folio.ed.security;

import static org.folio.ed.service.SecurityManagerService.STAGING_DIRECTOR_CLIENT_NAME;

import java.util.Optional;
import java.util.Properties;

import org.folio.edge.core.security.AwsParamStore;

import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TenantAwareAWSParamStore extends AwsParamStore {

  public TenantAwareAWSParamStore(Properties properties) {
    super(properties);
  }

  public Optional<String> getTenants() {
    String key = STAGING_DIRECTOR_CLIENT_NAME + "_tenants";
    GetParameterRequest req = (new GetParameterRequest()).withName(key)
      .withWithDecryption(true);

    try {
      return Optional.of(this.ssm.getParameter(req)
        .getParameter()
        .getValue());
    } catch (Exception e) {
      log.warn("Cannot get tenants list from key: " + key, e);
      return Optional.empty();
    }
  }
}
