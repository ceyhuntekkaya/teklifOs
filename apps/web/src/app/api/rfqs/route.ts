import { cookies } from "next/headers";
import { gatewayFetch } from "@/lib/gateway";

async function accessToken() {
  return (await cookies()).get("teklifos_access")?.value;
}

export async function GET(request: Request) {
  const token = await accessToken();
  if (!token) {
    return Response.json({ error: "unauthorized" }, { status: 401 });
  }
  const url = new URL(request.url);
  const qs = url.search;
  const res = await gatewayFetch(`/api/v1/rfqs${qs}`, { accessToken: token });
  return Response.json(await res.json(), { status: res.status });
}
