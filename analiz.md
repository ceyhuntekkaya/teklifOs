# Net değerlendirme

**TeklifOS yapılabilir ve satılabilir bir fikir; fakat yalnızca doğru daraltılırsa.**

“Yapay zekâ destekli teklif hazırlama programı” şeklinde genel bir ürün yaparsanız pazarda kaybolursunuz. Çünkü Türkiye’de CRM, ERP ve teklif modülü sunan çok sayıda ürün var. Buna karşılık şu kadar dar bir tanım daha anlamlı:

> **E-posta, PDF ve Excel ile gelen ürün taleplerini okuyup ürün kataloğuyla eşleştiren, fiyat kurallarını uygulayan ve insan onayına hazır teklif taslağı oluşturan satış operasyonu yazılımı.**

Burada hedef **üretici değil**:

* Endüstriyel ürün distribütörleri
* Elektrik ve otomasyon malzemesi satıcıları
* Hidrolik-pnömatik ürün satıcıları
* Rulman, bağlantı elemanı ve MRO tedarikçileri
* HVAC ve teknik ekipman dağıtıcıları
* Endüstriyel yedek parça satıcıları
* Çok markalı ürün satan toptancılar

Ürün; özel proje, teknik çizim, üretim süresi ve işçilik maliyeti hesaplamamalı. İlk sürümde **katalog ürünü satan firmaların taleplerini hızlandırmalı.**

OSTİM’de 6.500’den fazla işletme ve 65.000 çalışan bulunuyor. Otomotiv, makine, metal, elektrik-elektronik ve teknik ekipman gibi ürünün hedefleyebileceği sektörler bölgede güçlü şekilde temsil ediliyor. Ancak bu 6.500 firmanın tamamı müşteri değildir; benim tahminim, ilk ürün tanımına gerçekten uyabilecek kitlenin birkaç yüz firma düzeyinde olduğudur. ([Ostim Organized Industrial Zone][1])

---

# 1. TeklifOS tam olarak ne yapmalı?

Tipik senaryo şöyle:

Müşteri bir e-posta gönderiyor:

> Aşağıdaki malzemeler için fiyat ve teslim süresi rica ederiz.
> 25 adet Siemens 3RV2011-1GA10
> 40 adet kontaktör, 9A, 220V
> 100 metre 4x6 NYY kablo
> Teklifinizi yarın saat 15.00’e kadar bekliyoruz.

Ekinde de 80 satırlık Excel veya PDF bulunuyor.

TeklifOS şu işlemleri yapar:

1. E-postayı ve eklerini alır.
2. Müşteriyi tanır.
3. Talep numarası, son tarih ve teslimat şartlarını çıkarır.
4. Satırları ürün, miktar ve birim bilgisine dönüştürür.
5. Firma kataloğundaki ürünlerle eşleştirir.
6. Müşteriye özel fiyat listesini bulur.
7. İskonto ve marj kurallarını uygular.
8. Eşleşmeyen veya riskli satırları işaretler.
9. Satış personeline kontrol ekranı sunar.
10. Onay sonrası şirket şablonunda PDF teklif oluşturur.
11. E-postayı gönderir ve takip tarihini açar.
12. Teklifin kazanıldı veya kaybedildi bilgisini kaydeder.

Ana değer önermesi:

> “Teklif personelinin yerini almak” değil, “teklif hazırlarken yaptığı arama, kopyalama, kontrol ve biçimlendirme işlerini azaltmak.”

---

# 2. Ürünün ideal müşterisi

TeklifOS her şirkete uygun değildir.

## Güçlü müşteri profili

Aşağıdaki koşullardan çoğu varsa ürün anlamlı olur:

* Ayda en az **150–200 teklif talebi** alıyor.
* Teklif hazırlama işinde en az iki kişi görev alıyor.
* Taleplerin çoğu e-posta, PDF veya Excel üzerinden geliyor.
* 1.000’den fazla ürün veya ürün varyasyonu var.
* Aynı ürünler farklı isimlerle isteniyor.
* Müşteriye özel fiyat ve iskonto uygulanıyor.
* Teklif başına en az 10–15 dakika harcanıyor.
* Teklif takibi Excel veya e-posta üzerinden yapılıyor.
* Fiyat hatası veya unutulan teklif gerçek para kaybettiriyor.

## Uygun olmayan müşteri

* Ayda 20–30 teklif hazırlayan küçük işletme
* Her teklifin baştan teknik mühendislik gerektirdiği şirket
* Tamamen özel üretim yapan firma
* Ürün kodu ve fiyat listesi bulunmayan işletme
* Teklifleri zaten ERP içinde tamamen otomatik hazırlanan şirket
* Teklif başına 2–3 dakikadan az zaman harcayan satıcı
* Satış sürecini standardize etmek istemeyen firma

Bu ayrım kritik. Aksi durumda çok sayıda görüşme yapar, herkes ürünü beğenir fakat kimse ödeme yapmaz.

---

# 3. Teknik mimari

İlk sürüm için mikroservis ordusuna veya Kubernetes’e ihtiyaç yok. **Modüler monolit + asenkron worker mimarisi** yeterlidir.

