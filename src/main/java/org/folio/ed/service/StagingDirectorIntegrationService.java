package org.folio.ed.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolveAddress;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePollingTimeFrame;
import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePort;

import lombok.RequiredArgsConstructor;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.handler.FeedbackChannelHandler;
import org.folio.ed.handler.PrimaryChannelHandler;
import org.folio.ed.handler.StatusChannelHandler;
import org.folio.ed.util.StagingDirectorMessageHelper;
import org.folio.ed.util.StagingDirectorSerializerDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StagingDirectorIntegrationService {
  private static final String POLLER_CHANNEL_POSTFIX = "_pollerChannel";
  private static final String FEEDBACK_CHANNEL_POSTFIX = "_feedbackChannel";

  @Value("${primary.channel.heartbeat.timeframe}")
  private long heartbeatTimeframe;

  @Value("${primary.channel.response.timeout}")
  private int responseTimeout;

  private final IntegrationFlowContext integrationFlowContext;
  private final RemoteStorageService remoteStorageService;
  private final StatusChannelHandler statusChannelHandler;
  private final PrimaryChannelHandler primaryChannelHandler;
  private final FeedbackChannelHandler feedbackChannelHandler;
  private final StagingDirectorSerializerDeserializer serializerDeserializer;
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
    registerFeedbackChannelListener(configuration);
    registerPrimaryChannelOutboundGateway(configuration);
    registerPrimaryChannelHeartbeatPoller(configuration);
    registerPrimaryChannelAccessionPoller(configuration);
    registerPrimaryChannelRetrievalPoller(configuration);
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
        .from(configuration.getName() + POLLER_CHANNEL_POSTFIX)
        .<String>handle((p, h) -> primaryChannelHandler.handle(p, configuration))
        .handle(Tcp
          .outboundGateway(Tcp
            .netClient(resolveAddress(configuration.getUrl()), resolvePort(configuration.getUrl()))
            .soTimeout(responseTimeout)
            .serializer(serializerDeserializer)
            .deserializer(serializerDeserializer)))
        .<String>handle((p, h) -> primaryChannelHandler.handle(p, configuration))
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelHeartbeatPoller(
    Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(StagingDirectorMessageHelper::buildHeartbeatMessage,
          p -> p.poller(Pollers.fixedDelay(heartbeatTimeframe)))
        .channel(configuration.getName() + POLLER_CHANNEL_POSTFIX)
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelAccessionPoller(
    Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(() -> {
            var connectionSystemParameters = sms.getStagingDirectorConnectionParameters(configuration.getTenantId());
            return remoteStorageService.getAccessionQueueRecords(configuration.getId(), connectionSystemParameters.getTenantId(),
              connectionSystemParameters.getOkapiToken());
          },
          p -> p.poller(Pollers.fixedDelay(resolvePollingTimeFrame(configuration.getAccessionDelay(),
            configuration.getAccessionTimeUnit()))))
        .split()
        .transform(StagingDirectorMessageHelper::buildInventoryAddMessage)
        .channel(configuration.getName() + POLLER_CHANNEL_POSTFIX)
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerPrimaryChannelRetrievalPoller(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(() -> remoteStorageService.getRetrievalQueueRecords(configuration.getId(), configuration.getTenantId(),
          sms.getStagingDirectorConnectionParameters(configuration.getTenantId()).getOkapiToken()),
          p -> p.poller(Pollers.fixedDelay(resolvePollingTimeFrame(configuration.getAccessionDelay(),
            configuration.getAccessionTimeUnit()))))
        .split()
        .transform(StagingDirectorMessageHelper::buildStatusCheckMessage)
        .channel(configuration.getName() + POLLER_CHANNEL_POSTFIX)
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerFeedbackChannelListener(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(MessageChannels.publishSubscribe(configuration.getName() + FEEDBACK_CHANNEL_POSTFIX))
        .<String>handle((p, h) -> feedbackChannelHandler.handle(p, configuration.getId()))
        .channel(MessageChannels.publishSubscribe(configuration.getName() + POLLER_CHANNEL_POSTFIX))
        .get())
      .register();
  }

  public IntegrationFlowContext.IntegrationFlowRegistration registerStatusChannelFlow(Configuration configuration) {
    return integrationFlowContext
      .registration(IntegrationFlows
        .from(Tcp
          .inboundGateway(Tcp
            .netClient(resolveAddress(configuration.getStatusUrl()), resolvePort(configuration.getStatusUrl()))
              .singleUseConnections(false)
              .serializer(serializerDeserializer)
              .deserializer(serializerDeserializer))
          .clientMode(true)
          .retryInterval(SECONDS.toMillis(1)))
        .channel(configuration.getName() + FEEDBACK_CHANNEL_POSTFIX)
        .<String>handle((p, h) -> statusChannelHandler.handle(p, configuration))
        .get())
      .register();
  }
}
