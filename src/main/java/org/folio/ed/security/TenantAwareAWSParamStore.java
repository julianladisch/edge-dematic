package org.folio.ed.security;

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.folio.edge.core.security.AwsParamStore;

import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TenantAwareAWSParamStore extends AwsParamStore {

  public static final String DEFAULT_AWS_KEY_PARAMETER = "stagingDirector_tenants";

  public TenantAwareAWSParamStore(Properties properties) {
    super(properties);
  }

  public Optional<String> getTenants(String stagingDirectorTenants) {
    String key = StringUtils.isNotEmpty(stagingDirectorTenants) ? stagingDirectorTenants : DEFAULT_AWS_KEY_PARAMETER;
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
