package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchEntryTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProcedureService {
  private final DatabaseProcedureRepository repository;
  private final DatabaseProcedureLinkRepository linkRepository;
  private final DatabaseDocumentationUnitSearchRepository documentUnitRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  public ProcedureService(
      DatabaseProcedureRepository repository,
      DatabaseProcedureLinkRepository linkRepository,
      DatabaseDocumentationUnitSearchRepository documentUnitRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository) {
    this.repository = repository;
    this.linkRepository = linkRepository;
    this.documentUnitRepository = documentUnitRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  public Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable) {
    return repository
        .findByLabelContainingAndDocumentationOffice(
            query, documentationOfficeRepository.findByLabel(documentationOffice.label()), pageable)
        .map(
            dto ->
                Procedure.builder()
                    .label(dto.getLabel())
                    .documentUnitCount(
                        linkRepository.countLatestProcedureLinksByProcedure(dto.getId()))
                    .created_at(dto.getCreatedAt())
                    .build());
  }

  public List<DocumentationUnitSearchEntry> getDocumentUnits(
      String procedureLabel, DocumentationOffice documentationOffice) {
    return linkRepository
        .findLatestProcedureLinksByProcedure(
            repository
                .findByLabelAndDocumentationOfficeOrderByCreatedAtDesc(
                    procedureLabel,
                    documentationOfficeRepository.findByLabel(documentationOffice.label()))
                .getId())
        .stream()
        .map(ProcedureLinkDTO::getDocumentationUnitId)
        .map(documentUnitRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(DocumentationUnitSearchEntryTransformer::transferDTO)
        .toList();
  }
}
