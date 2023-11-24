package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO.ActiveCitationDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveCitationTransformer extends RelatedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(ActiveCitationDTO activeCitationDTO) {

    CitationTypeDTO citationTypeDTO = activeCitationDTO.getCitationType();
    CitationType citationType = null;
    if (citationTypeDTO != null && citationTypeDTO.getId() != null) {
      citationType =
          CitationType.builder()
              .uuid(citationTypeDTO.getId())
              .documentType(citationTypeDTO.getDocumentationUnitDocumentCategory().getLabel())
              .citationDocumentType(citationTypeDTO.getCitationDocumentCategory().getLabel())
              .jurisShortcut(citationTypeDTO.getAbbreviation())
              .label(citationTypeDTO.getLabel())
              .build();
    }

    return ActiveCitation.builder()
        .uuid(activeCitationDTO.getId())
        .documentNumber(activeCitationDTO.getDocumentNumber())
        .referencedDocumentationUnitId(
            activeCitationDTO.getReferencedDocumentationUnit() == null
                ? null
                : activeCitationDTO.getReferencedDocumentationUnit().getId())
        .court(getCourtFromDTO(activeCitationDTO.getCourt()))
        .fileNumber(getFileNumber(activeCitationDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(activeCitationDTO.getDocumentType()))
        .decisionDate(activeCitationDTO.getDate())
        .citationType(citationType)
        .build();
  }

  public static ActiveCitationDTO transformToDTO(ActiveCitation activeCitation) {
    if (activeCitation.hasNoValues()) {
      return null;
    }

    ActiveCitationDTOBuilder<?, ?> activeCitationDTOBuilder =
        ActiveCitationDTO.builder()
            .id(activeCitation.getUuid())
            .court(getCourtFromDomain(activeCitation.getCourt()))
            .date(activeCitation.getDecisionDate())
            .documentNumber(activeCitation.getDocumentNumber())
            .referencedDocumentationUnit(
                activeCitation.getReferencedDocumentationUnitId() == null
                    ? null
                    : DocumentationUnitDTO.builder()
                        .id(activeCitation.getReferencedDocumentationUnitId())
                        .build())
            .documentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
            .fileNumber(getFileNumber(activeCitation.getFileNumber()));

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      activeCitationDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return activeCitationDTOBuilder.build();
  }
}
