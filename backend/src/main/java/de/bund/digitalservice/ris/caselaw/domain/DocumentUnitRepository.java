package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface DocumentUnitRepository {

  Mono<DocumentUnit> findByDocumentNumber(String documentNumber);

  Mono<DocumentUnit> findByUuid(UUID uuid);

  Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice);

  Mono<DocumentUnit> save(DocumentUnit documentUnit);

  Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName);

  Mono<DocumentUnit> removeFile(UUID documentUnitId);

  Mono<Void> delete(DocumentUnit documentUnit);

  <T extends LinkedDocumentationUnit> Flux<T> searchByLinkedDocumentationUnit(
      T linkedDocumentationUnit, Pageable pageable);

  Mono<Long> count();

  Mono<Long> countSearchByLinkedDocumentationUnit(LinkedDocumentationUnit linkedDocumentationUnit);

  Mono<Long> countSearchByDocumentUnitListEntry(
      DocumentationOffice documentationOffice, DocumentUnitListEntry searchInput);

  Flux<DocumentUnitListEntry> searchByDocumentUnitListEntry(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentUnitListEntry searchInput);

  <T extends LinkedDocumentationUnit>
      Flux<T> findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
          UUID parentDocumentUnitUuid, DocumentationUnitLinkType type);

  Mono<DocumentationUnitLink> linkDocumentUnits(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid, DocumentationUnitLinkType type);

  Mono<Void> unlinkDocumentUnit(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid, DocumentationUnitLinkType type);

  Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid);

  Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid);
}