```text
E-posta / PDF / Excel / Manuel Yükleme
                  ↓
           Talep Kabul Katmanı
                  ↓
       Belge Ayrıştırma ve Güvenlik
                  ↓
        Yapılandırılmış Veri Çıkarma
                  ↓
       Ürün Normalizasyonu ve Eşleştirme
                  ↓
       Fiyatlandırma ve Kural Motoru
                  ↓
          İnsan Kontrol Ekranı
                  ↓
        Onay → PDF → E-posta → Takip
```

## Önerilen teknoloji yığını

Mevcut altyapınız düşünülürse:

* **Backend:** Spring Boot 3 / Java 17
* **Frontend:** Angular
* **Veritabanı:** PostgreSQL
* **Benzerlik araması:** PostgreSQL `pg_trgm` ve gerektiğinde `pgvector`
* **Kuyruk:** RabbitMQ
* **Cache:** Redis
* **Dosya saklama:** S3 uyumlu nesne deposu
* **PDF oluşturma:** HTML/CSS + headless Chromium/Playwright
* **Gözlemleme:** OpenTelemetry, Prometheus/Grafana ve Sentry
* **LLM katmanı:** Model sağlayıcıdan bağımsız bir gateway
* **OCR:** Önce yerel veya açık kaynak; başarısız sayfalarda bulut servisi
* **Kimlik doğrulama:** Mevcut Genixo kullanıcı ve tenant yapısı yeniden kullanılabilir

Bu aşamada Spring Boot ve .NET’i aynı üründe karıştırmak gereksiz operasyon yükü yaratır. Ekibin fiilen daha hızlı geliştirdiği tek bir backend yığını seçilmeli.

---

# 4. E-posta alımı

En zor görünen ama yönetilebilir bölümlerden biridir.

## İlk sürüm

Her müşteri için bir yönlendirme adresi oluşturulur:

```text
firmaadi@teklifos.com
```

Müşteri kendi teklif adresine gelen e-postaları bu adrese yönlendirir.

Avantajları:

* Gmail ve Microsoft OAuth süreçleri beklenmez.
* Pilot daha hızlı başlar.
* Kurumsal BT izni daha kolay alınabilir.
* Her müşteriye özel gelen kutusu mantığı oluşturulur.

## İkinci aşama

* Microsoft Graph ile Outlook/Exchange bağlantısı
* Gmail API bağlantısı
* Paylaşımlı gelen kutuları
* Belirli klasör ve etiketleri izleme
* Yanıtların mevcut RFQ kaydıyla eşleştirilmesi

Gmail tarafında bazı posta erişim kapsamları “restricted scope” kabul edildiğinden OAuth uygulama doğrulaması ve güvenlik incelemesi gündeme gelebilir. Bu nedenle native Gmail entegrasyonunu ilk pilotun ön koşulu yapmak süreyi riske atar. Microsoft Graph tarafında webhook/change notification yaklaşımı kullanılabilir. ([Google for Developers][2])

---

# 5. Belge işleme katmanı

Sistem şu formatları desteklemeli:

* Metin içeren PDF
* Taranmış PDF
* XLS/XLSX
* DOCX
* E-posta gövdesi
* JPG/PNG
* İlerleyen aşamada ZIP

İşleme sırası:

1. Dosya türünü doğrulama
2. Zararlı yazılım taraması
3. Dosya hash’iyle tekrar kontrolü
4. Metin tabakası varsa doğrudan çıkarma
5. Excel hücrelerinin ve tabloların okunması
6. Metin yoksa OCR
7. Sayfa ve hücre koordinatlarının saklanması
8. Orijinal dosyanın değiştirilemez biçimde korunması

Google Document AI’ın yayımlanan fiyatlarında temel OCR 1.000 sayfa başına 1,50 dolar, layout parser 10 dolar ve bazı özel çıkarım modelleri 30 dolar düzeyindedir. Bu nedenle her belgeyi pahalı bir doküman AI servisine göndermek yerine önce standart ayrıştırma yapılmalıdır. ([Google Cloud][3])

---

# 6. Yapay zekâ ne yapmalı, ne yapmamalı?

## Yapay zekânın yapacağı işler

* Talep numarasını bulma
* Son teklif tarihini çıkarma
* Ürün satırlarını ayırma
* Marka ve model numarasını belirleme
* Miktar ve birimi çıkarma
* Teslimat ve ödeme şartlarını bulma
* Ürün açıklamasını normalize etme
* Birden fazla ürün adayı arasından sıralama yapma
* Müşteriye gönderilecek e-posta taslağını oluşturma
* Geçmiş tekliflerden ilgili kayıtları bulma

## Yapay zekânın yapmaması gerekenler

* Ürün kodu uydurmak
* Fiyat belirlemek
* Döviz kuru seçmek
* İskonto yetkisi vermek
* Marj sınırını değiştirmek
* Eşleşmeyen ürünü kesin eşleşmiş saymak
* İnsan kontrolü olmadan teklif göndermek
* ERP’ye kontrolsüz sipariş yazmak

**Fiyatlandırma bir LLM işi değildir.** Kural motoru işi olmalıdır.

