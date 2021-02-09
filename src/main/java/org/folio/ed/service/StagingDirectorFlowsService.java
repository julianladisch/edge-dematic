package org.folio.ed.service;

import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolveAddress;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePollingTimeFrame;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePort;

import lombok.RequiredArgsConstructor;
import org.folio.ed.domain.AsyncFolioExecutionContext;
import org.folio.ed.domain.TenantHolder;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.util.StagingDirectorMessageHelper;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
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
  private final MessageService messageService;
  private final SecurityManagerService securityManagerService;
  private final TenantHolder tenantHolder;

  @Scheduled(fixedDelayString = "${configurations.update.timeframe}")
  public void updateIntegrationFlows() {
    stopAndRemoveAllFlows();
    var systemUserParameters = securityManagerService.getSystemUserParameters(tenantHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
      new AsyncFolioExecutionContext(systemUserParameters, null));
    remoteStorageService.getStagingDirectorConfigurations().forEach(this::createFlows);
  }

  private void createFlows(Configuration configuration) {
    registerPrimaryChannelOutboundGateway(configuration);
    registerPrimaryChannelHeartbeatPoller(configuration);
    registerPrimaryChannelAccessionPoller(configuration);
    registerStatusChannelFlow(configuration);
  }

  public void stopAndRemoveAllFlows() {
    integrationFlowContext.getRegistry().keySet().forEach(key -> {
      integrationFlowContext.getRegistrationById(key).stop();
      integrationFlowContext.remove(key);
    });
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelOutboundGateway(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(MessageChannels.publishSubscribe(configuration.getName()))
        .handle(Tcp.outboundGateway(Tcp.netClient(resolveAddress(configuration.getUrl()), resolvePort(configuration.getUrl()))))
        .handle(String.class, messageService::handleResponse)
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelHeartbeatPoller(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(StagingDirectorMessageHelper::buildHeartbeatMessage,
          p -> p.poller(Pollers.fixedDelay(heartbeatTimeframe)))
        .channel(configuration.getName())
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelAccessionPoller(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(() -> remoteStorageService.getAccessionQueueRecords(configuration.getId()),
          p -> p.poller(Pollers.fixedDelay(resolvePollingTimeFrame(configuration.getAccessionDelay(), configuration.getAccessionTimeUnit()))))
        .split()
        .transform(StagingDirectorMessageHelper::buildInventoryAddMessage)
        .channel(configuration.getName())
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerStatusChannelFlow(Configuration configuration) {
    TcpClientConnectionFactorySpec statusChannelFactory =
      Tcp.netClient(resolveAddress(configuration.getStatusUrl()), resolvePort(configuration.getStatusUrl()))
        .singleUseConnections(false);
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(Tcp.inboundGateway(statusChannelFactory).clientMode(true))
        .handle(String.class, messageService::handleStatusMessage)
        .get())
      .register();
  }
}
