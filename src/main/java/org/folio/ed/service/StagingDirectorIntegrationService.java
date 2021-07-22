package org.folio.ed.service;

import static java.util.Objects.isNull;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StagingDirectorIntegrationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(StagingDirectorIntegrationService.class);

  private static final String POLLER_CHANNEL_POSTFIX = "_pollerChannel";
  private static final String FEEDBACK_CHANNEL_POSTFIX = "_feedbackChannel";

  @Value("${primary.channel.heartbeat.timeframe}")
  private long heartbeatTimeframe;

  private final IntegrationFlowContext integrationFlowContext;
  private final RemoteStorageService remoteStorageService;
  private final StatusChannelHandler statusChannelHandler;
  private final PrimaryChannelHandler primaryChannelHandler;
  private final FeedbackChannelHandler feedbackChannelHandler;
  private final StagingDirectorSerializerDeserializer serializerDeserializer;
  private final SecurityManagerService sms;

  @PostConstruct
  private void createIntegrationFlows() {
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
        .from(() -> buildHeartbeatMessageIfNeeded(configuration),
          p -> p.poller(Pollers.fixedDelay(SECONDS.toMillis(1))))
        .channel(configuration.getName() + POLLER_CHANNEL_POSTFIX)
        .get())
      .register();
  }

  private String buildHeartbeatMessageIfNeeded(Configuration configuration) {
    if (isNull(remoteStorageService.getLastMessageTime(configuration.getId())) ||
      remoteStorageService.getLastMessageTime(configuration.getId()).plusSeconds(heartbeatTimeframe).isBefore(LocalDateTime.now())) {
      return StagingDirectorMessageHelper.buildHeartbeatMessage();
    }
    return null;
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
        .<String>handle((p, h) -> feedbackChannelHandler.handle(p, configuration))
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
              .soTimeout((int) SECONDS.toMillis(60))
              .serializer(serializerDeserializer)
              .deserializer(serializerDeserializer))
          .clientMode(true)
          .retryInterval(SECONDS.toMillis(1)))
        .channel(configuration.getName() + FEEDBACK_CHANNEL_POSTFIX)
        .<String>handle((p, h) -> statusChannelHandler.handle(p, configuration))
        .get())
      .register();
  }

  @EventListener(TcpConnectionOpenEvent.class)
  public void handleConnectionOpenEvent(TcpConnectionOpenEvent event) {
    LOGGER.info("Open connection: {}", event);
  }

  @EventListener(TcpConnectionCloseEvent.class)
  public void handleConnectionCloseEvent(TcpConnectionCloseEvent event) {
    LOGGER.info("Close connection: {}", event);
  }
}
