package online.worldseed.config;

import online.worldseed.model.properties.GeneratorProperties;
import online.worldseed.model.properties.SrtmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableJpaAuditing
@EnableConfigurationProperties({GeneratorProperties.class, SrtmProperties.class})
public class AppConfig {
}
