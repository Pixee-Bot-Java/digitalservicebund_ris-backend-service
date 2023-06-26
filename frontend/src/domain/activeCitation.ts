import dayjs from "dayjs"
import { CitationStyle } from "./citationStyle"
import LinkedDocumentUnit from "./linkedDocumentUnit"

export default class ActiveCitation extends LinkedDocumentUnit {
  public citationStyle?: CitationStyle

  static requiredFields = ["court"] as const

  constructor(data: Partial<ActiveCitation> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    console.log(this.citationStyle)
    return [
      ...(this.citationStyle?.label ? [this.citationStyle.label] : []),
      ...(this.court?.label ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.documentType ? [this.documentType.label] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.isDocUnit() ? [this.documentNumber] : []),
    ].join(", ")
  }

  get missingRequiredFields() {
    return ActiveCitation.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(this[field])
    )
  }

  private requiredFieldIsEmpty(
    value: ActiveCitation[(typeof ActiveCitation.requiredFields)[number]]
  ) {
    if (value === undefined || !value || value === null) {
      return true
    }

    return false
  }
}
