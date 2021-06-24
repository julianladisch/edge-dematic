package org.folio.ed.handler;

import static org.folio.ed.util.MessageTypes.INVENTORY_CONFIRM;
import static org.folio.ed.util.MessageTypes.ITEM_RETURNED;
import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.SecurityManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatusChannelHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatusChannelHandler.class);

  private final RemoteStorageService remoteStorageService;
  private final SecurityManagerService sms;

  public Object handle(String payload, Configuration configuration) {
    LOGGER.info("Status channel handler income: \"{}\"", payload);
    var tenantId = configuration.getTenantId();
    if ((resolveMessageType(payload) == INVENTORY_CONFIRM) && (extractErrorCode(payload) == SUCCESS)) {
      remoteStorageService.setAccessionedAsync(extractBarcode(payload), tenantId,
        sms.getStagingDirectorConnectionParameters(tenantId)
          .getOkapiToken());
    } else if (resolveMessageType(payload) == ITEM_RETURNED) {
      remoteStorageService.returnItemByBarcode(configuration.getId(), extractBarcode(payload), tenantId,
        sms.getStagingDirectorConnectionParameters(tenantId)
          .getOkapiToken());
    }
    var response = buildTransactionResponseMessage(payload);
    LOGGER.info("Status channel sending: \"{}\"", response);
    return response;
  }
}
