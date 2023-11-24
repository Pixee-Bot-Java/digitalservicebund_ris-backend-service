import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import InlineDecision from "@/components/InlineDecision.vue"
import { Court, DocumentType } from "@/domain/documentUnit"
import PreviousDecision from "@/domain/previousDecision"

function renderComponent(options?: {
  court?: Court
  documentType?: DocumentType
  decisionDate?: string
  documentNumber?: string
  referencedDocumentationUnitId?: string
}) {
  const props: { decision: PreviousDecision } = {
    decision: new PreviousDecision({
      ...{
        court: options?.court ?? {
          type: "testCourtType",
          location: "testCourtLocation",
          label: "label1",
        },
        documentType: options?.documentType ?? {
          label: "testDocumentType",
          jurisShortcut: "testDocumentTypeShortcut",
        },
        decisionDate:
          options?.decisionDate ?? "2004-12-02 12:00:00.000000 +00:00",
        documentNumber: options?.documentNumber ?? undefined,
        referencedDocumentationUnitId:
          options?.referencedDocumentationUnitId ?? undefined,
      },
    }),
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "index",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {},
      },
    ],
  })

  return render(InlineDecision, {
    props,
    global: { plugins: [router] },
  })
}

describe("Decision ListItem", () => {
  it("renders court correctly", async () => {
    renderComponent({
      court: { type: "foo", location: "bar", label: "testLabel" },
    })
    expect(await screen.findByText(/testLabel/)).toBeVisible()
  })

  it("renders documentType shortcut", async () => {
    renderComponent({
      documentType: {
        label: "fooLabel",
        jurisShortcut: "barShortcut",
      },
    })
    expect(await screen.findByText(/barShortcut/)).toBeVisible()
  })

  it("renders date correctly", async () => {
    renderComponent({ decisionDate: "2022-03-27" })
    expect(await screen.findByText(/27.03.2022/)).toBeVisible()
  })

  it("renders with link if linked to docunit", async () => {
    renderComponent({
      documentNumber: "fooDocumentNumber",
      referencedDocumentationUnitId: "abc",
    })
    expect(screen.getByRole("link")).toHaveAttribute(
      "href",
      expect.stringMatching(/fooDocumentNumber/),
    )
  })
})
