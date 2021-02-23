package org.folio.ed.converter;

import static java.util.Objects.nonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.rs.domain.dto.AsrRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RetrievalQueueRecordToAsrRequestConverter implements Converter<RetrievalQueueRecord, AsrRequest> {

  private static final String EMS_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.S";

  @Override
  public AsrRequest convert(RetrievalQueueRecord retrievalQueueRecord) {
    return new AsrRequest().holdId(retrievalQueueRecord.getHoldId())
      .itemBarcode(retrievalQueueRecord.getItemBarcode())
      .title(retrievalQueueRecord.getInstanceTitle())
      .author(retrievalQueueRecord.getInstanceAuthor())
      .callNumber(retrievalQueueRecord.getCallNumber())
      .patronBarcode(retrievalQueueRecord.getPatronBarcode())
      .patronName(retrievalQueueRecord.getPatronName())
      .requestDate(convertToString(retrievalQueueRecord.getCreatedDateTime()))
      .pickupLocation(retrievalQueueRecord.getPickupLocation())
      .requestStatus(retrievalQueueRecord.getRequestStatus())
      .requestNote(retrievalQueueRecord.getRequestNote());
  }

  private String convertToString(LocalDateTime dateToConvert) {
    if (nonNull(dateToConvert)) {
      return dateToConvert.format(DateTimeFormatter.ofPattern(EMS_DATE_TIME_PATTERN));
    } else {
      return null;
    }
  }
}
