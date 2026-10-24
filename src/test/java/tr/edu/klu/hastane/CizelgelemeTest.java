package tr.edu.klu.hastane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.klu.hastane.depo.BellekVeriDeposu;
import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.AmeliyatDurumu;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;
import tr.edu.klu.hastane.model.TriajSeviyesi;
import tr.edu.klu.hastane.model.ZamanAraligi;
import tr.edu.klu.hastane.oncelik.StandartOncelikStratejisi;
import tr.edu.klu.hastane.servis.AmeliyatCizelgeleyici;
import tr.edu.klu.hastane.servis.CizelgeSonucu;
import tr.edu.klu.hastane.servis.HastaneYonetimi;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CizelgelemeTest {

    private static final LocalDateTime SIMDI = LocalDateTime.of(2026, 10, 23, 7, 0);
    private static final LocalDateTime GUN_BAS = LocalDateTime.of(2026, 10, 23, 8, 0);
    private static final LocalDateTime GUN_BIT = LocalDateTime.of(2026, 10, 23, 18, 0);

    private AmeliyatCizelgeleyici cizelgeleyici;
    private Map<String, Hasta> hastalar;
    private List<Doktor> doktorlar;
    private List<Ameliyathane> salonlar;

    @BeforeEach
    void hazirla() {
        cizelgeleyici = new AmeliyatCizelgeleyici(new StandartOncelikStratejisi());
        hastalar = new HashMap<>();
        hastalar.put("H1", new AcilHasta("H1", "Ali", "Kaya", 40, "-", SIMDI, TriajSeviyesi.YESIL));
        hastalar.put("H2", new AcilHasta("H2", "Can", "Ak", 40, "-", SIMDI, TriajSeviyesi.KIRMIZI));
        hastalar.put("H3", new AcilHasta("H3", "Ece", "Su", 40, "-", SIMDI, TriajSeviyesi.SARI));
        doktorlar = new ArrayList<>(List.of(new Doktor("D1", "Ahmet", "Yılmaz", Brans.GENEL_CERRAHI, true)));
        salonlar = new ArrayList<>(List.of(new Ameliyathane("AH-1"), new Ameliyathane("AH-2")));
    }

    @Test
    @DisplayName("Algoritma: Aynı doktor ve aynı salon hiçbir zaman çakışan iki ameliyat almaz")
    void cakismaYok() {
        List<Ameliyat> ameliyatlar = List.of(
                new Ameliyat("A1", "H1", Brans.GENEL_CERRAHI, 120),
                new Ameliyat("A2", "H2", Brans.GENEL_CERRAHI, 90),
                new Ameliyat("A3", "H3", Brans.GENEL_CERRAHI, 60));

        CizelgeSonucu sonuc = cizelgeleyici.planla(ameliyatlar, hastalar, doktorlar, salonlar, GUN_BAS, GUN_BIT, SIMDI);

        assertEquals(3, sonuc.getPlanlananlar().size());
        List<Ameliyat> p = sonuc.getPlanlananlar();
        for (int i = 0; i < p.size(); i++) {
            for (int j = i + 1; j < p.size(); j++) {
                ZamanAraligi a = p.get(i).zamanAraligi();
                ZamanAraligi b = p.get(j).zamanAraligi();
                if (p.get(i).getDoktorId().equals(p.get(j).getDoktorId())
                        || p.get(i).getAmeliyathaneKodu().equals(p.get(j).getAmeliyathaneKodu())) {
                    assertFalse(a.cakisirMi(b), p.get(i).getId() + " ile " + p.get(j).getId() + " çakışıyor");
                }
            }
        }
    }

    @Test
    @DisplayName("Algoritma: En öncelikli hastanın ameliyatı günün ilk saatine yerleşir")
    void oncelikliOnceYerlesir() {
        List<Ameliyat> ameliyatlar = List.of(
                new Ameliyat("A1", "H1", Brans.GENEL_CERRAHI, 60),  // yeşil
                new Ameliyat("A2", "H3", Brans.GENEL_CERRAHI, 60),  // sarı
                new Ameliyat("A3", "H2", Brans.GENEL_CERRAHI, 60)); // kırmızı

        cizelgeleyici.planla(ameliyatlar, hastalar, doktorlar, salonlar, GUN_BAS, GUN_BIT, SIMDI);

        assertEquals(GUN_BAS, ameliyatlar.get(2).getBaslangic());               // kırmızı 08:00
        assertEquals(GUN_BAS.plusMinutes(60), ameliyatlar.get(1).getBaslangic()); // sarı 09:00
        assertEquals(GUN_BAS.plusMinutes(120), ameliyatlar.get(0).getBaslangic()); // yeşil 10:00
    }

    @Test
    @DisplayName("Algoritma: Branşa uygun cerrah yoksa veya gün yetmezse ameliyat ertelenir")
    void uygunKaynakYoksaErtelenir() {
        Ameliyat ortopedi = new Ameliyat("A1", "H1", Brans.ORTOPEDI, 60);   // ortopedi cerrahı yok
        Ameliyat cokUzun = new Ameliyat("A2", "H2", Brans.GENEL_CERRAHI, 660); // 11 saat > mesai

        CizelgeSonucu sonuc = cizelgeleyici.planla(List.of(ortopedi, cokUzun), hastalar, doktorlar, salonlar,
                GUN_BAS, GUN_BIT, SIMDI);

        assertEquals(2, sonuc.getErtelenenler().size());
        assertEquals(AmeliyatDurumu.ERTELENDI, ortopedi.getDurum());
        assertEquals(AmeliyatDurumu.ERTELENDI, cokUzun.getDurum());
    }

    @Test
    @DisplayName("Servis: Ameliyathane arızalanınca plan otomatik güncellenir ve o salon kullanılmaz")
    void arizadaYenidenPlanlama() {
        Clock saat = Clock.fixed(Instant.parse("2026-10-23T07:00:00Z"), ZoneOffset.UTC);
        BellekVeriDeposu depo = new BellekVeriDeposu();
        HastaneYonetimi yonetim = new HastaneYonetimi(depo, new StandartOncelikStratejisi(), saat);
        yonetim.doktorEkle("Ahmet", "Yılmaz", Brans.ORTOPEDI, true);
        yonetim.doktorEkle("Elif", "Demir", Brans.ORTOPEDI, true);
        yonetim.ameliyathaneEkle("AH-1");
        yonetim.ameliyathaneEkle("AH-2");
        Hasta h1 = yonetim.hastaKaydet(HastaTuru.ACIL, "Ali", "Kaya", 30, "-", "SARI");
        Hasta h2 = yonetim.hastaKaydet(HastaTuru.ACIL, "Can", "Ak", 30, "-", "SARI");
        yonetim.ameliyatTalebiOlustur(h1.getId(), Brans.ORTOPEDI, 120);
        yonetim.ameliyatTalebiOlustur(h2.getId(), Brans.ORTOPEDI, 120);

        yonetim.ameliyatlariPlanla(LocalDate.of(2026, 10, 23));
        // İki salon ve iki cerrah var: iki ameliyat da 08:00'de paralel başlar
        assertEquals(GUN_BAS, yonetim.getAmeliyatlar().get(0).getBaslangic());
        assertEquals(GUN_BAS, yonetim.getAmeliyatlar().get(1).getBaslangic());

        Optional<CizelgeSonucu> yeni = yonetim.ameliyathaneDurumuDegistir("AH-2", false);

        assertTrue(yeni.isPresent());
        assertEquals(2, yeni.get().getPlanlananlar().size());
        yonetim.getAmeliyatlar().forEach(a -> assertEquals("AH-1", a.getAmeliyathaneKodu()));
        assertNotEquals(yonetim.getAmeliyatlar().get(0).getBaslangic(), yonetim.getAmeliyatlar().get(1).getBaslangic());
        assertTrue(depo.getKayitSayisi() > 0, "Değişiklikler depoya kaydedilmeli");
    }
}
