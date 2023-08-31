package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.exceptions.exception.NotFoundWithInstanceException
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.*
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

  @GetMapping(path = ["/{guid}"])
  @Operation(summary = "Load a single norm", description = "Retrieves a single norm given its guid")
  @ApiResponses(
      ApiResponse(responseCode = "200", description = "Norm was found"),
      ApiResponse(responseCode = "404", description = "No norm found for this query"),
  )
  fun getNormByGuid(
      @Parameter(
          name = "guid", description = "the unique guid identifier of a norm", required = true)
      @PathVariable
      guid: String
  ): Mono<ResponseEntity<NormResponseSchema>> {
    val query = LoadNormUseCase.Query(UUID.fromString(guid))
    return loadNormService
        .loadNorm(query)
        .map { norm -> NormResponseSchema.fromUseCaseData(norm) }
        .map { normResponseSchema -> ResponseEntity.ok(normResponseSchema) }
        .switchIfEmpty(Mono.error(NotFoundWithInstanceException(URI(guid))))
  }

  data class NormResponseSchema
  internal constructor(
      val guid: String,
      val metadataSections: Collection<MetadataSectionResponseSchema>,
      var eli: String,
      var files: Collection<FileReferenceResponseSchema>,
      val recitals: RecitalsResponseSchema?,
      val formula: FormulaResponseSchema?,
      val documentation: Collection<DocumentationResponseSchema>,
      val conclusion: ConclusionResponseSchema?,
  ) {
    companion object {
      fun fromUseCaseData(data: Norm): NormResponseSchema {
        val recitals = data.recitals?.let(RecitalsResponseSchema::fromUseCaseData)
        val formula = data.formula?.let(FormulaResponseSchema::fromUseCaseData)
        val documentation = data.documentation.map(DocumentationResponseSchema::fromUseCaseData)
        val conclusion = data.conclusion?.let(ConclusionResponseSchema::fromUseCaseData)
        val files = data.files.map(FileReferenceResponseSchema::fromUseCaseData)
        val metadataSections =
            data.metadataSections.map(MetadataSectionResponseSchema::fromUseCaseData)

        return NormResponseSchema(
            guid = encodeGuid(data.guid),
            metadataSections = metadataSections,
            eli = encodeEli(data.eli),
            files = files,
            recitals = recitals,
            formula = formula,
            documentation = documentation,
            conclusion = conclusion,
        )
      }
    }
  }

  abstract interface DocumentationResponseSchema {
    val guid: String
    val order: Int
    val marker: String
    val heading: String?

    companion object {
      fun fromUseCaseData(data: Documentation): DocumentationResponseSchema {
        return when (data) {
          is DocumentSection -> DocumentSectionResponseSchema.fromUseCaseData(data)
          is Article -> ArticleResponseSchema.fromUseCaseData(data)
        }
      }
    }
  }

  data class DocumentSectionResponseSchema
  internal constructor(
      override val guid: String,
      override val order: Int,
      override val marker: String,
      override val heading: String,
      val type: String,
      val documentation: Collection<DocumentationResponseSchema>,
  ) : DocumentationResponseSchema {
    companion object {
      fun fromUseCaseData(data: DocumentSection) =
          DocumentSectionResponseSchema(
              guid = encodeGuid(data.guid),
              order = data.order,
              marker = data.marker,
              heading = data.heading,
              type = data.type.toString(),
              documentation = data.documentation.map(DocumentationResponseSchema::fromUseCaseData))
    }
  }

  data class ArticleResponseSchema
  internal constructor(
      override val guid: String,
      override val order: Int,
      override val marker: String,
      override val heading: String?,
      val paragraphs: Collection<ParagraphResponseSchema>,
  ) : DocumentationResponseSchema {
    companion object {
      fun fromUseCaseData(data: Article) =
          ArticleResponseSchema(
              guid = encodeGuid(data.guid),
              order = data.order,
              marker = data.marker,
              heading = data.heading,
              paragraphs = data.paragraphs.map(ParagraphResponseSchema::fromUseCaseData),
          )
    }
  }

  data class ParagraphResponseSchema
  private constructor(val guid: String, val marker: String? = null, val text: String) {
    companion object {
      fun fromUseCaseData(data: Paragraph): ParagraphResponseSchema {
        return ParagraphResponseSchema(encodeGuid(data.guid), data.marker, data.text)
      }
    }
  }

  data class RecitalsResponseSchema
  private constructor(
      val guid: String,
      val marker: String?,
      val heading: String?,
      val text: String
  ) {
    companion object {
      fun fromUseCaseData(data: Recitals): RecitalsResponseSchema {
        return RecitalsResponseSchema(encodeGuid(data.guid), data.marker, data.heading, data.text)
      }
    }
  }

  data class FormulaResponseSchema private constructor(val guid: String, val text: String) {
    companion object {
      fun fromUseCaseData(data: Formula): FormulaResponseSchema {
        return FormulaResponseSchema(encodeGuid(data.guid), data.text)
      }
    }
  }

  data class ConclusionResponseSchema private constructor(val guid: String, val text: String) {
    companion object {
      fun fromUseCaseData(data: Conclusion): ConclusionResponseSchema {
        return ConclusionResponseSchema(encodeGuid(data.guid), data.text)
      }
    }
  }

  data class FileReferenceResponseSchema
  internal constructor(
      val guid: String,
      val name: String,
      val hash: String,
      val createdAt: String
  ) {
    companion object {
      fun fromUseCaseData(data: FileReference) =
          FileReferenceResponseSchema(
              encodeGuid(data.guid),
              data.name,
              data.hash,
              encodeLocalDateTime(data.createdAt),
          )
    }
  }

  data class MetadataSectionResponseSchema
  internal constructor(
      val guid: String,
      val name: MetadataSectionName,
      val order: Int,
      val metadata: List<MetadatumResponseSchema>,
      val sections: List<MetadataSectionResponseSchema>?
  ) {
    companion object {
      fun fromUseCaseData(metadataSection: MetadataSection): MetadataSectionResponseSchema {
        val metadata = metadataSection.metadata.map { MetadatumResponseSchema.fromUseCaseData(it) }
        val childSections = metadataSection.sections?.map { fromUseCaseData(it) }
        return MetadataSectionResponseSchema(
            guid = encodeGuid(metadataSection.guid),
            name = metadataSection.name,
            order = metadataSection.order,
            metadata = metadata,
            sections = childSections)
      }
    }
  }

  data class MetadatumResponseSchema
  internal constructor(val guid: String, val value: String, val type: String, val order: Int) {
    companion object {
      fun fromUseCaseData(metadatum: Metadatum<*>): MetadatumResponseSchema {
        val value: String = metadatum.value.toString()
        val type = metadatum.type.name
        return MetadatumResponseSchema(
            guid = encodeGuid(metadatum.guid), value = value, type = type, order = metadatum.order)
      }
    }
  }
}