---

# 7. Ürün eşleştirme motoru

Ürünün teknik ve ticari değerinin önemli bölümü burada oluşur.

Müşteri şöyle yazabilir:

* `3RV2011 1GA10`
* `3RV2011-1GA10`
* `Siemens motor koruma 4,5-6,3A`
* `Bizim kod: ELK-271`
* `Geçen sefer aldığımız 6,3 amper Siemens`

Bunların aynı ürüne bağlanması gerekir.

## Eşleştirme sırası

### 1. Kesin eşleşme

* Şirket ürün kodu
* Üretici parça numarası
* EAN/barkod
* Müşterinin kendi ürün kodu

### 2. Normalizasyon

* Boşluk ve tireleri kaldırma
* Büyük-küçük harf standardı
* Türkçe karakter normalizasyonu
* Ölçü birimi standardı
* Marka takma adları

### 3. Müşteri özelindeki alias tablosu

Örneğin:

```text
Müşteri A'nın "POMPA-25" kodu
→ Şirketin "HYD-000873" koduna karşılık gelir.
```

Satış personeli bir eşleşmeyi düzelttiğinde sistem bunu öğrenme kaydı olarak saklar.

### 4. Fuzzy arama

* PostgreSQL trigram benzerliği
* Ürün adı ve açıklama benzerliği
* Marka, ölçü ve teknik özellik filtreleri

### 5. Embedding araması

Semantik olarak benzeyen ürünler bulunur.

### 6. LLM ile aday sıralama

Model sadece önceden belirlenmiş beş-on aday arasından sıralama yapar. Katalog dışında ürün üretemez.

## Güven eşikleri

Örnek başlangıç politikası:

* Kesin kod veya doğrulanmış alias: otomatik eşleştirme
* Yüksek ama kesin olmayan güven: kullanıcı onayı
* Düşük güven: eşleşmemiş ürün
* Birbiriyle çok yakın iki aday: zorunlu inceleme

Eşikler teorik olarak değil, gerçek müşteri verisiyle kalibre edilmelidir.

---

# 8. Fiyatlandırma motoru

Her ürün için yalnızca “liste fiyatı” yeterli olmayacaktır.

Sistem şu öncelik sırasını desteklemeli:

1. Müşteri sözleşme fiyatı
2. Müşteriye özel fiyat listesi
3. Kampanya fiyatı
4. Miktar kademesi
5. Genel bayi fiyatı
6. Standart liste fiyatı

Ardından:

* Para birimi
* Kur tarihi ve kur kaynağı
* Miktar iskontosu
* Müşteri grubu iskontosu
* Satıcı yetkisi
* Minimum brüt marj
* MOQ
* Paket adedi
* Nakliye
* KDV
* Fiyat geçerlilik tarihi
* Teslim süresi
* Stok bilgisi

kontrol edilir.

Önemli teknik ayrıntı: Teklif oluşturulduğu anda kullanılan fiyat ve kur **snapshot olarak saklanmalı**. Ana fiyat listesi daha sonra değişse bile eski teklif değişmemelidir.

Örnek:

```text
Liste fiyatı:              10.000 TL
Müşteri grup indirimi:         %8
Satış personeli indirimi:       %3
Minimum marj:                  %18
Yetki sınırı:                  %10
Sonuç: Yönetici onayı gerekli
```

---

# 9. Temel veri modeli

Asgari tablolar:

* `tenant`
* `user`
* `role`
* `mailbox`
* `customer`
* `customer_contact`
* `product`
* `manufacturer`
* `product_alias`
* `customer_product_alias`
* `unit_conversion`
* `price_list`
* `price_list_item`
* `pricing_rule`
* `rfq`
* `rfq_document`
* `rfq_line`
* `product_match_candidate`
* `quote`
* `quote_version`
* `quote_line`
* `approval_request`
* `outbound_message`
* `follow_up`
* `audit_event`

Her önemli işlem için audit kaydı tutulmalı:

* Belge ne zaman geldi?
* Hangi model işlendi?
* Model hangi sürüm prompt’u kullandı?
* İlk önerilen ürün neydi?
* Kullanıcı neyi değiştirdi?
* Hangi fiyat kuralı uygulandı?
* Kim onayladı?
* Teklif hangi sürümle gönderildi?

Bu hem hata analizi hem de kurumsal güven açısından gereklidir.

---

# 10. Güvenlik

Teklif dosyaları çoğu zaman şunları içerir:

* Müşteri isimleri
* İletişim bilgileri
* Alım miktarları
* Özel fiyatlar
* İskonto oranları
* Kâr marjları
* Ticari koşullar

Dolayısıyla güvenlik “sonra eklenir” konusu değildir.

Asgari önlemler:

* Tenant izolasyonu
* Rol bazlı erişim
* Dosya şifreleme
* Aktarım sırasında TLS
* Audit log
* Yedekleme ve geri yükleme testi
* Dosya zararlı yazılım taraması
* Verilerin saklama ve silme politikası
* LLM sağlayıcısına gönderilen verilerin kontrolü
* Model girdisinde kişisel verinin asgariye indirilmesi
* Hassas fiyat alanlarının loglarda maskelenmesi
* Prompt injection’a karşı belge metninin “talimat” değil “veri” olarak işlenmesi
* Excel formüllerinin çalıştırılmaması
* Belgelerin hiçbir zaman otomatik araç veya kod çalıştırmasına izin verilmemesi

