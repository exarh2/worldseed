package ru.synchro.tcc.mobile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.synchro.tcc.mobile.config.properties.OpenAPIProperties;

import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(OpenAPIProperties.class)
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "bearerAuth", scheme = "bearer", bearerFormat = "JWT")
public class SpringDocConfiguration {

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE));
    }

    @Bean
    OpenAPI openAPI(OpenAPIProperties properties) {
        var info = new Info()
            .title(properties.getTitle())
            .description(properties.getDescription());

        var servers = properties.getServers().stream()
            .map(e -> new Server().url(e.getUrl()).description(e.getDescription()))
            .collect(Collectors.toList());

        return new OpenAPI()
            .info(info)
            .servers(servers);
    }
}
