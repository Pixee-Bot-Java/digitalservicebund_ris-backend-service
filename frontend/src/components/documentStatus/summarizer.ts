import { createTextVNode, VNode } from "vue"
import { Metadata, MetadataSections } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

function documentStatusSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const workNote = data?.WORK_NOTE?.[0]
  if (workNote) {
    summarizerData.push(new SummarizerDataSet([workNote]))
  }
  const description = data?.DESCRIPTION?.[0]
  if (description) {
    summarizerData.push(new SummarizerDataSet([description]))
  }
  const date = data.DATE?.[0]
  if (date) {
    summarizerData.push(
      new SummarizerDataSet([date], { type: Type.DATE, format: "DD.MM.YYYY" }),
    )
  }
  const year = data?.YEAR?.[0]
  if (year) {
    summarizerData.push(new SummarizerDataSet([year]))
  }
  const reference = data?.REFERENCE?.[0]
  if (reference) {
    summarizerData.push(new SummarizerDataSet([reference]))
  }
  const entryIntoForceDateState = data?.ENTRY_INTO_FORCE_DATE_NOTE ?? []
  if (entryIntoForceDateState.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(entryIntoForceDateState, { separator: "," }),
    )
  }

  const proofIndication = data?.PROOF_INDICATION?.[0]
  if (proofIndication) {
    summarizerData.push(new SummarizerDataSet([proofIndication]))
  }

  return normsMetadataSummarizer(summarizerData)
}

function documentTextProofSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const text = data?.TEXT?.[0]

  if (text) {
    summarizerData.push(new SummarizerDataSet([text]))
  }

  return normsMetadataSummarizer(summarizerData, "")
}

function documentOtherSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const text = data?.TEXT?.[0]

  if (text) {
    summarizerData.push(new SummarizerDataSet([text]))
  }

  return normsMetadataSummarizer(summarizerData)
}

export function documentStatusSectionSummarizer(data: MetadataSections): VNode {
  if (!data) return createTextVNode("")

  if (data.DOCUMENT_STATUS) {
    return documentStatusSummary(data.DOCUMENT_STATUS[0])
  } else if (data.DOCUMENT_TEXT_PROOF) {
    return documentTextProofSummary(data.DOCUMENT_TEXT_PROOF[0])
  } else if (data.DOCUMENT_OTHER) {
    return documentOtherSummary(data.DOCUMENT_OTHER[0])
  }
  return createTextVNode("")
}
