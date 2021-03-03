package org.folio.ed.support;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SchedulingConfig {
}
