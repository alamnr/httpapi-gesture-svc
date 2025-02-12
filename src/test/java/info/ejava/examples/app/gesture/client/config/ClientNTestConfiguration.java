package info.ejava.examples.app.gesture.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.common.web.ServerConfig;
import info.ejava.examples.app.common.web.client.GestureApiTemplateClient;
import info.ejava.examples.app.common.web.client.GesturesAPIRestClient;
import info.ejava.examples.app.common.web.client.GesturesAPISyncWebClient;
import info.ejava.examples.app.gesture.client.ClientTestBaseConfiguration;


@TestConfiguration
public class ClientNTestConfiguration extends ClientTestBaseConfiguration {

    @Bean @Lazy
    public ServerConfig serverConfig(@LocalServerPort int port){
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    @Primary
    public GestureApi gesturesTemplateClient(RestTemplate restTemplate, ServerConfig cfg){
        return new GestureApiTemplateClient(restTemplate,cfg) ;
    }

    @Bean @Lazy
    @Qualifier("restclient")
    public GestureApi gesturesRestClient(RestClient restClient, ServerConfig serverConfig) {
        return new GesturesAPIRestClient(restClient, serverConfig);
    }

    @Bean @Lazy
    @Qualifier("webclient")
    public GestureApi gesturesWebClient(WebClient webClient, ServerConfig cfg) {
        return new GesturesAPISyncWebClient(webClient, cfg);
    }



}
