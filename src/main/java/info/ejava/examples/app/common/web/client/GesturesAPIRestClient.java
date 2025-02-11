package info.ejava.examples.app.common.web.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.common.web.ServerConfig;


/**
 * This class demonstrates an implementation of the Gestures client using
 * RestClient. The primary difference between this and the RestTemplate
 * version is the fluent API.
 */
@Slf4j
@Qualifier("restclient")
public class GesturesAPIRestClient implements GestureApi {
    private final URI baseUrl;
    private final RestClient restClient;

    public GesturesAPIRestClient(RestClient restClient, ServerConfig serverConfig) {
        this.restClient = restClient;
        baseUrl = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()).build().toUri();
    }

    @Override
    public ResponseEntity<String> upsertGesture(String gestureType, String gesture) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH).build(gestureType);

        RestClient.RequestBodySpec request = restClient.post()
                .uri(url)
                .accept(MediaType.TEXT_PLAIN)
                .contentType(MediaType.TEXT_PLAIN)
                .body(gesture);
        ResponseEntity<String> response = request.retrieve().toEntity(String.class);
        return response;

    }

    @Override
    public ResponseEntity<String> getGesture(String gestureType, String target) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH);
        if (target!=null) {
            builder = builder.queryParam("target", target);
        }
        URI url = builder.build(gestureType);

        RestClient.RequestHeadersSpec<?> request = restClient
                .get()
                .uri(url)
                .accept(MediaType.TEXT_PLAIN);
        ResponseEntity<String> response = request.retrieve().toEntity(String.class);
        return response;

    }

    @Override
    public ResponseEntity<Void> deleteGesture(String gestureType) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURE_PATH).build(gestureType);
        return doDelete(url);
    }

    @Override
    public ResponseEntity<Void> deleteAllGesture() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(GESTURES_PATH).build().toUri();
        return doDelete(url);

    }

    public ResponseEntity<Void> doDelete(URI url) {
        RestClient.RequestHeadersSpec<?> request = restClient
                .delete()
                .uri(url);
        ResponseEntity<Void> response = request.retrieve().toEntity(Void.class);
        return response;
    }
    

}
