package info.ejava.examples.app.gesture;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest(classes={ClientTestBaseConfiguration.class})
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
        
    }
    



}
