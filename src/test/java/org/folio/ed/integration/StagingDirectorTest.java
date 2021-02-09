package org.folio.ed.integration;

import static org.awaitility.Awaitility.await;
import static org.folio.ed.config.ServerMessageService.HEARTBEAT_MESSAGE;
import static org.folio.ed.config.ServerMessageService.TRANSACTION_RESPONSE_MESSAGE;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.folio.ed.TestBase;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.config.MockServerConfig;
import org.folio.ed.config.ServerMessageService;
import org.folio.ed.service.MessageService;
import org.folio.ed.service.StagingDirectorFlowsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import java.util.concurrent.TimeUnit;

@Import(MockServerConfig.class)
public class StagingDirectorTest extends TestBase {
  @Autowired
  private StagingDirectorFlowsService flowsService;

  @Autowired
  private IntegrationFlowContext integrationFlowContext;

  @SpyBean
  private MessageService messageService;

  @SpyBean
  private ServerMessageService serverMessageService;

  @Test
  void shouldReceiveServerResponseOnHeartbeatMessage() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelHeartbeatPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageService, times(1))
        .handleIncomingMessage(matches("HM00001\\d{14}"), anyMap());
      verify(messageService, times(1))
        .handleResponse(eq(TRANSACTION_RESPONSE_MESSAGE), anyMap());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldReceiveHeartbeatMessageFromStatusChannel() {
    serverMessageService.setMessageType("HM");
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(messageService, times(1))
        .handleStatusMessage(eq(HEARTBEAT_MESSAGE), anyMap());
      verify(serverMessageService, times(1))
        .handleIncomingMessage(matches("TR00001\\d{14}000"), anyMap());
    });

    integrationFlowContext.remove(f1.getId());
  }

  @Test
  void shouldSendInventoryAddMessageWhenNewItemIsPresent() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelAccessionPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageService, times(1))
        .handleIncomingMessage(matches("IA\\d{5}\\d{14}697685458679\\s{2}some-callnumber\\s{35}Nod\\s{32}Barnes, Adrian\\s{36}"), anyMap());
      verify(messageService, times(1))
        .handleResponse(eq(TRANSACTION_RESPONSE_MESSAGE), anyMap());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSetAccessionedWhenInventoryConfirmReceived() {
    serverMessageService.setMessageType("IC");
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->
      verify(serverMessageService, times(1))
        .handleIncomingMessage(matches("TR00001\\d{14}000"), anyMap()));

    integrationFlowContext.remove(f1.getId());
  }

  private Configuration buildConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setId("de17bad7-2a30-4f1c-bee5-f653ded15629");
    configuration.setName("RS1");
    configuration.setProviderName("Dematic_SD");
    configuration.setUrl("localhost:10001");
    configuration.setStatusUrl("localhost:10002");
    configuration.setAccessionDelay(2);
    configuration.setAccessionTimeUnit("minutes");
    return configuration;
  }
}
