package tr.edu.klu.hastane.arayuz;

import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.AmeliyatDurumu;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;
import tr.edu.klu.hastane.model.TriajSeviyesi;
import tr.edu.klu.hastane.oncelik.KorunanGrupOncelikDekoratoru;
import tr.edu.klu.hastane.oncelik.StandartOncelikStratejisi;
import tr.edu.klu.hastane.servis.CizelgeSonucu;
import tr.edu.klu.hastane.servis.HastaneYonetimi;
import tr.edu.klu.hastane.servis.OrnekVeriYukleyici;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Kullanıcı ile konuşan konsol menüsü. Yalnızca girdi okur ve çıktı yazar;
 * iş kurallarını bilmez, hepsini HastaneYonetimi'ne devreder (Single Responsibility).
 */
public class KonsolArayuzu {

    private static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final HastaneYonetimi yonetim;
    private final Scanner girdi;

    public KonsolArayuzu(HastaneYonetimi yonetim, Scanner girdi) {
        this.yonetim = yonetim;
        this.girdi = girdi;
    }

    public void baslat() {
        System.out.println("==============================================");
        System.out.println("      HASTANE PLANLAMA SİSTEMİ  v1.0");
        System.out.println("==============================================");
        boolean devam = true;
        while (devam) {
            menuyuYazdir();
            String secim = satirOku("Seçiminiz: ");
            try {
                devam = secimiIsle(secim);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("!! Hata: " + e.getMessage());
            }
        }
        System.out.println("Veriler kaydedildi. Güle güle!");
    }

    private void menuyuYazdir() {
        System.out.println();
        System.out.println("---------------- ANA MENÜ ----------------");
        System.out.println("Öncelik stratejisi: " + yonetim.getStrateji().ad());
        System.out.println(" 1) Hasta kaydet");
        System.out.println(" 2) Muayene öncelik sırasını göster");
        System.out.println(" 3) Sıradaki hastayı muayeneye çağır");
        System.out.println(" 4) Doktor ekle");
        System.out.println(" 5) Ameliyathane ekle");
        System.out.println(" 6) Ameliyat talebi oluştur");
        System.out.println(" 7) Ameliyat çizelgesini oluştur (planla)");
        System.out.println(" 8) Ameliyat çizelgesini göster");
        System.out.println(" 9) Ameliyathane arıza / bakım durumu değiştir");
        System.out.println("10) Öncelik stratejisini değiştir");
        System.out.println("11) Tüm kayıtları listele");
        System.out.println("12) Örnek veri yükle");
        System.out.println(" 0) Çıkış");
    }

    private boolean secimiIsle(String secim) {
        switch (secim) {
            case "1": hastaKaydet(); break;
            case "2": muayeneSirasiniGoster(); break;
            case "3": siradakiniCagir(); break;
            case "4": doktorEkle(); break;
            case "5": ameliyathaneEkle(); break;
            case "6": ameliyatTalebi(); break;
            case "7": planla(); break;
            case "8": cizelgeyiGoster(); break;
            case "9": ameliyathaneDurumu(); break;
            case "10": stratejiDegistir(); break;
            case "11": tumunuListele(); break;
            case "12": ornekVeri(); break;
            case "0": return false;
            default: System.out.println("Geçersiz seçim.");
        }
        return true;
    }

    // ------------------------------------------------------------------

    private void hastaKaydet() {
        int tur = sayiOku("Hasta türü (1 = Acil, 2 = Poliklinik): ", 1, 2);
        String ad = satirOku("Ad: ");
        String soyad = satirOku("Soyad: ");
        int yas = sayiOku("Yaş: ", 0, 130);
        String sikayet = satirOku("Şikâyet: ");
        Hasta hasta;
        if (tur == 1) {
            TriajSeviyesi[] seviyeler = TriajSeviyesi.values();
            for (int i = 0; i < seviyeler.length; i++) {
                System.out.println("  " + (i + 1) + ") " + seviyeler[i] + " - " + seviyeler[i].getAciklama());
            }
            TriajSeviyesi triaj = seviyeler[sayiOku("Triaj: ", 1, seviyeler.length) - 1];
            hasta = yonetim.hastaKaydet(HastaTuru.ACIL, ad, soyad, yas, sikayet, triaj.name());
        } else {
            Brans brans = bransSec();
            boolean randevulu = satirOku("Randevulu mu? (e/h): ").equalsIgnoreCase("e");
            hasta = yonetim.hastaKaydet(HastaTuru.POLIKLINIK, ad, soyad, yas, sikayet,
                    brans.name() + (randevulu ? ":R" : ""));
        }
        System.out.println("Kaydedildi -> " + hasta);
    }

