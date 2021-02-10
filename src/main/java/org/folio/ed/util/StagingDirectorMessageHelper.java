package org.folio.ed.util;

import org.folio.ed.domain.dto.AccessionQueueRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.folio.ed.util.MessageTypes.HEARTBEAT;
import static org.folio.ed.util.MessageTypes.INVENTORY_ADD;
import static org.folio.ed.util.MessageTypes.TRANSACTION_RESPONSE;
import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;

public final class StagingDirectorMessageHelper {
  private static final int MAX_TRANSACTION_NUMBER = 99999;
  private static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmss";

  private static final int AUTHOR_SIZE = 50;
  private static final int CALL_NUM_SIZE = 50;
  private static final int ERROR_REASON_SIZE = 3;
  private static final int MSG_TYPE_SIZE = 2;
  private static final int SKU_SIZE = 14;
  private static final int TIMESTAMP_SIZE = 14;
  private static final int TITLE_SIZE = 35;
  private static final int TRANS_NUM_SIZE = 5;

  private static final int SKU_POSITION = MSG_TYPE_SIZE + TRANS_NUM_SIZE + TIMESTAMP_SIZE;
  private static final int ERROR_REASON_POSITION = SKU_POSITION + SKU_SIZE;

  private static int transactionNumber = 1;

  private StagingDirectorMessageHelper(){}

  public static String buildInventoryAddMessage(AccessionQueueRecord record) {
    return buildMessageHeader(INVENTORY_ADD.getCode()) +
      formatValue(SKU_SIZE, record.getItemBarcode()) +
      formatValue(CALL_NUM_SIZE, record.getCallNumber()) +
      formatValue(TITLE_SIZE, record.getInstanceTitle()) +
      formatValue(AUTHOR_SIZE, record.getInstanceAuthor());
  }

  public static String buildHeartbeatMessage() {
    return buildMessageHeader(HEARTBEAT.getCode());
  }

  public static String buildTransactionResponseMessage(String message) {
    return TRANSACTION_RESPONSE.getCode() +
      message.substring(MSG_TYPE_SIZE, MSG_TYPE_SIZE + TRANS_NUM_SIZE) +
      getTimestampString() +
      SUCCESS.getValue();
  }

  public static MessageTypes resolveMessageType(String message) {
    return MessageTypes.fromCode(message.substring(0, MSG_TYPE_SIZE));
  }

  public static String extractBarcode(String statusMessage) {
    return statusMessage.substring(SKU_POSITION, SKU_POSITION + SKU_SIZE).trim();
  }

  public static StagingDirectorErrorCodes extractErrorCode(String statusMessage) {
    return StagingDirectorErrorCodes.fromCode(statusMessage
      .substring(ERROR_REASON_POSITION, ERROR_REASON_POSITION + ERROR_REASON_SIZE));
  }

  private static String formatValue(int size, String value) {
    return String.format("%-" + size + "." + size + "s", value);
  }

  private static String buildMessageHeader(String messageType) {
    return messageType + getTransactionNumberString() + getTimestampString();
  }

  private static String getTransactionNumberString() {
    transactionNumber = (transactionNumber > MAX_TRANSACTION_NUMBER) ? 1 : transactionNumber;
    return String.format("%0" + TRANS_NUM_SIZE + "d", transactionNumber++);
  }

  private static String getTimestampString() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
  }
}
