package org.folio.ed.handler;

import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.SecurityManagerService;
import org.folio.ed.util.MessageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatusMessageHandler implements GenericHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatusMessageHandler.class);
  private Configuration configuration;

  private final RemoteStorageService remoteStorageService;
  private final SecurityManagerService sms;

  public GenericHandler<String> withConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public Object handle(String payload, MessageHeaders messageHeaders) {
    LOGGER.info("Status handler income: {}", payload);
    if ((resolveMessageType(payload) == MessageTypes.INVENTORY_CONFIRM) && (extractErrorCode(payload) == SUCCESS)) {
      var tenantId = configuration.getTenantId();
      remoteStorageService.setAccessionedByBarcode(extractBarcode(payload), tenantId,
          sms.getStagingDirectorConnectionParameters(tenantId)
            .getOkapiToken());
    }
    return buildTransactionResponseMessage(payload);
  }
}
