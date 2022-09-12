import service from "@/services/docUnitService"
import { ServiceResponse } from "@/services/httpClient"

describe("docUnitService", () => {
  vi.mock("@/services/api", () => {
    const testResponse: ServiceResponse<string> = {
      status: 500,
      data: "foo",
    }
    return {
      default: {
        get: vi.fn().mockReturnValue(testResponse),
      },
    }
  })

  it("appends correct error message if status 500", async () => {
    const result = await service.getAll()
    expect(result.error?.title).toEqual(
      "Dokumentationseinheiten konnten nicht geladen werden."
    )
  })
})
