package info.ejava.examples.app.gesture;

import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.app.api.GestureApi;
import info.ejava.examples.app.common.web.ServerConfig;
import info.ejava.examples.app.gesture.client.ClientTestBaseConfiguration;
import lombok.extern.slf4j.Slf4j;

/*
 * This class is an example of unit integration test of a controller class.
 * There is a spring context that creates components under test 
 * that we communicate with directly through a MockMvc
 */

//@SpringBootTest(classes={ClientTestBaseConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
@Tag("springboot")
@DisplayName("Gestures MockMvc Integration Test ")
@Slf4j
@AutoConfigureMockMvc
public class GesturesMockMvcNTest {

    //private static final String[] GESTURES_PATH="api/gestures".split("/");
    //private static final String[] GESTURE_PATH= "api/gestures/{gestureType}".split("/");

    private ServerConfig serverConfig;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception{
        serverConfig = new ServerConfig().build();
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl())
                                       .path(GestureApi.GESTURES_PATH)     
                                       .build().toUri();
        
        
        mockMvc.perform(delete(url)).andExpect(status().is2xxSuccessful());

        
    }

    @Test
    public void add_new_gesture() throws Exception     {

        // given 
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl())
                                        .path(GestureApi.GESTURE_PATH).build("hello");
                    
        // when - adding a new gesture
        ResultActions response = mockMvc.perform(post(url)
                                        .accept(MediaType.TEXT_PLAIN)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .content("hi"))
                                        .andDo(print());
        
        // then - it will be accepted and nothing returned

        response.andExpect(status().isCreated());
        response.andExpect(content().string(containsString("")));
        response.andExpect(header().string(HttpHeaders.LOCATION,url.toString()));
        
    }
    
    @Test
    public void replace_gesture() throws Exception {

        // given 
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl()) 
                                        .path(GestureApi.GESTURE_PATH).build("hello");

        // when - we update first time
        ResultActions response = mockMvc.perform(post(url)
                                            .accept(MediaType.TEXT_PLAIN)
                                            .contentType(MediaType.TEXT_PLAIN)
                                            .content("hi"));
        
        // then - gesture accepted, nothing returned
        response.andExpect(status().isCreated());
        response.andExpect(content().string(containsString("")));

        // when - an existing value gets updated
        response = mockMvc.perform(post(url)
                            .accept(MediaType.TEXT_PLAIN)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("howdy")) 
                            .andDo(print());
        
        // then - gesture accepted , previous value "hi" returned, LOCATION header does not exist
        response.andExpect(status().isOk());
        response.andExpect(content().string(containsString("hi")));
        response.andExpect(header().doesNotExist(HttpHeaders.LOCATION));

    }

    @Test
    public void get_unknown_gesture_type() throws Exception {

        // given - unknown gesture
        URI url = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl())
                                        .path(GestureApi.GESTURE_PATH).build("unknown");

        // when - requesting an unknown gesture
        ResultActions response = mockMvc.perform(get(url)
                                            .accept(MediaType.TEXT_PLAIN)) ;
                                
        // then - not found will be returned
        response.andExpect(status().isNotFound());
        response.andExpect(content().string(containsString("unknown")));
        response.andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }



}
