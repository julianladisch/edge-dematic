package org.folio.ed.handler;

import static org.folio.ed.util.MessageTypes.STATUS_MESSAGE;
import static org.folio.ed.util.StagingDirectorErrorCodes.INVENTORY_IS_AVAILABLE;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildPickRequestMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.service.RemoteStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackChannelHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackChannelHandler.class);

  private final RemoteStorageService remoteStorageService;

  public Object handle(String payload, String configId) {
    LOGGER.info("Feedback channel handler income: {}", payload);
    if ((resolveMessageType(payload) == STATUS_MESSAGE) &&
      (extractErrorCode(payload) == INVENTORY_IS_AVAILABLE)) {
      return buildPickRequestMessage(remoteStorageService.getRetrievalByBarcode(extractBarcode(payload), configId));
    }
    return null;
  }
}
