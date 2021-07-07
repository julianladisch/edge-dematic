package org.folio.ed.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.folio.ed.util.Constants.COMMA;
import static org.folio.edge.core.Constants.X_OKAPI_TOKEN;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.folio.ed.client.AuthnClient;
import org.folio.ed.domain.entity.ConnectionSystemParameters;
import org.folio.ed.error.AuthorizationException;
import org.folio.ed.security.SecureStoreFactory;
import org.folio.ed.security.SecureTenantsProducer;
import org.folio.edge.core.model.ClientInfo;
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

  public static final String SYSTEM_USER_PARAMETERS_CACHE = "systemUserParameters";

  @Value("${secure_store}")
  private String secureStoreType;

  @Value("${secure_store_props}")
  private String secureStorePropsFile;

  @Value("${staging_director_tenants}")
  private String stagingDirectorTenants;

  @Value("${staging_director_client}")
  private String stagingDirectorClient;

  @Autowired
  private AuthnClient authnClient;

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");

  private SecureStore secureStore;

  private Map<String, String> stagingDirectorTenantsUserMap = new HashMap<>();

  @PostConstruct
  public void init() {

    Properties secureStoreProps = getProperties(secureStorePropsFile);

    secureStore = SecureStoreFactory.getSecureStore(secureStoreType, secureStoreProps);

    Optional<String> tenants = SecureTenantsProducer.getTenants(secureStoreProps, secureStore, stagingDirectorTenants);
    tenants.ifPresent(tenantsStr -> stagingDirectorTenantsUserMap = Arrays.stream(COMMA.split(tenantsStr))
      .collect(toMap(Function.identity(), tenant -> stagingDirectorClient)));
  }

  @Cacheable(value = SYSTEM_USER_PARAMETERS_CACHE, key = "#tenantId")
  public ConnectionSystemParameters getStagingDirectorConnectionParameters(String tenantId) {
    return enrichConnectionSystemParametersWithOkapiToken(stagingDirectorClient, tenantId, stagingDirectorTenantsUserMap.get(tenantId));
  }

  @Cacheable(value = SYSTEM_USER_PARAMETERS_CACHE, key = "#edgeApiKey")
  public ConnectionSystemParameters getOkapiConnectionParameters(String edgeApiKey) {
    try {
      ClientInfo clientInfo = ApiKeyUtils.parseApiKey(edgeApiKey);
      return enrichConnectionSystemParametersWithOkapiToken(clientInfo.salt, clientInfo.tenantId,
        clientInfo.username);
    } catch (ApiKeyUtils.MalformedApiKeyException e) {
      throw new AuthorizationException("Malformed edge api key: " + edgeApiKey);
    }
  }

  private ConnectionSystemParameters enrichConnectionSystemParametersWithOkapiToken(
    String salt, String tenantId, String username) {
    try {
      return enrichWithOkapiToken(ConnectionSystemParameters.builder()
        .tenantId(tenantId)
        .username(username)
        .password(secureStore.get(salt, tenantId, username))
        .build());
    } catch (NotFoundException e) {
      throw new AuthorizationException("Cannot get system connection properties for: " + tenantId);
    }
  }

  public Set<String> getStagingDirectorTenantsUserMap() {
    return stagingDirectorTenantsUserMap.keySet();
  }

  public Map<String, String> getStagingDirectorTenantsUsers() {
    return stagingDirectorTenantsUserMap;
  }

  private ConnectionSystemParameters enrichWithOkapiToken(ConnectionSystemParameters connectionSystemParameters) {
    final String token = ofNullable(authnClient.getApiKey(connectionSystemParameters, connectionSystemParameters.getTenantId())
      .getHeaders()
      .get(X_OKAPI_TOKEN)).orElseThrow(() -> new AuthorizationException("Cannot retrieve okapi token"))
        .get(0);
    connectionSystemParameters.setOkapiToken(token);
    return connectionSystemParameters;
  }

  private static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    log.info("Attempt to load properties from: " + secureStorePropFile);

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
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
