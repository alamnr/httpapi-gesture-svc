package info.ejava.examples.app;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class GuesturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuesturesApplication.class, args);
    }

    /*
     * Implements server side logging
     */

     @Bean
     // activate if client side logging is not defined
     @ConditionalOnProperty(value="logging.level.info.ejava.examples.app", havingValue="OFF", matchIfMissing=true)
     public Filter logFilter(){
        final List<String> headers = Arrays.asList("accept host content-length Content-Type     accept-encoding".toLowerCase().split(","));
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setBeforeMessagePrefix(System.lineSeparator());
        filter.setAfterMessagePrefix(System.lineSeparator());
        filter.setIncludeHeaders(true);
        filter.setHeaderPredicate(h -> headers.contains(h));
        return filter;

     }
}
