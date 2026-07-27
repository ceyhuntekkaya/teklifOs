import { cookies } from "next/headers";

const GATEWAY = process.env.GATEWAY_URL ?? "http://localhost:8080";

export async function POST(request: Request) {
  const token = (await cookies()).get("teklifos_access")?.value;
  if (!token) {
    return Response.json({ error: "unauthorized" }, { status: 401 });
  }
  const form = await request.formData();
  const res = await fetch(`${GATEWAY}/api/v1/rfqs/upload`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: form,
  });
  const text = await res.text();
  return new Response(text, {
    status: res.status,
    headers: { "Content-Type": "application/json" },
  });
}
