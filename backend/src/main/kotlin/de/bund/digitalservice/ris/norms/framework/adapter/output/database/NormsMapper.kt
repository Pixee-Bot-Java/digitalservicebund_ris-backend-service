package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.domain.entity.*
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

interface NormsMapper {
  fun normToEntity(
      normDto: NormDto,
      fileReferences: List<FileReference>,
      dtoMetadataSections: List<MetadataSectionDto>,
      dtoMetadata: Collection<MetadatumDto>,
      recitals: RecitalsDto?,
      formula: FormulaDto?,
      documentation: Collection<Documentation>,
      conclusion: ConclusionDto?,
  ): Norm {
    val listDomainSections = mutableListOf<MetadataSection>()

    // 1. Objective: move children from parent level to their respective parents because otherwise
    // we can't instantite the parent MetadataSection
    dtoMetadataSections
        .filter { dtoSectionToFilter -> dtoSectionToFilter.sectionGuid == null }
        .map { dtoCurrentParentSection ->
          val dtoChildrenOfCurrentParentSection =
              dtoMetadataSections.filter { it2 -> it2.sectionGuid == dtoCurrentParentSection.guid }
          if (dtoChildrenOfCurrentParentSection.isEmpty()) {
            // Parent section without children, meaning with metadata
            val dtoMetadatumOfCurrentParentSection =
                dtoMetadata.filter { dtoMetadatum ->
                  dtoCurrentParentSection.guid == dtoMetadatum.sectionGuid
                }
            val convertedSection =
                metadataSectionToEntity(
                    dtoCurrentParentSection,
                    dtoMetadatumOfCurrentParentSection.map { metadatumToEntity(it) })
            listDomainSections.add(convertedSection)
          } else {
            // Parent section with children (assumming without metadata)
            val listChildrenDomain = mutableListOf<MetadataSection>()
            dtoChildrenOfCurrentParentSection.map { dtoChildOfCurrentParentSection ->
              val dtoMetadatumOfChild =
                  dtoMetadata.filter { dtoMetadatum ->
                    dtoChildOfCurrentParentSection.guid == dtoMetadatum.sectionGuid
                  }
              val convertedChildSection =
                  metadataSectionToEntity(
                      dtoChildOfCurrentParentSection,
                      dtoMetadatumOfChild.map { metadatumToEntity(it) })
              listChildrenDomain.add(convertedChildSection)
            }
            val domainParent =
                MetadataSection(
                    name = dtoCurrentParentSection.name,
                    order = dtoCurrentParentSection.order,
                    guid = dtoCurrentParentSection.guid,
                    metadata = emptyList(),
                    sections = listChildrenDomain)
            listDomainSections.add(domainParent)
          }
        }

    return Norm(
        normDto.guid,
        listDomainSections,
        fileReferences,
        recitals?.let { recitalsToEntity(it) },
        formula?.let { formulaToEntity(it) },
        documentation,
        conclusion?.let { conclusionToEntity(it) },
    )
  }

  fun metadataSectionToEntity(
      metadataSectionDto: MetadataSectionDto,
      metadata: List<Metadatum<*>>
  ): MetadataSection {
    return MetadataSection(
        name = metadataSectionDto.name,
        order = metadataSectionDto.order,
        metadata = metadata,
        guid = metadataSectionDto.guid)
  }

  fun fileReferenceToEntity(fileReferenceDto: FileReferenceDto): FileReference {
    return FileReference(
        fileReferenceDto.name,
        fileReferenceDto.hash,
        fileReferenceDto.createdAt,
        fileReferenceDto.guid)
  }

  fun articleToEntity(data: ArticleDto, paragraphs: Collection<Paragraph>) =
      Article(
          guid = data.guid,
          order = data.order,
          marker = data.marker,
          heading = data.heading,
          paragraphs = paragraphs,
      )

  fun documentSectionToEntity(data: DocumentSectionDto, documentation: Collection<Documentation>) =
      DocumentSection(
          guid = data.guid,
          order = data.order,
          marker = data.marker,
          heading = data.heading,
          type = data.type,
          documentation = documentation,
      )

  fun paragraphToEntity(data: ParagraphDto) =
      Paragraph(
          guid = data.guid,
          marker = data.marker,
          text = data.text,
      )

