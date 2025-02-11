package info.ejava.examples.app.common.web.client;

/*
 * This class demonstrate a synchronous / blocking client using RestTemplate
 * -- which  is the only mode RestTemplate can operate in.Both the client and 
 * server side are implemented synchronously -- so we are able to naively leverage 
 * a common interface
 * 
 */

import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.common.web.ServerConfig;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Qualifier("template")
public class GestureApiTemplateClient implements GestureApi {

    private final URI baseUrl;
    private final RestTemplate restTemplate;

    public GestureApiTemplateClient(RestTemplate restTemplate, ServerConfig serverConfig){
        this.baseUrl = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).build().toUri();
        this.restTemplate = restTemplate; 
    }

    @Override
    public ResponseEntity<String> upsertGesture(String gestureType, String gesture) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH).build(gestureType);
        RequestEntity<String> request = RequestEntity.post(url)
                                                        .accept(MediaType.TEXT_PLAIN)
                                                        .contentType(MediaType.TEXT_PLAIN)
                                                        .body(gesture);
        
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        return response;                                                
    }

    @Override
    public ResponseEntity<String> getGesture(String gestureType, String target) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH);

        if(target != null){
            builder = builder.queryParam("target", target);
        }
        URI url = builder.build(gestureType);

        RequestEntity<Void> request = RequestEntity.get(url)
                                                        .accept(MediaType.TEXT_PLAIN)
                                                         .build();
        
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteGesture(String gestureType) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH).build(gestureType);
        RequestEntity<Void> request = RequestEntity.delete(url).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteAllGesture() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURES_PATH).build().toUri();

        RequestEntity<Void> request = RequestEntity.delete(url).build();

        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);

        return response;
    }


}
