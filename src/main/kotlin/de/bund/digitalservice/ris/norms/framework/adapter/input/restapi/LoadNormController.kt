package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

    @GetMapping(path = ["/{guid}"])
    fun getNormByGuid(@PathVariable guid: String): Mono<ResponseEntity<NormResponseSchema>> {
        val query = LoadNormUseCase.Query(UUID.fromString(guid))

        return loadNormService
            .loadNorm(query)
            .map({ norm -> NormResponseSchema.from(norm) })
            .map({ normResponseSchema -> ResponseEntity.ok(normResponseSchema) })
            .defaultIfEmpty(ResponseEntity.notFound().build<NormResponseSchema>())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    data class NormResponseSchema
    private constructor(
        val guid: String,
        val longTitle: String,
        val articles: List<ArticleResponseSchema>,
        var officialShortTitle: String? = null,
        var officialAbbreviation: String? = null,
        var referenceNumber: String? = null,
        var publicationDate: String? = null,
        var announcementDate: String? = null,
        var citationDate: String? = null,
        var frameKeywords: String? = null,
        var authorEntity: String? = null,
        var authorDecidingBody: String? = null,
        var authorIsResolutionMajority: Boolean? = null,
        var leadJurisdiction: String? = null,
        var leadUnit: String? = null,
        var participationType: String? = null,
        var participationInstitution: String? = null,
        var documentTypeName: String? = null,
        var documentNormCategory: String? = null,
        var documentTemplateName: String? = null,
        var subjectFna: String? = null,
        var subjectPreviousFna: String? = null,
        var subjectGesta: String? = null,
        var subjectBgb3: String? = null
    ) {
        companion object {
            fun from(norm: Norm): NormResponseSchema {
                val articles = norm.articles.map { ArticleResponseSchema.from(it) }
                return NormResponseSchema(
                    norm.guid.toString(), norm.longTitle, articles, norm.officialShortTitle, norm.officialAbbreviation, norm.referenceNumber,
                    if (norm.publicationDate != null) norm.publicationDate.toString() else null,
                    if (norm.announcementDate != null) norm.announcementDate.toString() else null,
                    if (norm.citationDate != null) norm.citationDate.toString() else null,
                    norm.frameKeywords, norm.authorEntity,
                    norm.authorDecidingBody, norm.authorIsResolutionMajority, norm.leadJurisdiction, norm.leadUnit, norm.participationType,
                    norm.participationInstitution, norm.documentTypeName, norm.documentNormCategory, norm.documentTemplateName,
                    norm.subjectFna, norm.subjectPreviousFna, norm.subjectGesta, norm.subjectBgb3
                )
            }
        }
    }

    data class ArticleResponseSchema
    private constructor(
        val guid: String,
        var title: String? = null,
        val marker: String,
        val paragraphs: List<ParagraphResponseSchema>
    ) {
        companion object {
            fun from(article: Article): ArticleResponseSchema {
                val paragraphs = article.paragraphs.map { ParagraphResponseSchema.from(it) }
                return ArticleResponseSchema(
                    article.guid.toString(),
                    article.title,
                    article.marker,
                    paragraphs
                )
            }
        }
    }

    data class ParagraphResponseSchema
    private constructor(val guid: String, var marker: String? = null, val text: String) {
        companion object {
            fun from(paragraph: Paragraph): ParagraphResponseSchema {
                return ParagraphResponseSchema(paragraph.guid.toString(), paragraph.marker, paragraph.text)
            }
        }
    }
}
