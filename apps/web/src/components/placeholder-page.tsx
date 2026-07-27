import { AppShell } from "@/components/app-shell";

export function PlaceholderPage({ title }: { title: string }) {
  return (
    <AppShell>
      <h1 className="text-2xl font-semibold">{title}</h1>
      <p className="text-slate-600 mt-2">Bu ekran API entegrasyonu ile bağlanır.</p>
    </AppShell>
  );
}
