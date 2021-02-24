package org.folio.ed.support;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Configuration
public class XmlConfig {

  @Bean
  public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(Jackson2ObjectMapperBuilder builder) {
    JacksonXmlModule module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    XmlMapper xmlMapper = builder.createXmlMapper(true)
      .defaultUseWrapper(false)
      .build();
    xmlMapper.enable(INDENT_OUTPUT);
    return new MappingJackson2XmlHttpMessageConverter(xmlMapper);
  }
}
