package info.ejava.examples.app.gesture.client;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.gesture.client.config.ClientNTestConfiguration;

/**
 * This class is an example of an integration test that uses full HTTP
 * communications and a convenient client command wrapper using RestClient.
 */
@SpringBootTest(classes = {ClientNTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("springboot")
@DisplayName("Gestures RestClient Integration Unit Test")
@Slf4j
public class GesturesRestClientNTest {
    @Autowired
    @Qualifier("restclient")
    private GestureApi gesturesClient;

    @BeforeEach
    public void setUp() {
        log.info("clearing all gestures");
        gesturesClient.deleteAllGesture();
    }

    @Test
    public void add_new_gesture() {
        //when - adding a new gesture
        ResponseEntity<?> response = gesturesClient.upsertGesture("hello", "hi");

        //then - it will be accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();
    }

    @Test
    public void replace_gesture() {
        //when - we update the first time
        ResponseEntity<?> response = gesturesClient.upsertGesture("hello","hi");

        //then -- gesture accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();

        //when - an existing value gets updated
        response = gesturesClient.upsertGesture("hello","howdy");

        //then - it gets accepted and initial gesture back in response
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("hi");
    }

    @Test
    public void get_unknown_gesture_type() {
        //when - requesting an unknown gesture
        HttpClientErrorException.NotFound ex = catchThrowableOfType(
                () -> gesturesClient.getGesture("unknown", null),
                HttpClientErrorException.NotFound.class);

        //then - not found will be returned
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(ex.getResponseBodyAsString()).contains("unknown");
    }

    @Test
    public void get_gesture_without_target() {
        //given - we have a known gesture present
        ResponseEntity<String> response = gesturesClient.upsertGesture("hello","howdy");
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //when - requesting a known gesture
        response = gesturesClient.getGesture("hello", null);

        //then - gesture will be returned without target
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("howdy");
    }

    @Test
    public void get_gesture_with_target() {
        //given - we have a known gesture present
        ResponseEntity<String> response = gesturesClient.upsertGesture("hello", "howdy");
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //when - requesting a known gesture
        response = gesturesClient.getGesture("hello", "jim");

        //then - gesture will be returned with target added
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("howdy, jim");
    }

    @Test
    public void delete_unknown_gesture() {
        //given
        //when - deleting unknown gesture
        ResponseEntity<?> response = gesturesClient.deleteGesture("unknown");

        //then - will receive success with no content
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(response.getBody()).isNull();
    }

    @Test
    public void delete_known_gesture() {
        //given - we have a known gesture present
        gesturesClient.upsertGesture("hello", "howdy");

        //when - deleting known gesture
        ResponseEntity<?> response = gesturesClient.deleteGesture("hello");

        //then - will receive success with no content
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        //and then the gestureType will be unknown
        HttpClientErrorException.NotFound ex = catchThrowableOfType(
                () -> gesturesClient.getGesture("hello", null),
                HttpClientErrorException.NotFound.class);
        then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void delete_all_gestures() {
        //given
        List<String> gestureTypes = Arrays.asList("hello", "goodbye");
        for (String gestureType : gestureTypes) {
            ResponseEntity<String> response = gesturesClient.upsertGesture(gestureType, "aloha");
            then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        //when deleting all gestures
        ResponseEntity<Void> response = gesturesClient.deleteAllGesture();

        //then - collection was cleared
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        //and then no gestures left
        for (String gestureType : gestureTypes) {
            HttpClientErrorException ex = catchThrowableOfType(
                    () -> gesturesClient.getGesture(gestureType, null),
                    HttpClientErrorException.NotFound.class);
            then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
