package info.ejava.examples.app.gesture;

import org.assertj.core.api.Assertions;
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
import info.ejava.examples.app.svc.ClientErrorException;
import info.ejava.examples.app.svc.GestureService;
import io.netty.handler.codec.Headers;
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

    @Test
    public void get_unknown_gesture_type(){
        // when - requesting an unknown gesture
        // NOTE: advice is not being applied to injected controller in this type of test

        ClientErrorException.NotFoundException ex = Assertions.catchThrowableOfType(
            () -> gesturesController.getGesture("unknown", null),
            ClientErrorException.NotFoundException.class
        );
        // then - not found will be returned
        ResponseEntity response = exceptionAdvice.handle(ex);
        //System.out.println(response);
        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(response.getBody().toString()).contains("unknown");
        then(response.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION)).isNull();
    }

    @Test
    public void get_gesture_without_target(){
        // given - we have a known gesture present
        ResponseEntity<String> response = gesturesController.upsertGesture("hello","howdy");
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // then - get gesture without target
        response = gesturesController.getGesture("hello", null);
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("howdy");
        then(response.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION))
             .isEqualTo(currentRequestUrl);

    }

    @Test
    public void get_gesture_with_target(){
        // given - we have known gesture
        ResponseEntity<String> result = gesturesController.upsertGesture("hello", "howdy");
        then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when - requesting a known gesture
        result = gesturesController.getGesture("hello", "jim");

        // then - gesture will be returned with target added

        then(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(result.getBody()).isEqualTo("howdy, jim");
        then(result.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION))
                                .isEqualTo(currentRequestUrl);
    }

    @Test
    public void delete_unknown_gesture(){
        // when - deleting unknown gesture
        ResponseEntity<Void> result = gesturesController.deleteGesture("unknown");

        // then we will receive success with no content
        then(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void delete_known_gesture(){

        // when - we have a known gesture present
        ResponseEntity<String> response = gesturesController.upsertGesture("hello","hi");
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when -  deleting known gesture
        ResponseEntity<Void> result = gesturesController.deleteGesture("hello");

        // then - will receive success with no content
        then(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void delete_all_gesture(){
        // given - upsert some gestures
        ResponseEntity<String> result = gesturesController.upsertGesture("hello","hi");
        then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(result.getBody()).isEqualTo(null);
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION))
                                .isEqualTo(currentRequestUrl);
        result = gesturesController.upsertGesture("smile", "face");
        then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(result.getBody()).isEqualTo(null);
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION))
                                .isEqualTo(currentRequestUrl);

        // when - call the delete from controller

        ResponseEntity<Void> deleteResult = gesturesController.deleteAllGesture();

        // then - the request is accepted and returned no content

        then(deleteResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        
        // then - check result returned by exception Advice
        ClientErrorException.NotFoundException ex = Assertions.catchThrowableOfType(
            () -> gesturesController.getGesture("smile", null),
                  ClientErrorException.NotFoundException.class
        );

        result = exceptionAdvice.handle(ex);
        then(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(result.getBody()).contains("smile");
        

    }


}
