import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitProceedingDecisions from "@/components/proceedingDecisions/ProceedingDecisions.vue"
import type { ProceedingDecision } from "@/domain/documentUnit"
import service from "@/services/proceedingDecisionService"

function renderComponent(options?: {
  documentUnitUuid?: string
  proceedingDecisions?: ProceedingDecision[]
}) {
  const props = {
    documentUnitUuid: options?.documentUnitUuid
      ? options?.documentUnitUuid
      : "fooUuid",
    proceedingDecisions: options?.proceedingDecisions,
  }

  const user = userEvent.setup()
  return { user, ...render(DocumentUnitProceedingDecisions, { props }) }
}

async function openExpandableArea(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByText("Vorgehende Entscheidungen"))
}

describe("DocumentUnitProceedingDecisions", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  const fetchSpy = vi
    .spyOn(service, "addProceedingDecision")
    .mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          {
            court: { type: "type1", location: "location1", label: "label1" },
            date: "2022-02-01",
            documentType: {
              jurisShortcut: "documentTypeShortcut1",
              label: "documentType1",
            },
            fileNumber: "testFileNumber1",
          },
          {
            court: { type: "type2", location: "location2", label: "label2" },
            date: "2022-02-02",
            documentType: {
              jurisShortcut: "documentTypeShortcut2",
              label: "documentType2",
            },
            fileNumber: "testFileNumber2",
          },
        ],
      })
    )

  it("shows all proceeding decision input fields if expanded", async () => {
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.getByLabelText("Gericht Rechtszug")).toBeVisible()
    expect(screen.getByLabelText("Datum Rechtszug")).toBeInTheDocument()
    expect(screen.getByLabelText("Aktenzeichen Rechtszug")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumenttyp Rechtszug")).toBeInTheDocument()
  })

  it("adds proceeding decision and updates list of existing ones", async () => {
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.queryByText(/testFileNumber1/)).not.toBeInTheDocument()
    expect(screen.queryByText(/testFileNumber2/)).not.toBeInTheDocument()

    await user.click(screen.getByLabelText("Entscheidung manuell hinzufügen"))
    expect(fetchSpy).toBeCalledTimes(1)

    expect(screen.getByText(/testFileNumber1/)).toBeVisible()
    expect(screen.getByText(/testFileNumber2/)).toBeVisible()
  })

  // it("does not emit update model event when inputs are empty and model is empty too", async () => {
  //   const { emitted, user } = renderComponent({
  //     modelValue: undefined,
  //   })
  //   const input = screen.getByLabelText("fileNumber")

  //   // Do anything without changing the inputs.
  //   await user.click(input)

  //   expect(emitted()["update:modelValue"]).toBeUndefined()
  // })

  // it("always shows at least one input group despite empty model list", () => {
  //   renderComponent({ modelValue: [] })

  //   const courtInput = screen.queryByLabelText(
  //     "Gericht Rechtszug"
  //   ) as HTMLInputElement
  //   const dateInput = screen.queryByLabelText(
  //     "Datum Rechtszug"
  //   ) as HTMLInputElement
  //   const identifierInput = screen.queryByLabelText(
  //     "Aktenzeichen Rechtszug"
  //   ) as HTMLInputElement

  //   expect(courtInput).toBeInTheDocument()
  //   expect(courtInput).toHaveDisplayValue("")
  //   expect(dateInput).toBeInTheDocument()
  //   expect(dateInput).toHaveDisplayValue("")
  //   expect(identifierInput).toBeInTheDocument()
  //   expect(identifierInput).toHaveDisplayValue("")
  // })
})
