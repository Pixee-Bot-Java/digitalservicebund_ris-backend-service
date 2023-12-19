import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Pagination from "@/shared/components/Pagination.vue"

function renderComponent(options?: {
  currentPage?: number
  getInitialData?: boolean
  navigationPosition?: "top" | "bottom"
  last?: boolean
  first?: boolean
  empty?: boolean
}) {
  return render(Pagination, {
    props: {
      page: {
        content: [1, 2, 3, 4, 5],
        size: 100,
        numberOfElements: 100,
        number: options?.currentPage ?? 0,
        first: options?.first ?? true,
        last: options?.last ?? true,
        empty: options?.empty ?? false,
      },
      ...(options?.navigationPosition
        ? { navigationPosition: options.navigationPosition }
        : {}),
    },
    slots: { default: "<div>list slot</div>" },
  })
}

describe("Pagination", () => {
  test("display navigation", async () => {
    renderComponent()

    for (const element of ["zurück", "vor"]) {
      await screen.findByText(element)
    }
  })

  test("pagination is at bottom position", async () => {
    renderComponent({ navigationPosition: "bottom" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(4)
  })

  test("pagination is on top", async () => {
    renderComponent({ navigationPosition: "top" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(2)
  })

  test("displays 0 pages if there a no pages", async () => {
    renderComponent({ empty: true })

    await screen.findByText("Keine Ergebnisse")
  })

  test("updates string correctly for one result", async () => {
    renderComponent({ getInitialData: true })

    await screen.findByText("100 Ergebniss(e) auf Seite 1")
  })

  test("next button disabled if on last page", async () => {
    renderComponent({ last: true })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeDisabled()
  })

  test("next button enabled if not on last page", async () => {
    renderComponent({ last: false })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeEnabled()
  })

  test("previous button disabled if on first page", async () => {
    renderComponent({ first: true })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeDisabled()
  })

  test("previous button enabled if not first page", async () => {
    renderComponent({ first: false })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeEnabled()
  })

  test("emits correct event at click on next Page", async () => {
    const { emitted } = renderComponent({
      last: false,
      first: false,
      currentPage: 3,
    })

    const user = userEvent.setup()
    await user.click(await screen.findByLabelText("nächste Ergebnisse"))
    expect(emitted()["updatePage"]).toHaveLength(1)
    expect(emitted()["updatePage"][0]).toEqual([4])

    await user.click(await screen.findByLabelText("vorherige Ergebnisse"))
    expect(emitted()["updatePage"]).toHaveLength(2)
    expect(emitted()["updatePage"][1]).toEqual([2])
  })
})
