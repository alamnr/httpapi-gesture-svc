package info.ejava.examples.app.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.svc.GestureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * The following is an example of using HTTP constructs , 
 * Resources and methods consistent with the HTTP-based/REST-like
 * category of service discussed in the lectures. To keep it 
 * simple, we are using a simple Map object to act as our back-end business service and storage
 * The Web MVC mappings have been assigned to the interface
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class GesturesController implements  GestureApi {

    // Simplistic business service
    private final GestureService gestures;

    @Override
    @RequestMapping(path=GESTURE_PATH, 
                    method={RequestMethod.POST,RequestMethod.PUT},
                    consumes={MediaType.TEXT_PLAIN_VALUE},
                    produces={MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> upsertGesture(@PathVariable(name="gestureType",
                                               required=true)String gestureType,  
                                               @RequestBody String gesture) {   
        
        // business method
        GestureService.UpsertResult result = gestures.upsertGesture(gestureType, gesture);

        log.debug("set gesture({}) to {} , returning previous value {}", 
                     gestureType,gesture, result.getPreviousValue());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        
        ResponseEntity.BodyBuilder response = result.isCreated() ? 
                                                ResponseEntity.created(location):
                                                ResponseEntity.status(HttpStatus.OK);
                    
        return response.body(result.getPreviousValue());
    }

    @RequestMapping(path=GESTURE_PATH, 
                    method=RequestMethod.GET, 
                    produces={MediaType.TEXT_PLAIN_VALUE})
    @Override
    public ResponseEntity<String> getGesture(@PathVariable(name="gestureType")String gestureType,
                                             @RequestParam(name="target", required=false)
                                             String target) {
        // business method
        String result = gestures.getGesture(gestureType, target);

        String location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();

        return ResponseEntity.status(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_LOCATION, location)
                            .body(result);
    }

    @RequestMapping(path=GESTURES_PATH+"/all" , method=RequestMethod.GET)
    public ResponseEntity<List<String>> getAllGesture(){
        List<String> result = gestures.getAllGesture();
        String location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
        return ResponseEntity.status(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_LOCATION, location)
                                .body(result);
    }

    @RequestMapping(path=GESTURE_PATH,method=RequestMethod.DELETE)
    @Override
    public ResponseEntity<Void> deleteGesture(@PathVariable(name="gestureType")
                                                String gestureType) {
        
        // business method
        gestures.deleteGesture(gestureType);
        return ResponseEntity.noContent().build();
    }


    @RequestMapping(path=GESTURES_PATH, method= RequestMethod.DELETE)
    @Override
    public ResponseEntity<Void> deleteAllGesture() {
        // business method
        gestures.deleteAllGesture();
        return ResponseEntity.noContent().build();
    }

    

}
