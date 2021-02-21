package org.folio.ed.service;

import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolveAddress;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePollingTimeFrame;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePort;

import lombok.RequiredArgsConstructor;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.handler.ResponseHandler;
import org.folio.ed.handler.StatusMessageHandler;
import org.folio.ed.util.StagingDirectorMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.dsl.TcpClientConnectionFactorySpec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StagingDirectorFlowsService {

  @Value("${primary.channel.heartbeat.timeframe}")
  private long heartbeatTimeframe;

  private final IntegrationFlowContext integrationFlowContext;
  private final RemoteStorageService remoteStorageService;
  private final StatusMessageHandler statusMessageHandler;
  private final ResponseHandler responseHandler;
  private final SecurityManagerService sms;

  @Scheduled(fixedDelayString = "${configurations.update.timeframe}")
  public void updateIntegrationFlows() {
    removeExistingFlows();
    var tenantsUsersMap = sms.getStagingDirectorTenantsUsers();
    for (String tenantId : tenantsUsersMap.keySet()) {
      for (Configuration configuration : remoteStorageService.getStagingDirectorConfigurations(tenantId, sms.getStagingDirectorConnectionParameters(tenantId).getOkapiToken())) {
        createFlows(configuration);
      }
    }
  }

  private void createFlows(Configuration configuration) {
    registerPrimaryChannelOutboundGateway(configuration);
    registerPrimaryChannelHeartbeatPoller(configuration);
    registerPrimaryChannelAccessionPoller(configuration);
    registerStatusChannelFlow(configuration);
  }

  public void removeExistingFlows() {
    integrationFlowContext.getRegistry().keySet().forEach(key -> {
      integrationFlowContext.getRegistrationById(key).stop();
      integrationFlowContext.remove(key);
    });
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelOutboundGateway(
    Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(MessageChannels.publishSubscribe(configuration.getName()))
        .handle(Tcp.outboundGateway(Tcp
          .netClient(resolveAddress(configuration.getUrl()), resolvePort(configuration.getUrl()))))
        .handle(String.class, responseHandler)
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelHeartbeatPoller(
    Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(StagingDirectorMessageHelper::buildHeartbeatMessage,
          p -> p.poller(Pollers.fixedDelay(heartbeatTimeframe)))
        .channel(configuration.getName())
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelAccessionPoller(
    Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(() -> {
            var connectionSystemParameters = sms.getStagingDirectorConnectionParameters(configuration.getTenantId());
            return remoteStorageService
              .getAccessionQueueRecords(configuration.getId(), connectionSystemParameters.getTenantId(), connectionSystemParameters.getOkapiToken());
          },
          p -> p.poller(Pollers.fixedDelay(
            resolvePollingTimeFrame(configuration.getAccessionDelay(),
              configuration.getAccessionTimeUnit()))))
        .split()
        .transform(StagingDirectorMessageHelper::buildInventoryAddMessage)
        .channel(configuration.getName())
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerStatusChannelFlow(
    Configuration configuration) {
    TcpClientConnectionFactorySpec statusChannelFactory =
      Tcp.netClient(resolveAddress(configuration.getStatusUrl()),
        resolvePort(configuration.getStatusUrl()))
        .singleUseConnections(false);
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(Tcp.inboundGateway(statusChannelFactory).clientMode(true))
        .handle(String.class, statusMessageHandler.withConfiguration(configuration))
        .get())
      .register();
  }
}
