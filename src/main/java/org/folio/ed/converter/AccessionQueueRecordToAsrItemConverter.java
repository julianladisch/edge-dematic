package org.folio.ed.converter;

import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.AsrItem;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccessionQueueRecordToAsrItemConverter implements Converter<AccessionQueueRecord, AsrItem> {
  @Override
  public AsrItem convert(AccessionQueueRecord accessionQueueRecord) {
    return new AsrItem().title(accessionQueueRecord.getInstanceTitle())
      .author(accessionQueueRecord.getInstanceAuthor())
      .itemNumber(accessionQueueRecord.getItemBarcode())
      .callNumber(accessionQueueRecord.getCallNumber());
  }
}
