package org.folio.ed.client;

import org.folio.ed.domain.dto.Instance;
import org.folio.ed.domain.dto.ResultList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "inventory")
public interface InstancesClient extends QueryableClient<Instance> {
  @Override
  @GetMapping("/instances")
  ResultList<Instance> query(@RequestParam("query") String query);
}
