package info.ejava.examples.app.gesture.client.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.common.web.ServerConfig;
import info.ejava.examples.app.common.web.client.GesturesAPISyncWebClient;
import info.ejava.examples.app.gesture.client.ClientTestBaseConfiguration;

/**
 * A test configuration used by remote IT test clients.
 */
@TestConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties //used to set it.server properties
public class ClientITTestConfiguration extends ClientTestBaseConfiguration {
    @Bean
    @ConfigurationProperties("it.server")
    public ServerConfig itServerConfig() {
        return new ServerConfig();
    }

    @Bean
    @Qualifier("webclient")
    public GestureApi gesturesWebClient(WebClient webClient, ServerConfig cfg) {
        return new GesturesAPISyncWebClient(webClient, cfg);
    }

}
