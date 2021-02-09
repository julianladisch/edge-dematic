package org.folio.ed.service;

import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

  private final RemoteStorageService remoteStorageService;

  public String handleStatusMessage(String payload, Map<String, Object> headers) {
    LOGGER.info("Client service income: " + payload);
    switch (resolveMessageType(payload)) {
      case INVENTORY_CONFIRM:
        if (extractErrorCode(payload) == SUCCESS) {
          remoteStorageService.setAccessionedByBarcode(extractBarcode(payload));
        }
    }
    return buildTransactionResponseMessage(payload);
  }

  public String handleResponse(String payload, Map<String, Object> headers) {
    LOGGER.info("Handle response: {}", payload);
    return null;
  }
}
