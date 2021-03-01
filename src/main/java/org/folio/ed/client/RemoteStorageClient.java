package org.folio.ed.client;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "remote-storage")
public interface RemoteStorageClient {

  @GetMapping(path = "/accessions", produces = "application/json")
  ResultList<AccessionQueueRecord> getAccessionsByQuery(@RequestParam("query") String query, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @GetMapping(path = "/retrievals", produces = "application/json")
  ResultList<RetrievalQueueRecord> getRetrievalsByQuery(@RequestParam("query") String query, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PutMapping("/accessions/barcode/{barcode}")
  ResponseEntity<String> setAccessionedByBarcode(@PathVariable("barcode") String barcode, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PutMapping("/retrievals/barcode/{barcode}")
  ResponseEntity<String> setRetrievalByBarcode(@PathVariable("barcode") String barcode, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PostMapping("/retrieve/{configurationId}/checkInItem")
  ResponseEntity<String> checkInItem(@PathVariable("configurationId") String configurationId, ItemBarcodeRequest itemBarcodeRequest,
      @RequestHeader(TENANT) String tenantId, @RequestHeader(TOKEN) String okapiToken);

  @GetMapping("/configurations")
  Configurations getStorageConfigurations(@RequestHeader(TENANT) String tenantId, @RequestHeader(TOKEN) String okapiToken);
}
