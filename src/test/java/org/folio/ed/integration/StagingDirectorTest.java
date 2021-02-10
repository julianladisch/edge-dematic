package org.folio.ed.integration;

import static org.awaitility.Awaitility.await;
import static org.folio.ed.support.ServerMessageHandler.TRANSACTION_RESPONSE_MESSAGE;
import static org.folio.ed.support.ServerMessageHelper.HEARTBEAT_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.folio.ed.TestBase;
import org.folio.ed.service.StagingDirectorFlowsService;
import org.folio.ed.support.ServerMessageHandler;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.config.MockServerConfig;
import org.folio.ed.support.ServerMessageHelper;
import org.folio.ed.handler.ResponseHandler;
import org.folio.ed.handler.StatusMessageHandler;
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

  @Autowired
  private ServerMessageHelper serverMessageHelper;

  @SpyBean
  private StatusMessageHandler statusMessageHandler;

  @SpyBean
  private ResponseHandler responseHandler;

  @SpyBean
  private ServerMessageHandler serverMessageHandler;

  @Test
  void shouldReceiveServerResponseOnHeartbeatMessage() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelHeartbeatPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler, times(1))
        .handle(matches("HM00001\\d{14}"), any());
      verify(responseHandler, times(1))
        .handle(eq(TRANSACTION_RESPONSE_MESSAGE), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldReceiveHeartbeatMessageFromStatusChannel() {
    serverMessageHelper.setMessageType("HM");
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(statusMessageHandler, times(1))
        .handle(eq(HEARTBEAT_MESSAGE), any());
      verify(serverMessageHandler, times(1))
        .handle(matches("TR00001\\d{14}000"), any());
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
      verify(serverMessageHandler, times(1))
        .handle(matches("IA\\d{5}\\d{14}697685458679\\s{2}some-callnumber\\s{35}Nod\\s{32}Barnes, Adrian\\s{36}"), any());
      verify(responseHandler, times(1))
        .handle(eq(TRANSACTION_RESPONSE_MESSAGE), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSetAccessionedWhenInventoryConfirmReceived() {
    serverMessageHelper.setMessageType("IC");
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->
      verify(serverMessageHandler, times(1))
        .handle(matches("TR00001\\d{14}000"), any()));

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
