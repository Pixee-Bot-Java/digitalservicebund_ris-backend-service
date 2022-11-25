export type Article = {
  guid: string
  title: string
  marker: string
  readonly paragraphs: Paragraph[]
}

export type Paragraph = {
  guid: string
  marker: string
  text: string
}

export class Norm {
  readonly longTitle: string
  readonly guid: string
  readonly articles: Article[]
  readonly officialShortTitle: string
  readonly officialAbbreviation: string
  readonly referenceNumber: string
  readonly publicationDate: string
  readonly announcementDate: string
  readonly citationDate: string
  readonly frameWords: string
  readonly authorEntity: string
  readonly authorDecidingBody: string
  readonly authorIsResolutionMajority: boolean
  readonly leadJurisdiction: string
  readonly leadUnit: string
  readonly participationType: string
  readonly participationInstitution: string
  readonly documentTypeName: string
  readonly documentNormCategory: string
  readonly documentTemplateName: string
  readonly subjectFna: string
  readonly subjectPreviousFna: string
  readonly subjectGesta: string
  readonly subjectBgb3: string

  constructor(
    longTitle: string,
    guid: string,
    articles: Article[],
    officialShortTitle: string,
    officialAbbreviation: string,
    referenceNumber: string,
    publicationDate: string,
    announcementDate: string,
    citationDate: string,
    frameWords: string,
    authorEntity: string,
    authorDecidingBody: string,
    authorIsResolutionMajority: boolean,
    leadJurisdiction: string,
    leadUnit: string,
    participationType: string,
    participationInstitution: string,
    documentTypeName: string,
    documentNormCategory: string,
    documentTemplateName: string,
    subjectFna: string,
    subjectPreviousFna: string,
    subjectGesta: string,
    subjectBgb3: string
  ) {
    ;(this.longTitle = longTitle),
      (this.guid = guid),
      (this.articles = articles),
      (this.officialShortTitle = officialShortTitle),
      (this.officialAbbreviation = officialAbbreviation),
      (this.referenceNumber = referenceNumber),
      (this.publicationDate = publicationDate),
      (this.announcementDate = announcementDate),
      (this.citationDate = citationDate),
      (this.frameWords = frameWords),
      (this.authorEntity = authorEntity),
      (this.authorDecidingBody = authorDecidingBody),
      (this.authorIsResolutionMajority = authorIsResolutionMajority),
      (this.leadJurisdiction = leadJurisdiction),
      (this.leadUnit = leadUnit),
      (this.participationType = participationType),
      (this.participationInstitution = participationInstitution),
      (this.documentTypeName = documentTypeName),
      (this.documentNormCategory = documentNormCategory),
      (this.documentTemplateName = documentTemplateName),
      (this.subjectFna = subjectFna),
      (this.subjectPreviousFna = subjectPreviousFna),
      (this.subjectGesta = subjectGesta),
      (this.subjectBgb3 = subjectBgb3)
  }
}
