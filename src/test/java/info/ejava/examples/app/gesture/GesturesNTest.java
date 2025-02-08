package info.ejava.examples.app.gesture;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.examples.app.controllers.ExceptionAdvice;
import info.ejava.examples.app.controllers.GesturesController;
import info.ejava.examples.app.svc.GestureService;
import lombok.extern.slf4j.Slf4j;

/*
 * This test is an example of integration unit test where we leverage
 * the spring context to provide us with the components of the application
 * but issue the commands directly to the controller class as if it were a POJO
 */

@SpringBootTest
@ActiveProfiles("test")
@Tag("springboot")
@DisplayName("Gestures integration unit test")
@Slf4j
public class GesturesNTest {

    @Autowired
    private GesturesController gesturesController;

    @Autowired
    private ExceptionAdvice exceptionAdvice;

    private String currentRequestUrl;

    @BeforeEach
    public void setUp(){
        currentRequestUrl  = ServletUriComponentsBuilder.fromCurrentRequest()
                                                        .build().toUriString();
        gesturesController.deleteAllGesture();
    }

    @Test
    public void add_new_gesture(){
        
        // when - adding a new gesture
        ResponseEntity<String> response = gesturesController.upsertGesture("hello","hi");

        // then - it will be acceted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(currentRequestUrl);

    }

    @Test
    public void replace_gesture(){

        // when - we update the first time
        ResponseEntity<String> response = gesturesController.upsertGesture("hello","hi");

        // then - gesture accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(currentRequestUrl);

        // when - existing value get updated 
        response = gesturesController.upsertGesture("hello", "howdy");

        // then - it get accepted and return HttpStatus OK and previous value - "hi"
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("hi");
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();

        // when - existing values get updated again
        response = gesturesController.upsertGesture("hello", "pokat");

        // then - it will be accepted with status - OK , HttpHeaders.LOCATION is null , body is howdy
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("howdy");
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();
    }


}