  fun recitalsToEntity(data: RecitalsDto) =
      Recitals(
          guid = data.guid,
          marker = data.marker,
          heading = data.heading,
          text = data.text,
      )

  fun formulaToEntity(data: FormulaDto) =
      Formula(
          guid = data.guid,
          text = data.text,
      )

  fun conclusionToEntity(data: ConclusionDto) =
      Conclusion(
          guid = data.guid,
          text = data.text,
      )

  fun metadatumToEntity(metadatumDto: MetadatumDto): Metadatum<*> {
    val value =
        when (metadatumDto.type) {
          MetadatumType.DATE -> LocalDate.parse(metadatumDto.value)
          MetadatumType.TIME -> LocalTime.parse(metadatumDto.value)
          MetadatumType.RESOLUTION_MAJORITY -> metadatumDto.value.toBoolean()
          MetadatumType.NORM_CATEGORY -> NormCategory.valueOf(metadatumDto.value)
          MetadatumType.UNDEFINED_DATE -> UndefinedDate.valueOf(metadatumDto.value)
          else -> metadatumDto.value
        }

    return Metadatum(value, metadatumDto.type, metadatumDto.order, metadatumDto.guid)
  }

  fun normToDto(norm: Norm): NormDto {
    return NormDto(
        norm.guid,
        norm.recitals?.guid,
        norm.formula?.guid,
        norm.conclusion?.guid,
        norm.eGesetzgebung,
    )
  }

  fun recitalsToDto(data: Recitals) = RecitalsDto(data.guid, data.marker, data.heading, data.text)

  fun formulaToDto(data: Formula) = FormulaDto(data.guid, data.text)

  fun conclusionToDto(data: Conclusion) = ConclusionDto(data.guid, data.text)

  fun documentSectionToDto(data: DocumentSection, normGuid: UUID, parentSectionGuid: UUID?) =
      DocumentSectionDto(
          guid = data.guid,
          order = data.order,
          type = data.type,
          marker = data.marker,
          heading = data.heading,
          parentSectionGuid = parentSectionGuid,
          normGuid = normGuid,
      )

  fun articleToDto(data: Article, normGuid: UUID, documentSectionGuid: UUID?) =
      ArticleDto(
          guid = data.guid,
          order = data.order,
          marker = data.marker,
          heading = data.heading,
          normGuid = normGuid,
          documentSectionGuid = documentSectionGuid,
      )

  fun paragraphToDto(data: Paragraph, articleGuid: UUID) =
      ParagraphDto(
          guid = data.guid,
          marker = data.marker,
          text = data.text,
          articleGuid = articleGuid,
      )

  fun fileReferencesToDto(
      fileReferences: Collection<FileReference>,
      normGuid: UUID
  ): List<FileReferenceDto> {
    return fileReferences.map { fileReferenceToDto(fileReference = it, normGuid = normGuid) }
  }

  fun fileReferenceToDto(fileReference: FileReference, normGuid: UUID): FileReferenceDto {
    return FileReferenceDto(
        fileReference.guid,
        fileReference.name,
        fileReference.hash,
        normGuid,
        fileReference.createdAt)
  }

  fun metadataListToDto(metadata: List<Metadatum<*>>, sectionGuid: UUID): List<MetadatumDto> {
    return metadata.map {
      MetadatumDto(
          value = it.value.toString(),
          type = it.type,
          order = it.order,
          guid = it.guid,
          sectionGuid = sectionGuid)
    }
  }

  fun metadataSectionToDto(
      metadataSection: MetadataSection,
      normGuid: UUID,
      sectionGuid: UUID? = null
  ): MetadataSectionDto {
    return MetadataSectionDto(
        guid = metadataSection.guid,
        name = metadataSection.name,
        order = metadataSection.order,
        sectionGuid = sectionGuid,
        normGuid = normGuid)
  }

  fun metadataSectionsToDto(
      sections: List<MetadataSection>,
      sectionGuid: UUID? = null,
      normGuid: UUID
  ): List<MetadataSectionDto> {
    return sections.map {
      metadataSectionToDto(
          it,
          normGuid,
          sectionGuid,
      )
    }
  }
}
