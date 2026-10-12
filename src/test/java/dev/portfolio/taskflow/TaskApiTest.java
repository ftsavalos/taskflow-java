package dev.portfolio.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createsReadsAndAdvancesTask() throws Exception {
        var result = mvc.perform(post("/tasks").contentType("application/json")
                        .content("{\"title\":\"  Learn Spring Boot  \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO")).andReturn();
        long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get(result.getResponse().getHeader("Location")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/tasks")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        mvc.perform(post("/tasks/{id}/advance", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(post("/tasks/{id}/advance", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
        mvc.perform(post("/tasks/{id}/advance", id)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsInvalidBodiesAndMissingTasks() throws Exception {
        for (String body : new String[]{"{}", "{\"title\":null}", "{\"title\":\"  \"}", "{", 
                mapper.writeValueAsString(new CreateTaskRequest("x".repeat(121)))}) {
            mvc.perform(post("/tasks").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
        mvc.perform(get("/tasks/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/tasks/999999/advance")).andExpect(status().isNotFound());
        mvc.perform(get("/tasks/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(post("/tasks").contentType("text/plain").content("hello"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(delete("/tasks")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void jacksonPreservesQuotesAndUnicode() throws Exception {
        String title = "Greek Ελληνικά \"quoted\"";
        mvc.perform(post("/tasks").contentType("application/json")
                        .content(mapper.writeValueAsString(new CreateTaskRequest(title))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value(title));
    }
}