OpenAI’ın kurumsal API dokümantasyonuna göre API üzerinden gönderilen iş verileri varsayılan olarak model eğitimi için kullanılmıyor. Bununla birlikte Türkiye dışına veri aktarımı, alt işleyenler ve saklama süreleri KVKK kapsamında ayrıca sözleşme ve aktarım mekanizmalarıyla ele alınmalıdır. ([OpenAI Help Center][4])

Yerel Qwen modeliniz güvenlik açısından satış argümanı olabilir. Ancak her müşteriye on-premise model kurmak, bakım ve destek maliyetini ciddi şekilde artırır. İlk aşamada:

* Standart paket: yönetilen bulut
* Büyük kurumsal paket: özel sunucu veya on-premise

daha gerçekçi olur.

---

# 11. İlk MVP’de neler bulunmalı?

## 8–12 haftalık ücretli MVP

* Tenant ve kullanıcı yönetimi
* E-posta yönlendirme adresi
* Manuel PDF/Excel yükleme
* PDF ve Excel veri çıkarma
* Ürün kataloğu CSV/Excel aktarımı
* Fiyat listesi aktarımı
* Kesin ve fuzzy ürün eşleştirme
* Müşteri ürün kodu eşleştirmesi
* Kural tabanlı fiyatlandırma
* Satır bazında insan kontrolü
* Tek teklif şablonu
* Yönetici onayı
* PDF oluşturma
* E-posta gönderme
* Teklif durumu ve takip tarihi
* Basit audit kaydı

## İlk MVP’ye alınmaması gerekenler

* Tam CRM
* Mobil uygulama
* WhatsApp entegrasyonu
* Beş farklı ERP entegrasyonu
* Otomatik sipariş oluşturma
* Stok yönetimi
* Satın alma modülü
* Üretim maliyetlendirmesi
* Teknik çizim analizi
* Teklif karşılaştırma portalı
* Müşteri portalı
* İnsan kontrolü olmadan otomatik gönderim
* Gelişmiş satış tahmini

Bunlar eklenirse ürün üç ayda çıkmaz.

---

# 12. Pazarın mevcut durumu

## Türkiye’deki yatay ürünler

Türkiye’de teklif hazırlama tek başına boş bir alan değil.

* **DİA:** Teklif oluşturma, PDF/e-posta gönderme ve onay süreçleri sunuyor.
* **Planports:** Ürün seçimi, otomatik hesaplama, e-posta/WhatsApp gönderimi, görüntülenme takibi ve siparişe dönüştürme sağlıyor.
* **Logo CRM:** Teklif yönetimi, onay ve ERP entegrasyonu sunuyor.
* **Paraşüt:** Daha temel seviyede teklif hazırlama fonksiyonu sağlıyor.
* Ayrıca Portakal Yazılım ve benzeri yerel firmalar özel teklif otomasyonları geliştiriyor. ([DİA Yazılım][5])

Bunların önemli avantajları:

* Türkiye’de bilinirlik
* Mevzuat ve muhasebe uyumu
* Kurulu müşteri tabanı
* ERP verisine erişim
* Bayi ve entegratör ağı

TeklifOS’un bunlarla “bizde de teklif ekranı var” diyerek rekabet etmesi mümkün değil.

Fark şu olmalı:

> “Kullanıcının teklif ekranına ürünleri tek tek girmesinden önceki işi otomatikleştiriyoruz.”

Yani rakip teklif modülü, TeklifOS ise **gelen talebi teklif verisine dönüştüren katman** olmalı.

---

## Doğrudan yabancı rakipler

### Aginera

TeklifOS’a en yakın örneklerden biri. RFQ ve sipariş e-postalarını, PDF ve Excel eklerini okuyup ürünlerle eşleştiriyor; fiyat ve indirim kuralları uyguluyor ve teklif oluşturuyor. Professional planı yıllık ödeme koşuluyla kullanıcı başına aylık 299 dolar olarak listeleniyor. ([aginera.ai][6])

Bu ürünün varlığı iki şeyi gösteriyor:

1. Problem gerçek.
2. Fikir özgün değil.

Beş kullanıcılı bir müşteri için liste fiyatı aylık yaklaşık 1.495 dolar olur. Bu, Türkiye’de çoğu KOBİ için yüksek; fakat yerel ürünün fiyat avantajına sahip olabileceğini gösteriyor.

### Hexa

Endüstriyel distribütör ve üreticilerin gelen e-posta siparişlerini işliyor; satırları çıkarıyor, ürün kodlarına bağlıyor ve orijinal teklifle siparişi karşılaştırıyor. Şirket OpenAI Startup Fund ve Y Combinator desteğini vurguluyor. ([Hexa][7])

### Graip

E-posta, PDF ve Excel’den RFQ verisi çıkarma, malzeme ana verisiyle eşleştirme, ERP’ye gönderme ve audit trail özellikleri sunuyor. ([graip.ai][8])

