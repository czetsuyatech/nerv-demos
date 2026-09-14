package com.czetsuyatech.nerv.example.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false", "debug=false"})
@AutoConfigureMockMvc
class ExceptionDemoContextTest {
  @Autowired MockMvc mvc;
  @Test
  void starterMapsDemoFailuresToTheirHttpStatuses() throws Exception {
    mvc.perform(get("/payments/123")).andExpect(status().isOk());
    mvc.perform(get("/payments/404")).andExpect(status().isNotFound());
    mvc.perform(get("/payments/timeout")).andExpect(status().isGatewayTimeout());
    mvc.perform(get("/payments/error")).andExpect(status().isInternalServerError());
  }
}
