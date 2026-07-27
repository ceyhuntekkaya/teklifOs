import { cookies } from "next/headers";
import { gatewayFetch } from "@/lib/gateway";

async function accessToken() {
  return (await cookies()).get("teklifos_access")?.value;
}

export async function GET(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  const token = await accessToken();
  if (!token) {
    return Response.json({ error: "unauthorized" }, { status: 401 });
  }
  const { id } = await context.params;
  const res = await gatewayFetch(`/api/v1/rfqs/${id}`, { accessToken: token });
  return Response.json(await res.json(), { status: res.status });
}
