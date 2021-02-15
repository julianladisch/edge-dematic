package org.folio.ed.client;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.folio.ed.domain.SystemParametersHolder;
import org.folio.ed.service.SecurityManagerService;
import org.springframework.beans.factory.annotation.Autowired;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EnrichHeadersClient extends Client.Default {

  @Autowired
  private SystemParametersHolder systemParametersHolder;

  public EnrichHeadersClient() {
    super(null, null);
  }

  @Override
  @SneakyThrows
  public Response execute(Request request, Options options) {

    FieldUtils.writeDeclaredField(request, "url", request.url()
      .replace("http://", systemParametersHolder.getOkapiUrl() + "/"), true);

    return super.execute(request, options);
  }
}
