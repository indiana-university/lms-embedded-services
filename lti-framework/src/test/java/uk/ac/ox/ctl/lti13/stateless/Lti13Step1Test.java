package uk.ac.ox.ctl.lti13.stateless;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ox.ctl.lti13.config.Lti13Configuration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(properties = {"use.state=true"})
@ContextConfiguration(classes = {Lti13Configuration.class})
//@ExtendWith(SpringExtension.class)
//@WebAppConfiguration
//@TestPropertySource(properties = "use.state=true")
//@SpringJUnitWebConfig(classes = {Lti13Configuration.class})
public class Lti13Step1Test {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void testSecured() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStep1Unknown() throws Exception {
        this.mockMvc.perform(post("/lti/login_initiation/unknown"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testStep1Empty() throws Exception {
        this.mockMvc.perform(post("/lti/login_initiation/test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testStep1Complete() throws Exception {
        this.mockMvc.perform(post("/lti/login_initiation/test")
                    .param("iss", "https://test.com")
                    .param("login_hint", "hint")
                    .param("target_link_uri", "https://localhost/")
                    .param("lti_storage_target", "_parent")
                )
                .andExpect(status().isOk())
                // Just check that we're putting the right content in the page.
                .andExpect(content().string(containsString("https://platform.test/auth/")));
    }


}
