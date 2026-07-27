import { cookies } from "next/headers";
import { gatewayFetch } from "@/lib/gateway";

const ACCESS = "teklifos_access";
const REFRESH = "teklifos_refresh";

export async function POST(request: Request) {
  const body = await request.json();
  const res = await gatewayFetch("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    return Response.json(await res.json().catch(() => ({})), { status: res.status });
  }
  const tokens = await res.json();
  const jar = await cookies();
  jar.set(ACCESS, tokens.accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: tokens.expiresIn ?? 900,
  });
  jar.set(REFRESH, tokens.refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 60 * 24 * 30,
  });
  return Response.json({ ok: true });
}
