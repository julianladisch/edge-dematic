package org.folio.ed.client;

import org.folio.ed.domain.dto.ResultList;
import org.springframework.web.bind.annotation.RequestParam;

public interface QueryableClient<E> {
  ResultList<E> query(@RequestParam("query") String query);
}
