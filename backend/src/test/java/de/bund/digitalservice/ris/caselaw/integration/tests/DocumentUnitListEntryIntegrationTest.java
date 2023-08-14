package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      KeycloakUserService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitListEntryIntegrationTest {
  @Container
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitStatusRepository statusRepository;
  @Autowired private DatabaseDocumentUnitMetadataRepository listEntryRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private JPADocumentationOfficeRepository documentationOfficeRepository;

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private JPADocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
    statusRepository.deleteAll().block();

    docOfficeDTO = documentationOfficeRepository.findByLabel("DigitalService");

    doReturn(Mono.just(DocumentationOfficeTransformer.transformDTO(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    DocumentUnitDTO migrationDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("MIGR202200012")
                    .dataSource(DataSource.MIGRATION)
                    .build())
            .block();
    DocumentUnitDTO neurisDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("NEUR202300007")
                    .dataSource(DataSource.NEURIS)
                    .documentationOfficeId(docOfficeDTO.getId())
                    .build())
            .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(neurisDto.getId())
                .fileNumber("AkteX")
                .isDeviating(false)
                .build())
        .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(migrationDto.getId())
                .fileNumber("AkteM")
                .isDeviating(false)
                .build())
        .block();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/search-by-document-unit-list-entry?pg=0&sz=3")
        .bodyValue(DocumentUnitListEntry.builder().build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content[0].documentNumber")
        .isEqualTo("NEUR202300007")
        .jsonPath("$.content[0].uuid")
        .isEqualTo(neurisDto.getUuid().toString())
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteX")
        .jsonPath("$.content[0].documentationOffice.label")
        .isEqualTo("DigitalService")
        .jsonPath("$.content[0].status.publicationStatus")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.totalElements")
        .isEqualTo(2);
  }

  @Test
  void testForCorrectResponseWhenRequesting_onlyMyDocOffice() {
    DocumentUnitDTO Dto1 =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("NEUR202300007")
                    .dataSource(DataSource.NEURIS)
                    .documentationOfficeId(docOfficeDTO.getId())
                    .build())
            .block();

    DocumentUnitDTO Dto2 =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("BGH202300008")
                    .dataSource(DataSource.NEURIS)
                    .documentationOfficeId(
                        documentationOfficeRepository.findByLabel("BGH").block().getId())
                    .build())
            .block();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/search-by-document-unit-list-entry?pg=0&sz=3")
        .bodyValue(DocumentUnitListEntry.builder().myDocOfficeOnly(true).build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content[0].documentationOffice.label")
        .isEqualTo("DigitalService")
        .jsonPath("$.totalElements")
        .isEqualTo(1);
  }

  @Test
  void testForCorrectOrdering() {
    List<String> documentNumbers = Arrays.asList("ABCD202300007", "EFGH202200123", "IJKL202300099");

    for (String documentNumber : documentNumbers) {
      repository
          .save(
              DocumentUnitDTO.builder()
                  .uuid(UUID.randomUUID())
                  .creationtimestamp(Instant.now())
                  .documentnumber(documentNumber)
                  .dataSource(DataSource.NEURIS)
                  .documentationOfficeId(docOfficeDTO.getId())
                  .build())
          .block();
    }

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/search-by-document-unit-list-entry?pg=0&sz=10")
            .bodyValue(DocumentUnitListEntry.builder().build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> documentNumbersActual =
        JsonPath.read(result.getResponseBody(), "$.content[*].documentNumber");
    assertThat(documentNumbersActual)
        .hasSize(3)
        .containsExactly("IJKL202300099", "EFGH202200123", "ABCD202300007");
  }

  @Test
  void testForCorrectPagination() {
    List<DocumentUnitDTO> documents =
        IntStream.range(0, 99)
            .mapToObj(
                i ->
                    DocumentUnitDTO.builder()
                        .uuid(UUID.randomUUID())
                        .creationtimestamp(Instant.now())
                        .documentnumber("123456780" + i)
                        .dataSource(DataSource.NEURIS)
                        .documentationOfficeId(docOfficeDTO.getId())
                        .build())
            .collect(Collectors.toList());

    repository.saveAll(documents).blockLast();

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/search-by-document-unit-list-entry?pg=0&sz=1")
            .bodyValue(DocumentUnitListEntry.builder().build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    Integer totalElements = JsonPath.read(result.getResponseBody(), "$.totalElements");
    assertThat(totalElements).isEqualTo(99);
  }
}
