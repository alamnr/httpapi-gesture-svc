package info.ejava.examples.app.gesture;

import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.BDDMockito;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.examples.app.controllers.ExceptionAdvice;
import info.ejava.examples.app.controllers.GesturesController;
import info.ejava.examples.app.svc.ClientErrorException.NotFoundException;
import info.ejava.examples.app.svc.GestureService;
import lombok.extern.slf4j.Slf4j;

/*
 * This class example unit test of controller class. There is no Spring context 
 * initiated and all dependencies have been mocked
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
public class GesturesMockMvcTest {

    @InjectMocks
    private GesturesController gestureController;

    @Mock
    private GestureService gestureService;

    //@Captor
    //private ArgumentCaptor<String> stringArg;

    private String currentRequestUrl;
    private ExceptionAdvice exceptionAdvice = new ExceptionAdvice();

    @BeforeEach
    public void setUp(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        currentRequestUrl = ServletUriComponentsBuilder.fromRequest(request)
                                                        .build().toUriString();
        gestureController.deleteAllGesture();
        reset(gestureService);  // don't bleed into test methods
    }

    @Test
    public void add_new_gesture(){
        // given - a service with no current gesture
        given(gestureService.upsertGesture("hello", "hi"))
                .willReturn(new GestureService.UpsertResult(true,null));

        // when - adding a new gesture
        ResponseEntity<String> response = gestureController.upsertGesture("hello","hi");

        // then - it will be accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(currentRequestUrl);
    }

    @Test
    public void replace_gesture(){
        // given - that our mock business service will return "hi" as previous gesture
        given(gestureService.upsertGesture(anyString(), anyString()))
                            .willReturn(new GestureService.UpsertResult(true,null),
                            new GestureService.UpsertResult(false,"hi"));
        // when - we update first time
        ResponseEntity<String> response = gestureController.upsertGesture("hello", "hi");

        // then - gesture accepted and nothing returned
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(response.getBody()).isNull();

        // when - an existing value gets updated
        response = gestureController.upsertGesture("hello", "howdy");

        // then - it gets accepted and initial gesture back in response
        BDDMockito.then(gestureService).should(times(2)).upsertGesture(anyString(), anyString());
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo("hi");
        then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isNull();
    
    }

    @Test
    public void get_unknown_gesture_type(){
        // given - a gesture does not exist
        given(gestureController.getGesture("unknown", null))
                    .willThrow(new NotFoundException("unknown gestureType[unknown]"));
        
        // when - requesting an unknown gesture
        NotFoundException ex = catchThrowableOfType(()->
                                gestureController.getGesture("unknown", null),
                                NotFoundException.class);

        // then - not found will be returned  
        ResponseEntity<String> response = exceptionAdvice.handle(ex);
        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(response.getBody().toString()).contains("unknown");
        then(response.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION)).isNull();
    }

    @Test
    public void get_gesture_without_target(){
        // given - when we have a known gesture present
        given(gestureService.getGesture("hello", null)).willReturn("howdy");

        // when - requesting a known gesture
        ResponseEntity<String> response = gestureController.getGesture("hello", null);

        // then - gesture will be returned without target
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody().toString()).contains("howdy");
        then(response.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION))
                                  .isEqualTo(currentRequestUrl);

    }
    
    @Test
    public void get_gesture_with_target(){
        //  when - we have a known gesture present
        given(gestureService.getGesture("hello", "jim")).willReturn("hello, jim");

        // when - requesting a known gesture
        ResponseEntity<String> response = gestureController.getGesture("hello", "jim");

        // then - gesture will be returned with target added
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody().toString()).contains("hello, jim");
        then(response.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION))
                                  .isEqualTo(currentRequestUrl);

    }

    @Test
    public void delete_unknown_gesture(){
        // when - deleting unknown gesture
        ResponseEntity<Void> response = gestureController.deleteGesture("unknown");

        // then - will receive success with no content
        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    public void delete_known_gesture(){
        // when - deleting known gesture
        ResponseEntity<Void> response = gestureController.deleteGesture("hello");

        // then - gestureType will have been remove from map
        BDDMockito.then(gestureService).should(times(1)).deleteGesture("hello");

        // and then will receive success with no-content
        and.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void delete_all_gesture(){
        // when 
        ResponseEntity<Void> response = gestureController.deleteAllGesture();

        // then - collection was cleared
        BDDMockito.then(gestureService).should(times(1)).deleteAllGesture();
        and.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


}
