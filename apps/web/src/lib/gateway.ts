const GATEWAY = process.env.GATEWAY_URL ?? "http://localhost:8080";

export async function gatewayFetch(
  path: string,
  init?: RequestInit & { accessToken?: string },
) {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  if (init?.accessToken) {
    headers.set("Authorization", `Bearer ${init.accessToken}`);
  }
  return fetch(`${GATEWAY}${path}`, { ...init, headers });
}
