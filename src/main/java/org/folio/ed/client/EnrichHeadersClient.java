package org.folio.ed.client;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Value;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EnrichHeadersClient extends Client.Default {

  @Value("${okapi_url}")
  private String okapiUrl;

  public EnrichHeadersClient() {
    super(null, null);
  }

  @Override
  @SneakyThrows
  public Response execute(Request request, Options options) {

    FieldUtils.writeDeclaredField(request, "url", request.url().replace("http://", okapiUrl + "/"), true);

    return super.execute(request, options);
  }
}