### Kavida

RFQ işleme, teklif ve sipariş operasyonlarında AI ajanları geliştiriyor. Şirketin 2025’te QAD/Redzone tarafından satın alınması, büyük ERP üreticilerinin bu alanı stratejik gördüğüne işaret ediyor. Bu, bağımsız ürünler açısından hem pazar doğrulaması hem de ileride ERP üreticilerinden gelecek rekabet riskidir. ([Kavida][9])

---

## Büyük CPQ ürünleri

* **Salesforce Revenue Cloud**
* **DealHub**
* **Tacton**
* **Oracle CPQ**
* **SAP CPQ**
* **Paperless Parts**

Salesforce Revenue Cloud’un yayımlanan fiyatlarında Growth paketi kullanıcı başına aylık 150 dolar, Advanced paketi 200 dolar düzeyinde. Bu ürünler teklif yapılandırma, sözleşme, sipariş ve abonelik yönetimine kadar uzanıyor. ([Salesforce][10])

DealHub’ın 2026 başında 100 milyon dolar yatırım açıklaması ve Kavida’nın satın alınması, teklif ve gelir operasyonları pazarında yatırım ilgisinin sürdüğünü gösteriyor. Ancak bu şirketlerin kârlılığına veya gerçek gelirlerine ilişkin güvenilir kamu verisi yok. “Yatırım aldılar” ile “kârlı iş yapıyorlar” aynı anlama gelmez. ([DealHub][11])

Paperless Parts, CAD ve üretim maliyetlendirmesine daha yakın bir ürün. 30 milyon dolarlık yatırım açıklamış durumda; ancak sizin “üretim değil” tanımınızdan dolayı doğrudan hedef alınacak örnek değil. ([Business Wire][12])

---

## Türkiye kaynaklı yakın oyuncu: Eluvium

Eluvium daha çok satın alma tarafında çalışıyor:

* Tedarikçi bulma
* RFQ oluşturma
* Teklif toplama
* Teklifleri değerlendirme
* Satın alma operasyonu

Yani TeklifOS satıcının gelen taleplerini işlerken Eluvium alıcının tedarik süreçlerini yönetiyor. Bugün doğrudan aynı ürün değil, fakat altyapı ve yetenek açısından yakın bir oyuncu. Şirket kamuya açık açıklamalarda 700 bin dolar yatırım ve Anadolu Efes ile pilot çalışma duyurdu. ([Eluvium][13])

---

# 13. Pazardaki gerçek boşluk

Pazarın boşluğu “teklif hazırlama” değil.

Daha dar boşluk şudur:

> **Türkçe e-posta, PDF ve Excel ile çalışan; Logo, DİA, Mikro ve Netsis gibi yerel sistemlerle bağlantı kuran; endüstriyel distribütörlere yönelik RFQ giriş otomasyonu.**

Başlangıç avantajlarınız:

* OSTİM’e fiziksel erişim
* BNI ağı
* Yerinde süreç analizi yapabilme
* Yerel destek
* Türkçe belge ve ürün adlarına uyarlama
* Yerel ERP entegrasyonu
* Özel sunucu seçeneği
* Müşteriyi tanıyan satış imkânı

Ancak bunlar kalıcı teknolojik savunma değildir. Aginera veya başka bir ürün Türkçe destek ve yerel entegratör edinirse avantajınız azalır.

Kalıcılaştırılabilecek varlıklar:

* Müşteri ürün kodu eşleştirme verisi
* Sektöre özel ürün normalizasyon sözlüğü
* Logo/DİA/Mikro/Netsis adaptörleri
* Sektöre özel fiyatlandırma şablonları
* Kullanıcı düzeltmelerinden oluşan alias ağı
* Yerel satış ve entegratör kanalı

---

# 14. Müşteri başına teknik maliyet

Aşağıdaki hesap piyasa verisi değil; planlama senaryosudur.

## Örnek küçük müşteri

* 5 kullanıcı
* Ayda 300 RFQ
* RFQ başına ortalama 3 sayfa
* Toplam 900 sayfa
* RFQ başına yaklaşık 8.000 input ve 1.000 output token
* Her RFQ için tek ana model çağrısı

OpenAI’ın güncel yayımlanan fiyatlarında GPT-5.4 mini için input 1 milyon token başına 0,375 dolar, output 2,25 dolar seviyesinde. Bu varsayımla 300 RFQ’nun teorik temel LLM maliyeti yaklaşık **1,58 dolar**, yani yuvarlak kurla yaklaşık **74 TL** olur. Yeniden denemeler, embedding, ikinci model kontrolü ve test trafiği eklense bile model maliyeti genellikle birkaç yüz TL düzeyinde kalır. ([OpenAI Developers][14])

900 sayfanın tamamında bulut OCR kullanılsa temel OCR maliyeti yaklaşık **1,35 dolar**, yani yaklaşık **64 TL** olur. Daha gelişmiş layout parser kullanılırsa maliyet yükselir. ([Google Cloud][3])

## Aylık gerçek maliyet tahmini

