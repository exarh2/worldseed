package online.worldseed.config;

import online.worldseed.config.properties.GeneratorProperties;
import online.worldseed.config.properties.SrtmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableJpaAuditing
@EnableConfigurationProperties({GeneratorProperties.class, SrtmProperties.class})
public class AppConfig {

    @Bean
    ThreadPoolTaskExecutor terrainGeneratorTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //TODO в конфиг
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(3);
        return taskExecutor;
    }
}
