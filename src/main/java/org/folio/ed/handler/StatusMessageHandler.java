package org.folio.ed.handler;

import lombok.RequiredArgsConstructor;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.util.MessageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

@Component
@RequiredArgsConstructor
public class StatusMessageHandler implements GenericHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatusMessageHandler.class);

  private final RemoteStorageService remoteStorageService;

  @Override
  public Object handle(String payload, MessageHeaders messageHeaders) {
    LOGGER.info("Status handler income: {}", payload);
    if ((resolveMessageType(payload) == MessageTypes.INVENTORY_CONFIRM) &&
      (extractErrorCode(payload) == SUCCESS)) {
      remoteStorageService.setAccessionedByBarcode(extractBarcode(payload));
    }
    return buildTransactionResponseMessage(payload);
  }
}