| Maliyet kalemi               |   Küçük müşteri/ay |
| ---------------------------- | -----------------: |
| LLM ve embedding             |         250–750 TL |
| OCR ve belge işleme          |         100–500 TL |
| Sunucu/DB/yedek payı         |       500–1.500 TL |
| Depolama ve trafik           |          50–200 TL |
| E-posta ve izleme servisleri |         100–400 TL |
| Teknik destek, 2–5 saat      |     1.600–6.000 TL |
| **Toplam**                   | **2.600–9.350 TL** |

Gerçekte en büyük maliyet yapay zekâ değildir:

> **Destek, müşteri verisini temizleme, fiyat kuralı oluşturma ve entegrasyondur.**

Stabil müşteride 2.500–7.500 TL aylık doğrudan maliyet mümkün. Sorunlu veya çok özelleştirme isteyen müşteride maliyet 10.000 TL’nin üzerine kolayca çıkar.

---

# 15. Kurulum maliyeti

## Standart müşteri

* Katalog aktarımı
* Fiyat listesi aktarımı
* Müşteri eşleştirmeleri
* Teklif şablonu
* Kullanıcı eğitimi
* E-posta yönlendirmesi
* Temel fiyat kuralları

Tahmini iş yükü:

* 20–50 saat
* İç maliyet hesabıyla yaklaşık 20.000–75.000 TL

## Entegrasyonlu müşteri

* ERP API inceleme
* Ürün ve müşteri senkronizasyonu
* Özel fiyat kuralları
* Çoklu şirket veya depo
* Yetki matrisi
* Sipariş yazma entegrasyonu

Tahmini iş yükü:

* 60–150 saat
* Yaklaşık 75.000–225.000 TL veya daha fazla

İlk üç müşteride gerçek iş yükü bunun iki katına yaklaşabilir. Çünkü ürün geliştirirken aynı zamanda müşteri öğreniyorsunuz.

Kurulum bedeli alınmazsa ürün hızla “düşük fiyatlı özel yazılım hizmetine” dönüşür.

---

# 16. Satış fiyatı

Önceki 6–12 bin TL aylık fiyat önerisi bu ürün için düşük kalır. Entegrasyon, destek ve kritik ticari veri dikkate alındığında şu yapı daha gerçekçi:

## Ücretli pilot

* 6–8 hafta
* Tek e-posta kutusu
* Tek teklif şablonu
* Belirli ürün grubu
* ERP entegrasyonu yok
* Sınırlı tarihsel veri

**40.000–75.000 TL + KDV**

Pilot ücretsiz olmamalı. Dönüşüm gerçekleşirse bedelin bir kısmı kurulum ücretinden düşülebilir.

## Starter

* 5 kullanıcı
* Ayda 250–300 RFQ
* Bir e-posta kutusu
* Bir teklif şablonu
* Excel katalog ve fiyat listesi
* Temel onay akışı
* ERP’ye yazma yok

**Kurulum:** 60.000–90.000 TL
**Abonelik:** 12.500–17.500 TL/ay

## Professional

* 10–20 kullanıcı
* Ayda 1.000 RFQ
* Çoklu e-posta kutusu
* Çoklu fiyat listesi
* Gelişmiş yetkilendirme
* Yönetici onayı
* Bir ERP’den salt okunur entegrasyon
* Gelişmiş raporlama

**Kurulum:** 120.000–200.000 TL
**Abonelik:** 25.000–40.000 TL/ay

## Enterprise

* SSO
* SLA
* Özel sunucu veya on-premise
* ERP çift yönlü entegrasyon
* Çoklu şirket
* Özel güvenlik gereksinimleri
* Özel destek

**Kurulum:** 250.000–750.000 TL
**Abonelik:** 50.000–120.000 TL/ay

Fiyatlar döviz veya TÜFE esaslı yılda en az bir kez güncellenmelidir. Aksi durumda destek maliyetleri abonelik gelirini aşındırır.

---

# 17. Müşteri açısından ekonomik değer

Örnek:

* Ayda 300 RFQ
* Her RFQ’da 18 dakika tasarruf
* Personelin şirkete tam saat maliyeti 400 TL

```text
300 × 18 / 60 × 400 TL
= 36.000 TL aylık işçilik değeri
```

Buna şunlar dahil değil:

* Daha hızlı dönüş nedeniyle kazanılan işler
* Yanlış fiyat riskinin azalması
* Takipsiz tekliflerin azalması
* Kıdemli personel bilgisinin sisteme aktarılması
* Yeni çalışan eğitim süresinin azalması

Bu senaryoda 12.500–17.500 TL aylık bedel savunulabilir.

Fakat müşteri ayda yalnızca 50 RFQ hazırlıyorsa aynı hesap:

```text
50 × 18 / 60 × 400 TL
= 6.000 TL
```

Böyle bir müşteriye 15.000 TL abonelik satmak mantıklı değildir. Dolayısıyla satış ekibinin ilk sorusu çalışan sayısı değil, **aylık teklif sayısı ve teklif başına harcanan süre** olmalıdır.

---

# 18. Bir müşteriyi kazanma maliyeti

