package de.bund.digitalservice.ris;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@Tag("journey")
@TestPropertySource(locations = "classpath:application.properties")
class ApplicationJourneyTest {

  @Value("${application.staging.url}")
  private String stagingUrl;

  @Test
  void applicationHealthTest() {
    WebTestClient.bindToServer()
        .baseUrl(stagingUrl)
        .build()
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk();
  }
}
