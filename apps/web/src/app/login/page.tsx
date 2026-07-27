"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const fd = new FormData(e.currentTarget);
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: fd.get("email"),
        password: fd.get("password"),
        tenantSlug: fd.get("tenantSlug") || "demo",
      }),
    });
    setLoading(false);
    if (!res.ok) {
      setError("Giriş başarısız");
      return;
    }
    router.push("/dashboard");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100">
      <form
        onSubmit={onSubmit}
        className="w-full max-w-md bg-white p-8 rounded-xl shadow border space-y-4"
      >
        <h1 className="text-xl font-semibold">TeklifOS</h1>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <input
          name="tenantSlug"
          placeholder="Tenant (demo)"
          defaultValue="demo"
          className="w-full border rounded px-3 py-2"
        />
        <input
          name="email"
          type="email"
          required
          placeholder="E-posta"
          defaultValue="admin@demo.local"
          className="w-full border rounded px-3 py-2"
        />
        <input
          name="password"
          type="password"
          required
          placeholder="Şifre"
          className="w-full border rounded px-3 py-2"
        />
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-700 text-white py-2 rounded font-medium"
        >
          {loading ? "..." : "Giriş"}
        </button>
      </form>
    </div>
  );
}