    private void muayeneSirasiniGoster() {
        List<Hasta> sira = yonetim.muayeneSirasi();
        if (sira.isEmpty()) {
            System.out.println("Muayene bekleyen hasta yok.");
            return;
        }
        System.out.printf("%-4s %-6s %-20s %-13s %-9s %-6s %s%n", "Sıra", "ID", "Ad Soyad", "Tür", "Bekleme", "Puan", "Şikâyet");
        int no = 1;
        for (Hasta h : sira) {
            long bekleme = Duration.between(h.getKayitZamani(), yonetim.simdi()).toMinutes();
            String tur = h instanceof AcilHasta ? "Acil-" + ((AcilHasta) h).getTriaj() : h.turAdi();
            System.out.printf("%-4d %-6s %-20s %-13s %-9s %-6d %s%n", no++, h.getId(), h.tamAd(), tur,
                    bekleme + " dk", yonetim.oncelikPuani(h), h.getSikayet());
        }
    }

    private void siradakiniCagir() {
        Optional<Hasta> hasta = yonetim.siradakiHastayiCagir();
        if (hasta.isPresent()) {
            System.out.println("Muayeneye çağrılan hasta: " + hasta.get());
        } else {
            System.out.println("Muayene bekleyen hasta yok.");
        }
    }

    private void doktorEkle() {
        String ad = satirOku("Ad: ");
        String soyad = satirOku("Soyad: ");
        Brans brans = bransSec();
        boolean cerrah = satirOku("Cerrah mı? (e/h): ").equalsIgnoreCase("e");
        System.out.println("Eklendi -> " + yonetim.doktorEkle(ad, soyad, brans, cerrah));
    }

    private void ameliyathaneEkle() {
        String kod = satirOku("Ameliyathane kodu (ör. AH-3): ");
        System.out.println("Eklendi -> " + yonetim.ameliyathaneEkle(kod));
    }

    private void ameliyatTalebi() {
        String hastaId = satirOku("Hasta ID (ör. H001): ");
        Brans brans = bransSec();
        int sure = sayiOku("Tahmini süre (dakika): ", 1, 720);
        Ameliyat a = yonetim.ameliyatTalebiOlustur(hastaId, brans, sure);
        System.out.println("Ameliyat talebi oluşturuldu: " + a.getId());
    }

    private void planla() {
        LocalDate tarih = tarihOku();
        CizelgeSonucu sonuc = yonetim.ameliyatlariPlanla(tarih);
        System.out.println(TARIH.format(tarih) + " için çizelge oluşturuldu. Planlanan: "
                + sonuc.getPlanlananlar().size() + ", ertelenen: " + sonuc.getErtelenenler().size());
        cizelgeyiGoster();
    }

    private void cizelgeyiGoster() {
        List<Ameliyat> ameliyatlar = yonetim.getAmeliyatlar();
        if (ameliyatlar.isEmpty()) {
            System.out.println("Ameliyat talebi yok.");
            return;
        }
        System.out.printf("%-5s %-6s %-12s %-20s %-15s %-14s %-7s %s%n",
                "ID", "Hasta", "Saat", "Hasta Adı", "Branş", "Doktor", "Salon", "Durum");
        ameliyatlar.stream()
                .sorted((a, b) -> {
                    if (a.getBaslangic() == null) return b.getBaslangic() == null ? a.getId().compareTo(b.getId()) : 1;
                    if (b.getBaslangic() == null) return -1;
                    return a.getBaslangic().compareTo(b.getBaslangic());
                })
                .forEach(a -> {
                    String saat = a.getBaslangic() == null ? "-"
                            : SAAT.format(a.getBaslangic()) + "-" + SAAT.format(a.getBaslangic().plusMinutes(a.getSureDakika()));
                    String hastaAdi = yonetim.hastaBul(a.getHastaId()).map(Hasta::tamAd).orElse("?");
                    String doktor = a.getDoktorId() == null ? "-"
                            : yonetim.doktorBul(a.getDoktorId()).map(d -> "Dr. " + d.getSoyad()).orElse("?");
                    System.out.printf("%-5s %-6s %-12s %-20s %-15s %-14s %-7s %s%n", a.getId(), a.getHastaId(), saat,
                            hastaAdi, a.getBrans().getGorunenAd(), doktor,
                            a.getAmeliyathaneKodu() == null ? "-" : a.getAmeliyathaneKodu(),
                            a.getDurum() == AmeliyatDurumu.ERTELENDI ? "ERTELENDİ" : a.getDurum().toString());
                });
    }