Bunlar ölçülmeden kesin sayı verilemez. Başlangıç planı olarak:

## Kurucu satışlı OSTİM/BNI modeli

* Görüşmeler
* Yerinde analiz
* Demo hazırlığı
* Pilot teklifi
* Teklif takibi
* Sözleşme çalışması

Nakit pazarlama harcaması düşük olsa bile zaman dahil gerçek müşteri edinme maliyeti:

**20.000–60.000 TL**

## Soğuk satış ve reklam modeli

* Liste oluşturma
* Telefon/e-posta
* Reklam
* Çok sayıda demo
* Uzun satış süreci

**40.000–100.000 TL veya daha fazla**

Buna kurulum maliyeti dahil değildir.

Örneğin:

```text
Müşteri edinme maliyeti:       40.000 TL
Kurulum iç maliyeti:           60.000 TL
Toplam başlangıç maliyeti:    100.000 TL
Alınan kurulum bedeli:         90.000 TL
Aylık abonelik:                20.000 TL
Aylık doğrudan maliyet:         6.000 TL
Aylık brüt katkı:              14.000 TL
```

Bu müşterinin başlangıç açığını yaklaşık bir ayda kapatması mümkündür.

Ama kurulum bedeli 30.000 TL’ye indirilirse yaklaşık 70.000 TL açık oluşur ve bunu kapatmak beş ay sürer. Müşteri erken ayrılırsa zarar edilir.

---

# 19. Gerçek riskler

## 1. Hizmet şirketine dönüşme riski

En büyük risk budur.

Her müşteri:

* Farklı fiyat mantığı
* Farklı Excel
* Farklı ERP
* Farklı teklif şablonu
* Farklı onay süreci
* Farklı ürün kodlama düzeni

isterse ürün değil, özel yazılım yaparsınız.

Çözüm:

* Tek dikey
* Standart veri şablonu
* Konfigürasyonla çözülebilen kurallar
* Özelleştirme için yüksek ücret
* Çekirdek ürüne girmeyen taleplere hayır

## 2. Veri kalitesi

Müşterinin ürün kartları ve fiyat listeleri bozuksa AI çözüm üretemez.

Tipik sorunlar:

* Aynı ürünün beş kodu
* Eski fiyat listeleri
* Birim bilgisi yok
* Marka alanı boş
* Müşteri kodları kayıtlı değil
* Excel’de formüller ve manuel notlar

Bazen ürün kurulumundan önce veri temizliği projesi gerekir.

## 3. ERP firmalarının alanı kapatması

Logo, DİA, Mikro veya Netsis zamanla e-postadan teklif oluşturma özellikleri geliştirebilir. Globalde ERP üreticilerinin AI teklif şirketlerini satın alması bu riskin gerçek olduğunu gösteriyor. ([constellationr.com][15])

Bu nedenle ürünün uzun vadeli stratejisi:

* ERP rakibi olmak değil
* Farklı ERP’lerin üzerinde çalışan bağımsız giriş katmanı olmak

olmalıdır.

## 4. AI hatasının ticari sonucu

Yanlış ürün, yanlış miktar veya yanlış para birimi:

* Kâr kaybı
* Müşteri kaybı
* Sözleşme uyuşmazlığı
* İtibar sorunu

doğurabilir.

İlk dönemde teklif otomatik gönderilmemeli. İnsan onayı kaldırılacaksa bu ancak uzun süreli doğruluk verisinden sonra ve düşük riskli müşterilerde yapılmalıdır.

## 5. Satış döngüsü

Ürün satış departmanının ilgisini çeker; fakat:

* Finans marj kontrolü ister.
* BT veri güvenliği ister.
* Yönetim yatırım geri dönüşü ister.
* Satış personeli işinin elinden alınacağından çekinebilir.
* ERP danışmanı entegrasyon riskini gündeme getirir.

Dolayısıyla 20 dakikalık demo ile kapanan basit SaaS olmayabilir. Satış döngüsü orta ölçekli şirkette 1–3 ayı bulabilir.

## 6. Savunma gücü başlangıçta zayıf

İlk sürümün yazılım olarak kopyalanması zor değildir. Savunma gücü koddan değil:

* Kullanıcı düzeltme verisinden
* ERP bağlantılarından
* Sektör sözlüğünden
* Müşteri süreçlerine yerleşmekten
* Yerel satış kanalından

oluşacaktır.

---

# 20. Go-to-market önerim

İlk sektör:

> **Elektrik, otomasyon, hidrolik-pnömatik veya endüstriyel yedek parça distribütörleri**

Tek birini seçin. Dördünü aynı anda hedeflemeyin.

Örneğin yalnızca:

> “Çok markalı elektrik ve otomasyon ürünü satan distribütörler”

ile başlayabilirsiniz.

Bu segmentte:

* Ürün kodları görece düzenlidir.
* Tekrarlanan ürünler vardır.
* Marka ve alternatif ürün eşleştirmesi değerlidir.
* Talepler Excel/PDF şeklinde gelir.
* Fiyat ve iskonto karmaşıktır.
* Hız satış avantajı yaratır.

## İlk satış mesajı

“Yapay zekâlı teklif sistemi” demeyin.

