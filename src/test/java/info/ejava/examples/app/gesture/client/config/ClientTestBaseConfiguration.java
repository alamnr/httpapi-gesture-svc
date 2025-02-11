package info.ejava.examples.app.gesture.client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import info.ejava.examples.app.common.web.RestTemplateLoggingFilter;
import info.ejava.examples.app.common.web.WebLoggingFilter;
import info.ejava.examples.app.common.webflux.WebClientLoggingFilter;


@TestConfiguration
public class ClientTestBaseConfiguration {

    @Bean
    ClientHttpRequestFactory requestFactory(){
            return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, 
                                    ClientHttpRequestFactory requestFactory){
        return builder.requestFactory(
            // used to read the stream twice
            ()-> new BufferingClientHttpRequestFactory(requestFactory))
                                        .interceptors(new RestTemplateLoggingFilter()) 
                                        .build();        
        
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder, 
                                 ClientHttpRequestFactory requestFactory)
    {
        return builder // requestFactory used to read stream twice
                .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
                .requestInterceptor(new RestTemplateLoggingFilter())
                .build();
    
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        return builder  
                .filter(WebClientLoggingFilter.requestFilter())
                .filter(WebClientLoggingFilter.responseFilter())
                .build();
    }

}
