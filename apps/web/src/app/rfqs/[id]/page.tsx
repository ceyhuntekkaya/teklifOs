"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { AppShell } from "@/components/app-shell";

type Candidate = {
  productId: string;
  score: number;
  matchMethod: string;
  rankOrder: number;
  selected: boolean;
};

type Line = {
  id: string;
  lineNumber: number;
  rawDescription: string | null;
  rawCustomerSku: string | null;
  matchStatus: string;
  matchedProductId: string | null;
  candidates: Candidate[];
};

type RfqDetail = {
  id: string;
  referenceCode: string;
  status: string;
  lines: Line[];
};

export default function RfqReviewPage() {
  const params = useParams();
  const id = params.id as string;
  const [detail, setDetail] = useState<RfqDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const res = await fetch(`/api/rfqs/${id}`);
      if (!res.ok) {
        if (!cancelled) setError("RFQ yüklenemedi");
        return;
      }
      const data = (await res.json()) as RfqDetail;
      if (!cancelled) setDetail(data);
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [id]);

  return (
    <AppShell>
      <div className="mb-4">
        <Link href="/rfqs" className="text-sm text-blue-600 hover:underline">
          ← RFQ listesi
        </Link>
      </div>
      <h1 className="text-2xl font-semibold mt-2">
        {detail?.referenceCode ?? "RFQ inceleme"}
      </h1>
      {detail && (
        <p className="text-slate-600 mt-1">
          Durum: <span className="font-medium">{detail.status}</span>
        </p>
      )}
      {error && <p className="text-red-600 mt-4">{error}</p>}
      {detail && detail.lines.length === 0 && (
        <p className="text-slate-600 mt-6">Henüz eşleştirilmiş satır yok.</p>
      )}
      {detail && detail.lines.length > 0 && (
        <div className="mt-6 overflow-x-auto rounded-lg border border-slate-200">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left">
              <tr>
                <th className="px-3 py-2">#</th>
                <th className="px-3 py-2">Açıklama / Kod</th>
                <th className="px-3 py-2">Eşleşme</th>
                <th className="px-3 py-2">Adaylar</th>
              </tr>
            </thead>
            <tbody>
              {detail.lines.map((line) => (
                <tr key={line.id} className="border-t border-slate-100">
                  <td className="px-3 py-2">{line.lineNumber}</td>
                  <td className="px-3 py-2">
                    <div>{line.rawCustomerSku ?? "—"}</div>
                    <div className="text-slate-500">{line.rawDescription}</div>
                  </td>
                  <td className="px-3 py-2">
                    <span
                      className={
                        line.matchStatus === "AUTO"
                          ? "text-green-700 font-medium"
                          : "text-amber-700"
                      }
                    >
                      {line.matchStatus}
                    </span>
                    {line.matchedProductId && (
                      <div className="text-xs text-slate-500 truncate max-w-[12rem]">
                        {line.matchedProductId}
                      </div>
                    )}
                  </td>
                  <td className="px-3 py-2">
                    <ul className="space-y-1">
                      {line.candidates.slice(0, 3).map((c) => (
                        <li key={`${line.id}-${c.productId}`} className="text-xs">
                          {c.matchMethod} · {(Number(c.score) * 100).toFixed(0)}%
                        </li>
                      ))}
                    </ul>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </AppShell>
  );
}