> “Gelen Excel ve PDF taleplerini ürün kataloğunuzla eşleştirerek satış personeline kontrol edilmiş teklif taslağı hazırlıyoruz.”

## İlk 30 günlük doğrulama

* Aynı sektörden 15 firma görüşmesi
* En az 500 geçmiş RFQ örneği
* En az iki ücretli pilot
* Pilot başına en az 40.000 TL ödeme
* Her pilot müşteride ayda en az 150–200 RFQ
* Gerçek katalog ve fiyat listesine erişim
* Teklif başına mevcut süreyi ölçme

## Pilot başarı kriterleri

* Talep giriş süresi en az %50 azalmalı.
* Ürün kodu bulunan satırlarda çok yüksek kesinlik sağlanmalı.
* İnsan düzeltme oranı birkaç haftada %15’in altına inmeli.
* Kullanıcılar ürünü haftanın çoğu günü kullanmalı.
* Pilot müşteri baştan konuşulan abonelik fiyatını kabul etmeli.
* Yönetici “güzel teknoloji” değil, “bize zaman ve para kazandırdı” demeli.

İki firma ücretli pilot vermiyorsa, ürün geliştirmeye başlanmamalı.

---

# Son kararım

| Başlık                         | Değerlendirme |
| ------------------------------ | ------------: |
| Problemin gerçekliği           |          8/10 |
| Teknik olarak 3 ayda MVP       |          7/10 |
| Ödeme isteği                   |          7/10 |
| Türkiye’de doğrudan rekabet    |          6/10 |
| Global rekabet                 |          8/10 |
| İlk aşama savunma gücü         |          3/10 |
| OSTİM ve BNI satış avantajınız |          8/10 |
| Özel yazılıma dönüşme riski    |          9/10 |
| Genel iş fikri                 |    **6,5/10** |

## Ben olsam ne yapardım?

**Ürünü doğrudan geliştirmeye başlamazdım.**

Önce:

1. Tek bir distribütör sektörü seçerdim.
2. 15 firmayla görüşürdüm.
3. 500–1.000 gerçek RFQ ve eski teklifi incelerdim.
4. Yalnızca belge çıkarma ve ürün eşleştirmeyi gösteren çalışan bir prototip yapardım.
5. İki müşteriden en az 40–75 bin TL ücretli pilot alırdım.
6. Bundan sonra 8–12 haftalık ürüne girerdim.

**İki ücretli pilot bulunursa yapılmaya değer.**

Ücretsiz pilot isteyen, ayda 50 teklif hazırlayan ve “Logo’yla da tam entegre olsun, WhatsApp da olsun, stok da yönetsin” diyen müşteriler çıkarsa bu projeye girilmemeli.

En net gerçek şu:

> TeklifOS’un teknolojisi yapılabilir. Zor olan yapay zekâ değildir. Zor olan standart müşteri bulmak, kirli ürün verisini yönetmek ve ürünü özel yazılım işine dönüştürmeden satmaktır.

[1]: https://www.ostim.org.tr/ "Ana Sayfa | Ostim Organize Sanayi Bölgesi"
[2]: https://developers.google.com/workspace/gmail/api/auth/scopes "https://developers.google.com/workspace/gmail/api/auth/scopes"
[3]: https://cloud.google.com/document-ai/pricing "https://cloud.google.com/document-ai/pricing"
[4]: https://help.openai.com/en/articles/5722486-api-data-usage-policies "https://help.openai.com/en/articles/5722486-api-data-usage-policies"
[5]: https://www.dia.com.tr/cozum/teklif-hazirlama-programi/ "https://www.dia.com.tr/cozum/teklif-hazirlama-programi/"
[6]: https://aginera.ai/solutions/automated-quoting "https://aginera.ai/solutions/automated-quoting"
[7]: https://www.hexaagents.com/sales "https://www.hexaagents.com/sales"
[8]: https://graip.ai/discrete-manufacturing "https://graip.ai/discrete-manufacturing"
[9]: https://www.kavida.ai/use-cases/ "https://www.kavida.ai/use-cases/"
[10]: https://www.salesforce.com/uk/sales/revenue-lifecycle-management/revenue-optimization-pricing/ "Revenue Cloud Pricing | Salesforce UK"
[11]: https://dealhub.io/blog/company/dealhub-io-amplifies-massive-growth-with-100m-new-funding/?utm_source=chatgpt.com "DealHub.io Amplifies Massive Growth with $100M New ..."
[12]: https://www.businesswire.com/news/home/20210913005163/en/Paperless-Parts-Announces-%2430M-Series-B-Funding-Led-by-OpenView-Partners?utm_source=chatgpt.com "Paperless Parts Announces $30M Series B Funding Led ..."
[13]: https://eluvium.ai/ "https://eluvium.ai/"
[14]: https://developers.openai.com/api/docs/pricing "
  Pricing | OpenAI API
"
[15]: https://www.constellationr.com/insights/news/qad-redzone-acquires-kavidaai-add-procurement-ai-agents "https://www.constellationr.com/insights/news/qad-redzone-acquires-kavidaai-add-procurement-ai-agents"
