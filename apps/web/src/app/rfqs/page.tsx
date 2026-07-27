"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AppShell } from "@/components/app-shell";

type RfqSummary = {
  id: string;
  referenceCode: string;
  status: string;
  sourceChannel: string | null;
  receivedAt: string;
};

async function fetchRfqs(statusFilter: string): Promise<RfqSummary[]> {
  const q = statusFilter ? `?status=${encodeURIComponent(statusFilter)}` : "";
  const res = await fetch(`/api/rfqs${q}`);
  if (!res.ok) {
    return [];
  }
  return res.json();
}

export default function RfqsPage() {
  const [items, setItems] = useState<RfqSummary[]>([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    fetchRfqs(statusFilter).then((data) => {
      if (active) {
        setItems(data);
      }
    });
    return () => {
      active = false;
    };
  }, [statusFilter]);

  async function onUpload(files: FileList | null) {
    if (!files?.length) return;
    setUploading(true);
    setError(null);
    const fd = new FormData();
    Array.from(files).forEach((f) => fd.append("files", f));
    const res = await fetch("/api/rfqs/upload", { method: "POST", body: fd });
    setUploading(false);
    if (!res.ok) {
      setError("Yükleme başarısız");
      return;
    }
    setItems(await fetchRfqs(statusFilter));
  }

  return (
    <AppShell>
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 className="text-2xl font-semibold">Talepler (RFQ)</h1>
        <select
          className="border rounded px-2 py-1"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">Tüm durumlar</option>
          <option value="RECEIVED">Alındı</option>
          <option value="READY_FOR_REVIEW">İncelemeye hazır</option>
          <option value="FAILED">Hatalı</option>
        </select>
      </div>
      <label className="block border-2 border-dashed rounded-lg p-8 text-center bg-white mb-6 cursor-pointer hover:border-blue-500">
        <input
          type="file"
          multiple
          className="hidden"
          onChange={(e) => onUpload(e.target.files)}
          disabled={uploading}
        />
        {uploading ? "Yükleniyor..." : "PDF veya Excel sürükleyin / seçin"}
      </label>
      {error && <p className="text-red-600 text-sm mb-4">{error}</p>}
      <table className="w-full text-sm bg-white border rounded-lg overflow-hidden">
        <thead className="bg-slate-100 text-left">
          <tr>
            <th className="p-3">Referans</th>
            <th className="p-3">Durum</th>
            <th className="p-3">Kaynak</th>
            <th className="p-3">Alınma</th>
          </tr>
        </thead>
        <tbody>
          {items.map((r) => (
            <tr key={r.id} className="border-t">
              <td className="p-3">
                <Link href={`/rfqs/${r.id}`} className="text-blue-700 hover:underline">
                  {r.referenceCode}
                </Link>
              </td>
              <td className="p-3">{r.status}</td>
              <td className="p-3">{r.sourceChannel ?? "—"}</td>
              <td className="p-3">{new Date(r.receivedAt).toLocaleString("tr-TR")}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </AppShell>
  );
}
