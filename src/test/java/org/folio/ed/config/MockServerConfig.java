package org.folio.ed.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.dsl.TcpServerConnectionFactorySpec;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.support.MessageBuilder;

@TestConfiguration
public class MockServerConfig {
  @Autowired
  private ServerMessageService serverMessageService;

  // primary channel stub server

  @Bean
  public TcpServerConnectionFactorySpec primaryChannelFactory() {
    return Tcp.netServer(10001);
  }

  @Bean
  public IntegrationFlow primaryChannelFlow() {
    return IntegrationFlows
      .from(Tcp.inboundGateway(primaryChannelFactory()))
      .handle(String.class, serverMessageService::handleIncomingMessage)
      .get();
  }

  // status channel stub server

  @Bean
  public TcpServerConnectionFactorySpec statusChannelFactory() {
    return Tcp.netServer(10002);
  }

  @Bean
  public ApplicationEventListeningMessageProducer eventsProducer() {
    ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
    producer.setEventTypes(TcpConnectionOpenEvent.class);
    return producer;
  }

  @Bean
  public IntegrationFlow statusChannelOutboundFlow() {
    return IntegrationFlows.from(eventsProducer())
      .transform(e -> MessageBuilder.withPayload(serverMessageService.getMessage())
        .setHeader(IpHeaders.CONNECTION_ID, ((TcpConnectionOpenEvent) e).getConnectionId()).build())
      .handle(Tcp.outboundAdapter(statusChannelFactory()))
      .get();
  }

  @Bean
  public IntegrationFlow statusChannelInboundFlow() {
    return IntegrationFlows.from(Tcp.inboundAdapter(statusChannelFactory()))
      .transform(Transformers.objectToString())
      .handle(serverMessageService::handleIncomingMessage)
      .get();
  }
}
