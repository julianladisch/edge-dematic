package org.folio.ed.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.folio.ed.util.Constants.COMMA;
import static org.folio.edge.core.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.core.Constants.PROP_SECURE_STORE_TYPE;
import static org.folio.edge.core.Constants.X_OKAPI_TOKEN;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.folio.ed.client.AuthnClient;
import org.folio.ed.domain.entity.ConnectionSystemParameters;
import org.folio.ed.error.AuthorizationException;
import org.folio.ed.security.SecureStoreFactory;
import org.folio.ed.security.TenantAwareAWSParamStore;
import org.folio.edge.core.model.ClientInfo;
import org.folio.edge.core.security.AwsParamStore;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.security.SecureStore.NotFoundException;
import org.folio.edge.core.utils.ApiKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SecurityManagerService {

  public static final String STAGING_DIRECTOR_CLIENT_NAME = "stagingDirector";
  public static final String SYSTEM_USER_PARAMETERS_CACHE = "systemUserParameters";

  @Value("${secure_store}")
  private String secureStoreType;

  @Value("${secure_store_props}")
  private String secureStorePropsFile;

  @Autowired
  private AuthnClient authnClient;

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");

  private SecureStore secureStore;

  private Map<String, String> tenantsUsersMap = new HashMap<>();

  @PostConstruct
  public void init() {

    Properties secureStoreProps = getProperties(secureStorePropsFile);
    String type = secureStoreProps.getProperty(PROP_SECURE_STORE_TYPE, DEFAULT_SECURE_STORE_TYPE);

    String tenantsStr;

    secureStore = SecureStoreFactory.getSecureStore(type, secureStoreProps);

    if (AwsParamStore.TYPE.equals(type)) {
      final Optional<String> stringOptional = ((TenantAwareAWSParamStore) secureStore).getTenants();
      if (stringOptional.isEmpty()) {
        log.warn("Tenants list not found in AWS Param store. Please create variable, which contains comma separated list of tenants");
        return;
      }
      tenantsStr = stringOptional.get();
    } else {
      tenantsStr = (String) secureStoreProps.get("tenants");
    }

    tenantsUsersMap = Arrays.stream(COMMA.split(tenantsStr))
      .collect(toMap(tenant -> tenant, tenant -> COMMA
        .split(secureStoreProps.getProperty(tenant))[0]));
  }

  @Cacheable(value = SYSTEM_USER_PARAMETERS_CACHE, key = "#tenantId")
  public ConnectionSystemParameters getStagingDirectorConnectionParameters(String tenantId) {
    return enrichConnectionSystemParametersWithOkapiToken(tenantId, tenantsUsersMap.get(tenantId));
  }

  @Cacheable(value = SYSTEM_USER_PARAMETERS_CACHE, key = "#edgeApiKey")
  public ConnectionSystemParameters getOkapiConnectionParameters(String edgeApiKey) {

    String tenantId;
    String username;
    try {
      ClientInfo clientInfo = ApiKeyUtils.parseApiKey(edgeApiKey);
      tenantId = clientInfo.tenantId;
      username = clientInfo.username;
    } catch (ApiKeyUtils.MalformedApiKeyException e) {
      throw new AuthorizationException("Malformed edge api key: " + edgeApiKey);
    }
    return enrichConnectionSystemParametersWithOkapiToken(tenantId, username);
  }

  private ConnectionSystemParameters enrichConnectionSystemParametersWithOkapiToken(String tenantId, String username) {
    try {
      return enrichWithOkapiToken(ConnectionSystemParameters.builder()
        .tenantId(tenantId)
        .username(username)
        .password(secureStore.get(STAGING_DIRECTOR_CLIENT_NAME, tenantId, username))
        .build());
    } catch (NotFoundException e) {
      throw new AuthorizationException("Cannot get system connection properties for: " + tenantId);
    }
  }

  public Set<String> getTenantsUsersMap() {
    return tenantsUsersMap.keySet();
  }

  public Map<String, String> getStagingDirectorTenantsUsers() {
    return tenantsUsersMap;
  }

  private ConnectionSystemParameters enrichWithOkapiToken(ConnectionSystemParameters connectionSystemParameters) {
    final String token = ofNullable(authnClient.getApiKey(connectionSystemParameters, connectionSystemParameters.getTenantId())
      .getHeaders()
      .get(X_OKAPI_TOKEN)).orElseThrow(() -> new AuthorizationException("Cannot retrieve okapi token"))
        .get(0);
    connectionSystemParameters.setOkapiToken(token);
    return connectionSystemParameters;
  }

  private Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile)
          .matches()) {
          url = new URL(secureStorePropFile);
        }
        try (InputStream in = url == null ? new FileInputStream(new File(secureStorePropFile)) : url.openStream()) {
          secureStoreProps.load(in);
          log.info("Successfully loaded properties from: " + secureStorePropFile);
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      log.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }
}
