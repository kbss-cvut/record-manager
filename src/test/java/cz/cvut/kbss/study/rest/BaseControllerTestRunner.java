package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.config.WebAppConfig;
import cz.cvut.kbss.study.environment.util.Environment;
import static cz.cvut.kbss.study.environment.util.Environment.createDefaultMessageConverter;
import static cz.cvut.kbss.study.environment.util.Environment.createResourceMessageConverter;
import static cz.cvut.kbss.study.environment.util.Environment.createStringEncodingMessageConverter;
import cz.cvut.kbss.study.rest.handler.RestExceptionHandler;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;

public abstract class BaseControllerTestRunner {

    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    public void setUp(BaseController controller) {
        setupObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new RestExceptionHandler())
            .setMessageConverters(
                createDefaultMessageConverter(), createStringEncodingMessageConverter(),
                createResourceMessageConverter())
            .setUseSuffixPatternMatch(false)
            .build();
    }

    void setupObjectMapper() {
        this.objectMapper = Environment.getObjectMapper();
    }


    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    <T> T readValue(MvcResult result, Class<T> targetType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), targetType);
    }

    <T> T readValue(MvcResult result, TypeReference<T> targetType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), targetType);
    }

    protected void verifyLocationEquals(String expectedPath, MvcResult result) {
        final String locationHeader = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertEquals("http://localhost" + expectedPath, locationHeader);
    }
}
