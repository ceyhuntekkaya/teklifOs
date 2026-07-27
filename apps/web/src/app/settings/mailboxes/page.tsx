"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";

type Mailbox = {
  id: string;
  email: string;
  display_name: string | null;
  imap_host: string;
  verification_status: string;
  last_poll_at: string | null;
  last_error: string | null;
};

const MAIL_BASE = process.env.NEXT_PUBLIC_MAIL_API ?? "http://localhost:9001";

export default function MailboxesSettingsPage() {
  const [items, setItems] = useState<Mailbox[]>([]);

  useEffect(() => {
    fetch(`${MAIL_BASE}/api/v1/mailboxes`)
      .then((r) => r.json())
      .then(setItems)
      .catch(() => setItems([]));
  }, []);

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold mb-4">Posta kutuları</h1>
      <p className="text-slate-600 mb-6 text-sm">
        Yönlendirme adresi doğrulama ve IMAP son çekim durumu.
      </p>
      <table className="w-full text-sm bg-white border rounded-lg">
        <thead className="bg-slate-100 text-left">
          <tr>
            <th className="p-3">E-posta</th>
            <th className="p-3">IMAP</th>
            <th className="p-3">Doğrulama</th>
            <th className="p-3">Son çekim</th>
          </tr>
        </thead>
        <tbody>
          {items.map((m) => (
            <tr key={m.id} className="border-t">
              <td className="p-3">{m.email}</td>
              <td className="p-3">
                {m.imap_host}
              </td>
              <td className="p-3">{m.verification_status}</td>
              <td className="p-3">
                {m.last_poll_at ? new Date(m.last_poll_at).toLocaleString("tr-TR") : "—"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </AppShell>
  );
}
