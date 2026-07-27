import Link from "next/link";

const nav = [
  { href: "/dashboard", label: "Panel" },
  { href: "/rfqs", label: "RFQ" },
  { href: "/quotes", label: "Teklifler" },
  { href: "/products", label: "Ürünler" },
  { href: "/customers", label: "Müşteriler" },
  { href: "/approvals", label: "Onaylar" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen">
      <aside className="w-56 bg-slate-900 text-slate-100 p-4 flex flex-col gap-2">
        <Link href="/dashboard" className="font-bold text-lg mb-4">
          TeklifOS
        </Link>
        {nav.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className="rounded px-2 py-1.5 text-sm hover:bg-slate-800"
          >
            {item.label}
          </Link>
        ))}
        <Link
          href="/login"
          className="mt-auto text-sm text-slate-400 hover:text-white"
        >
          Çıkış
        </Link>
      </aside>
      <main className="flex-1 p-6">{children}</main>
    </div>
  );
}
