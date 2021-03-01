package org.folio.ed.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.folio.ed.domain.entity.RequestWithHeaders;
import org.folio.ed.error.AuthorizationException;
import org.folio.ed.service.SecurityManagerService;
import org.folio.ed.util.ApiKeyHelper;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EdgeSecurityFilter implements Filter {

  private final SecurityManagerService securityManagerService;
  private final ApiKeyHelper apiKeyHelper;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    var edgeApiKey = apiKeyHelper.getEdgeApiKey(request);

    if (StringUtils.isEmpty(edgeApiKey)) {
      throw new AuthorizationException("Edge API key not found in the request");
    }

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    RequestWithHeaders wrapper = new RequestWithHeaders(httpRequest);

    var systemUserParameters = securityManagerService.getOkapiConnectionParameters(edgeApiKey);

    wrapper.putHeader(XOkapiHeaders.TOKEN, systemUserParameters.getOkapiToken());
    wrapper.putHeader(XOkapiHeaders.TENANT, systemUserParameters.getTenantId());

    filterChain.doFilter(wrapper, response);
  }
}
