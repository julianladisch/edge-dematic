package org.folio.ed.handler;

import static org.folio.ed.util.MessageTypes.STATUS_MESSAGE;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildPickRequestMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.SecurityManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackChannelHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackChannelHandler.class);

  private final RemoteStorageService remoteStorageService;
  private final SecurityManagerService sms;

  public Object handle(String payload, Configuration configuration) {
    LOGGER.info("Feedback channel handler income: \"{}\"", payload);
    var tenantId = configuration.getTenantId();
    var okapiToken = sms.getStagingDirectorConnectionParameters(tenantId).getOkapiToken();
    if (resolveMessageType(payload) == STATUS_MESSAGE) {
      var errorCode = extractErrorCode(payload);
      switch (errorCode) {
        case INVENTORY_IS_AVAILABLE:
          return buildPickRequestMessage(remoteStorageService.getRetrievalByBarcode(extractBarcode(payload), configuration.getId()));
        case INVENTORY_NOT_IN_DATABASE:
        case SKU_NOT_IN_DATABASE:
        case INVALID_SKU_FORMAT:
          remoteStorageService.markItemAsMissingAsync(extractBarcode(payload), tenantId, okapiToken);
          remoteStorageService.setRetrievedAsync(extractBarcode(payload), tenantId, okapiToken);
          return null;
        case INVENTORY_ALREADY_COMMITTED:
        case INVENTORY_IS_NOT_AVAILABLE:
          remoteStorageService.setRetrievedAsync(extractBarcode(payload), tenantId, okapiToken);
          return null;
        default:
          return null;
      }
    }
    return null;
  }
}
