package info.ejava.examples.app.gesture.client;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.gesture.client.config.ClientNTestConfiguration;
import lombok.extern.slf4j.Slf4j;

/*
 * This class is an example of a unit integration test that uses full http
 * communications and a convenient client command wrapper using restTmplate
 * 
 */

@SpringBootTest(classes={ClientNTestConfiguration.class},
                        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@ActiveProfiles("test")
@Tag("springboot")
@DisplayName("Gesture resttemplate Integration unit test")
@Slf4j
public class GesturesRestTemplateClientNTest {

    @Autowired
    private GestureApi gesturesClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp(){
        log.info("clearing all gesture");
        gesturesClient.deleteAllGesture();
    }

    @Test
    public void add_new_gesture(){
        // when - adding new gesture
        String gestureType = "hello";
        ResponseEntity<String> response = gesturesClient.upsertGesture(gestureType, "hi");

        // then - it  will be accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();
        //System.out.println(response.getHeaders().getFirst(HttpHeaders.LOCATION));
        String url = "http://localhost:"+port+"/"+GestureApi.GESTURE_PATH
                        .replace("{gestureType}", gestureType);
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(url);
    }

    @Test
    public void replace_gesture(){
        // when - we insert first time
        String gestureType = "hello";
        ResponseEntity<?> result = gesturesClient.upsertGesture("hello", "hi");

        // then - gesture  accepted and nothing returned
        then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(result.getBody()).isNull();
        String url = "http://localhost:"+port+"/"+GestureApi.GESTURE_PATH
                        .replace("{gestureType}", gestureType);
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(url);

        // when - an existing value gets updated 
        result = gesturesClient.upsertGesture(gestureType, "howdy");

        // then - it gets accepted and initial / previous gesture will be returned
        then(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(result.getBody()).isEqualTo("hi");
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();

    }

    @Test
    public void get_unknown_gesture_type() throws Exception {
        // when - requesting an unknown gesture
        RestClientResponseException ex = assertThrows(RestClientResponseException.class,
            ()-> gesturesClient.getGesture("unknown", null));

        // then- not found will be returned
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(ex.getResponseBodyAsString()).contains("unknown");
    }

    @Test
    public void get_gesture_without_target() throws Exception {

        // given - we have a known gesture present
        ResponseEntity<?> result = gesturesClient.upsertGesture("hello", "howdy");
        then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(result.getBody()).isNull();

        // when - requesting a known gesture

        result = gesturesClient.getGesture("hello", null);

        // then - gesture will be returned without target

        then(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(result.getBody()).isEqualTo("howdy");
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();


    }

    @Test
    public void get_gesture_with_target() throws Exception {
        // given -we have a known gesture present
        ResponseEntity<String> response = gesturesClient.upsertGesture("hello", "howdy");
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //when - requesting a known gesture
        response = gesturesClient.getGesture("hello", "jim");

        //then - gesture will be returned with target added
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("howdy, jim");
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();

    }

    @Test
    public void delete_unknown_gesture() throws Exception {

        // given 
        // when -- deleting unknown  gesture
        ResponseEntity<?> result = gesturesClient.deleteGesture("unknown");

        // then - we will receive sucess with no content
        then(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(result.getBody()).isNull();
        then(result.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();
    }

    @Test
    public void delete_known_gesture() throws Exception {
        // given - we have known gesture present
        gesturesClient.upsertGesture("hello", "howdy");

        // when - deleting known gesture
        ResponseEntity<?> response = gesturesClient.deleteGesture("hello");

        // then - will receive success with no content
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // and then the gesture type will be unknown
        // rest Template throws exception for non-200 response
        RestClientResponseException ex = assertThrows(RestClientResponseException.class, 
                  ()-> gesturesClient.getGesture("hello", null));
        
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void delete_all_gesture() throws Exception {
        // given
        List<String> gestureTypes = Arrays.asList("hello", "jello");
        ResponseEntity<?> result;
        for(String gestureType : gestureTypes){
            result = gesturesClient.upsertGesture(gestureType, "Pokat");
            then(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        // when - all gestures are deleted 
        result = gesturesClient.deleteAllGesture();

        // then - sucess with no content

        then(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // and then no gestures left

        RestClientResponseException ex;
        for(String gestureType : gestureTypes){
              //restTemplate throws exception for non-200 responses
            ex = catchThrowableOfType(()-> gesturesClient.getGesture(gestureType,null),RestClientResponseException.class);
            then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

}
