package org.folio.ed.client;

import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configurations;
import org.folio.ed.domain.dto.ResultList;
import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.ed.domain.request.ItemBarcodeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "remote-storage")
public interface RemoteStorageClient {

  @GetMapping(path = "/accessions", produces = "application/json")
  ResultList<AccessionQueueRecord> getAccessionsByQuery(@RequestParam("query") String query);

  @GetMapping(path = "/retrievals", produces = "application/json")
  ResultList<RetrievalQueueRecord> getRetrievalsByQuery(@RequestParam("query") String query);

  @PutMapping("/accessions/barcode/{barcode}")
  ResponseEntity<String> setAccessionedByBarcode(@PathVariable("barcode") String barcode);

  @PutMapping("/retrievals/barcode/{barcode}")
  ResponseEntity<String> setRetrievalByBarcode(@PathVariable("barcode") String barcode);

  @PostMapping("/retrieve/{configurationId}/checkInItem")
  ResponseEntity<String> checkInItem(@PathVariable("configurationId") String configurationId,
      ItemBarcodeRequest itemBarcodeRequest);

  @GetMapping("/configurations")
  Configurations getStorageConfigurations();
}
