package org.folio.ed.client;


import org.folio.ed.domain.dto.ResultList;
import org.folio.ed.domain.entity.AccessionQueueRecord;
import org.folio.ed.domain.entity.Configuration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "remote-storage")
public interface RemoteStorageClient extends QueryableClient<AccessionQueueRecord> {
  @Override
  @GetMapping("/accessions")
  ResultList<AccessionQueueRecord> query(@RequestParam("query") String query);

  @PutMapping("/accessions/barcode/{barcode}")
  String setAccessionedByBarcode(@PathVariable("barcode") String barcode);

  @GetMapping("/configurations")
  ResultList<Configuration> getConfigurations();
}
