package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.BOOK
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.CHAPTER
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import java.time.LocalDate
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.createSimpleMetadataSections
import utils.factory.norm

class NormTest {

  @Test
  fun `can create a norm with only mandatory fields`() {
    val guid = UUID.randomUUID()
    val norm = Norm(guid = guid)

    assertThat(norm.guid).isEqualTo(guid)
  }

  @Test
  fun `can create a norm with a list of metadata`() {
    val guid = UUID.randomUUID()
    val sections = createSimpleMetadataSections()
    val norm =
        Norm(
            guid = guid,
            metadataSections = createSimpleMetadataSections(),
        )

    assertThat(norm.metadataSections.flatMap { it.metadata }).hasSize(2)
    assertThat(norm.metadataSections.flatMap { it.metadata })
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid")
        .containsAll(sections.first().metadata)
  }

  @Test
  fun `can create a norm with some optional string fields`() {
    val guid = UUID.randomUUID()
    val norm =
        Norm(
            guid = guid,
            metadataSections =
                listOf(
                    MetadataSection(
                        name = MetadataSectionName.NORM,
                        metadata =
                            listOf(
                                Metadatum("short title", MetadatumType.OFFICIAL_SHORT_TITLE),
                                Metadatum("ABC", MetadatumType.OFFICIAL_ABBREVIATION),
                                Metadatum("ABC", MetadatumType.RIS_ABBREVIATION),
                            ),
                    ),
                ),
        )

    assertThat(norm.guid).isEqualTo(guid)
    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_SHORT_TITLE)
                ?.value
                .toString())
        .isEqualTo("short title")
    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_ABBREVIATION)
                ?.value
                .toString())
        .isEqualTo("ABC")
    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.RIS_ABBREVIATION)
                ?.value
                .toString())
        .isEqualTo("ABC")
  }

  @Test
  fun `can create a norm with optional date and boolean fields`() {
    val guid = UUID.randomUUID()

    val citationDate = Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE)
    val citationDateSection =
        MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))

    val resolutionMajority = Metadatum(true, MetadatumType.RESOLUTION_MAJORITY)
    val normProviderSection =
        MetadataSection(MetadataSectionName.NORM_PROVIDER, listOf(resolutionMajority))

    val risAbbreviation = Metadatum("ABC", MetadatumType.RIS_ABBREVIATION)
    val text = Metadatum("text", MetadatumType.TEXT)
    val normSection = MetadataSection(MetadataSectionName.NORM, listOf(risAbbreviation, text))

    val norm =
        Norm(
            guid = guid,
            metadataSections = listOf(citationDateSection, normProviderSection, normSection),
        )

    assertThat(norm.guid).isEqualTo(guid)
    assertThat(norm.metadataSections.flatMap { it.metadata }).contains(citationDate)
    assertThat(norm.metadataSections.flatMap { it.metadata }).contains(resolutionMajority)
    assertThat(norm.metadataSections.flatMap { it.metadata }).contains(risAbbreviation)
    assertThat(norm.metadataSections.flatMap { it.metadata }).contains(text)
  }

  @Test
  fun `it can create norm with only articles as documentation`() {
    val paragraph = Paragraph(UUID.randomUUID(), "(1)", "text")
    val article1 = Article(UUID.randomUUID(), 1, "§ 1", paragraphs = listOf(paragraph))
    val article2 = Article(UUID.randomUUID(), 2, "§ 2")
    val norm = Norm(UUID.randomUUID(), documentation = listOf(article1, article2))

    assertThat(norm.documentation).containsOnly(article1, article2)
  }

  @Test
  fun `it can create norm with nested documentation structure`() {
    val paragraph1 = Paragraph(UUID.randomUUID(), "(1)", "text 1")
    val article1 = Article(UUID.randomUUID(), 1, "§ 1", paragraphs = listOf(paragraph1))
    val article2 = Article(UUID.randomUUID(), 2, "§ 2")
    val chapter1 =
        DocumentSection(UUID.randomUUID(), 1, "1", "Chapter 1", CHAPTER, listOf(article1, article2))
    val paragraph2 = Paragraph(UUID.randomUUID(), "(2)", "text 2")
    val article3 = Article(UUID.randomUUID(), 3, "§ 3", paragraphs = listOf(paragraph2))
    val chapter2 =
        DocumentSection(UUID.randomUUID(), 2, "2", "Chapter 2", CHAPTER, listOf(article3))
    val book =
        DocumentSection(
            UUID.randomUUID(), 1, "1", "Book 1", BOOK, documentation = listOf(chapter1, chapter2))
    val norm = Norm(UUID.randomUUID(), documentation = listOf(book))

    assertThat(norm.documentation).containsOnly(book)
  }

  @Test
  fun `it can create a proper eli from the respective sections`() {
    val printAnnouncementSection =
        MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE,
            listOf(),
            1,
            listOf(
                MetadataSection(
                    MetadataSectionName.PRINT_ANNOUNCEMENT,
                    listOf(
                        Metadatum("BGBl I", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                        Metadatum("BGBL II", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
                        Metadatum("1102", MetadatumType.PAGE, 1),
                        Metadatum("1102", MetadatumType.PAGE, 2),
                    ),
                ),
            ),
        )
    val citationDateSection =
        MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(
                Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE),
            ),
        )

    val announcmentDateSection =
        MetadataSection(
            MetadataSectionName.ANNOUNCEMENT_DATE,
            listOf(
                Metadatum(LocalDate.parse("2022-11-19"), MetadatumType.DATE),
            ),
        )
    val guid = UUID.randomUUID()

    val norm =
        Norm(
            guid = guid,
            metadataSections =
                listOf(printAnnouncementSection, citationDateSection, announcmentDateSection),
        )

    assertThat(norm.eli.gazetteOrMedium).isEqualTo("bgbl-1")
    assertThat(norm.eli.printAnnouncementGazette).isEqualTo("BGBl I")
    assertThat(norm.eli.citationDate).isEqualTo(LocalDate.of(2022, 11, 19))
    assertThat(norm.eli.announcementYear).isEqualTo(2022)
    assertThat(norm.eli.toString()).isEqualTo("eli/bgbl-1/2022/s1102")
  }

  @Test
  fun `it creates the eli with the year of the date from the announcement date section`() {
    val norm =
        Norm(
            guid = UUID.randomUUID(),
            metadataSections =
                listOf(
                    MetadataSection(
                        MetadataSectionName.ANNOUNCEMENT_DATE,
                        listOf(
                            Metadatum(LocalDate.of(2021, 11, 18), MetadatumType.DATE),
                        ),
                    ),
                ),
        )

    assertThat(norm.eli.announcementYear).isEqualTo(2021)
  }

  @Test
  fun `it creates the eli with the year of the announcement date section`() {
    val norm =
        Norm(
            guid = UUID.randomUUID(),
            metadataSections =
                listOf(
                    MetadataSection(
                        MetadataSectionName.ANNOUNCEMENT_DATE,
                        listOf(
                            Metadatum("2012", MetadatumType.YEAR),
                        ),
                    ),
                ),
        )

    assertThat(norm.eli.announcementYear).isEqualTo(2012)
  }

  @Test
  fun `can create a norm using type safe builders`() {
    val norm = norm {
      recitals {
        marker = "recitals"
        heading = "Recitals"
        text = "recitals text"
      }
      formula { text = "formula text" }
      documentation {
        documentSection {
          order = 1
          marker = "1"
          heading = "book 1"
        }
      }
      conclusion { text = "conclusion text" }
      metadataSections {
        metadataSection {
          name = MetadataSectionName.OFFICIAL_REFERENCE
          metadataSections {
            metadataSection {
              name = MetadataSectionName.PRINT_ANNOUNCEMENT
              metadata {
                metadatum {
                  value = "PrintAnnouncementGazette"
                  type = MetadatumType.ANNOUNCEMENT_GAZETTE
                }
              }
            }
          }
        }
      }
      files { file { name = "file.zip" } }
    }
    assertThat(norm.documentation.first().order).isEqualTo(1)
    assertThat(norm.documentation.first().marker).isEqualTo("1")
    assertThat(norm.documentation.first().heading).isEqualTo("book 1")
    assertThat(norm.metadataSections.first().name).isEqualTo(MetadataSectionName.OFFICIAL_REFERENCE)
    assertThat(norm.metadataSections.first().metadata).isEmpty()
    assertThat(norm.metadataSections.first().sections?.first()?.metadata?.first()?.type)
        .isEqualTo(MetadatumType.ANNOUNCEMENT_GAZETTE)
    assertThat(norm.metadataSections.first().sections?.first()?.metadata?.first()?.value)
        .isEqualTo("PrintAnnouncementGazette")
    assertThat(norm.files.first().name).isEqualTo("file.zip")
    assertThat(norm.recitals?.marker).isEqualTo("recitals")
    assertThat(norm.recitals?.heading).isEqualTo("Recitals")
    assertThat(norm.recitals?.text).isEqualTo("recitals text")
    assertThat(norm.formula?.text).isEqualTo("formula text")
    assertThat(norm.conclusion?.text).isEqualTo("conclusion text")
  }

  @Test
  fun `it can create retrieve first metadata in a flat sections list`() {
    val leadSection =
        MetadataSection(
            MetadataSectionName.LEAD,
            listOf(
                Metadatum("jurisdiction1", MetadatumType.LEAD_JURISDICTION, 1),
                Metadatum("jurisdiction2", MetadatumType.LEAD_JURISDICTION, 2),
                Metadatum("lead1", MetadatumType.LEAD_UNIT, 1),
                Metadatum("lead2", MetadatumType.LEAD_UNIT, 2),
            ),
        )
    val citationDateSection =
        MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(
                Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE),
            ),
        )

    val norm =
        Norm(
            guid = UUID.randomUUID(),
            metadataSections = listOf(leadSection, citationDateSection),
        )

    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.LEAD, MetadatumType.LEAD_JURISDICTION)
                ?.value)
        .isEqualTo("jurisdiction1")
    assertThat(norm.getFirstMetadatum(MetadataSectionName.LEAD, MetadatumType.LEAD_UNIT)?.value)
        .isEqualTo("lead1")
  }

  @Test
  fun `it can retrieve first metadata in a tree of sections with two levels`() {
    val printAnnouncementSection1 =
        MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("gazette1", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                Metadatum("gazette2", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
            ),
            1,
        )
    val printAnnouncementSection2 =
        MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("gazette3", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                Metadatum("gazette4", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
            ),
            2,
        )
    val referenceSection1 =
        MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection1))
    val referenceSection2 =
        MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 2, listOf(printAnnouncementSection2))

    val norm =
        Norm(
            guid = UUID.randomUUID(),
            metadataSections = listOf(referenceSection1, referenceSection2),
        )

    assertThat(
            norm
                .getFirstMetadatum(
                    MetadataSectionName.PRINT_ANNOUNCEMENT,
                    MetadatumType.ANNOUNCEMENT_GAZETTE,
                    MetadataSectionName.OFFICIAL_REFERENCE)
                ?.value)
        .isEqualTo("gazette1")
  }
}
