import Link from "next/link";
import { AppShell } from "@/components/app-shell";

const links = [
  { href: "/dashboard", label: "Panel" },
  { href: "/rfqs", label: "Talepler (RFQ)" },
  { href: "/quotes", label: "Teklifler" },
  { href: "/products", label: "Ürünler" },
  { href: "/customers", label: "Müşteriler" },
  { href: "/imports", label: "İçe aktarma" },
  { href: "/price-lists", label: "Fiyat listeleri" },
  { href: "/pricing-rules", label: "Fiyat kuralları" },
  { href: "/approvals", label: "Onaylar" },
  { href: "/follow-ups", label: "Takipler" },
  { href: "/reports", label: "Raporlar" },
  { href: "/settings/users", label: "Kullanıcılar" },
  { href: "/settings/roles", label: "Roller" },
  { href: "/settings/audit", label: "Denetim" },
  { href: "/settings/mailboxes", label: "Posta kutuları" },
];

export default function DashboardPage() {
  return (
    <AppShell>
      <h1 className="text-2xl font-semibold mb-4">Panel</h1>
      <p className="text-slate-600 mb-6">
        RFQ işleme, eşleştirme ve teklif operasyonlarına hızlı erişim.
      </p>
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {links.map((l) => (
          <Link
            key={l.href}
            href={l.href}
            className="rounded-lg border bg-white p-4 shadow-sm hover:border-blue-500"
          >
            {l.label}
          </Link>
        ))}
      </div>
    </AppShell>
  );
}