    private void ameliyathaneDurumu() {
        yonetim.getAmeliyathaneler().forEach(s -> System.out.println("  " + s));
        String kod = satirOku("Ameliyathane kodu: ");
        int durum = sayiOku("1 = Arızalı / bakıma al, 2 = Tekrar aktif et: ", 1, 2);
        Optional<CizelgeSonucu> sonuc = yonetim.ameliyathaneDurumuDegistir(kod, durum == 2);
        System.out.println("Durum güncellendi.");
        if (sonuc.isPresent()) {
            System.out.println("Mevcut plan otomatik olarak YENİDEN hesaplandı. Planlanan: "
                    + sonuc.get().getPlanlananlar().size() + ", ertelenen: " + sonuc.get().getErtelenenler().size());
            cizelgeyiGoster();
        }
    }

    private void stratejiDegistir() {
        System.out.println("  1) Standart (triaj + bekleme süresi)");
        System.out.println("  2) Standart + korunan grup (65 yaş üstü / 7 yaş altı) bonusu");
        int secim = sayiOku("Strateji: ", 1, 2);
        if (secim == 1) {
            yonetim.stratejiDegistir(new StandartOncelikStratejisi());
        } else {
            yonetim.stratejiDegistir(new KorunanGrupOncelikDekoratoru(new StandartOncelikStratejisi()));
        }
        System.out.println("Yeni strateji: " + yonetim.getStrateji().ad());
    }

    private void tumunuListele() {
        System.out.println("--- Doktorlar ---");
        yonetim.getDoktorlar().forEach(d -> System.out.println("  " + d));
        System.out.println("--- Ameliyathaneler ---");
        yonetim.getAmeliyathaneler().forEach(s -> System.out.println("  " + s));
        System.out.println("--- Hastalar ---");
        yonetim.getHastalar().forEach(h -> System.out.println("  " + h + " -> " + h.getDurum()));
    }

    private void ornekVeri() {
        new OrnekVeriYukleyici(yonetim).yukle();
        System.out.println("Örnek veri yüklendi: " + yonetim.getDoktorlar().size() + " doktor, "
                + yonetim.getAmeliyathaneler().size() + " ameliyathane, " + yonetim.getHastalar().size()
                + " hasta, " + yonetim.getAmeliyatlar().size() + " ameliyat talebi.");
    }

    // ------------------------- yardımcı girdi metotları -------------------------

    private Brans bransSec() {
        Brans[] branslar = Brans.values();
        for (int i = 0; i < branslar.length; i++) {
            System.out.println("  " + (i + 1) + ") " + branslar[i].getGorunenAd());
        }
        return branslar[sayiOku("Branş: ", 1, branslar.length) - 1];
    }

    private LocalDate tarihOku() {
        while (true) {
            String metin = satirOku("Plan tarihi (gg.aa.yyyy, boş bırakılırsa bugün): ");
            if (metin.isEmpty()) {
                return yonetim.simdi().toLocalDate();
            }
            try {
                return LocalDate.parse(metin, TARIH);
            } catch (DateTimeParseException e) {
                System.out.println("Tarih biçimi hatalı, örnek: 24.10.2026");
            }
        }
    }

    private String satirOku(String mesaj) {
        System.out.print(mesaj);
        if (!girdi.hasNextLine()) {
            return "0";
        }
        return girdi.nextLine().trim();
    }

    private int sayiOku(String mesaj, int min, int maks) {
        while (true) {
            String metin = satirOku(mesaj);
            try {
                int deger = Integer.parseInt(metin);
                if (deger >= min && deger <= maks) {
                    return deger;
                }
            } catch (NumberFormatException ignored) {
                // aşağıdaki uyarı yazdırılır
            }
            System.out.println(min + " ile " + maks + " arasında bir sayı giriniz.");
        }
    }
}
