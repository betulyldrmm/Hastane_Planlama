package tr.edu.klu.hastane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.TriajSeviyesi;
import tr.edu.klu.hastane.oncelik.KorunanGrupOncelikDekoratoru;
import tr.edu.klu.hastane.oncelik.OncelikStratejisi;
import tr.edu.klu.hastane.oncelik.StandartOncelikStratejisi;
import tr.edu.klu.hastane.servis.MuayeneKuyrugu;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OncelikTest {

    private static final LocalDateTime SIMDI = LocalDateTime.of(2026, 10, 23, 12, 0);
    private final OncelikStratejisi standart = new StandartOncelikStratejisi();

    private Hasta acil(String id, int yas, TriajSeviyesi triaj, int kacDakikaOnce) {
        return new AcilHasta(id, "Ad" + id, "Soyad", yas, "-", SIMDI.minusMinutes(kacDakikaOnce), triaj);
    }

    @Test
    @DisplayName("Strateji: Bekleme süresi puanı artırır ama üst sınırı aşmaz")
    void beklemeBonusuSinirli() {
        assertEquals(60, standart.puanHesapla(acil("H1", 30, TriajSeviyesi.SARI, 5), SIMDI));
        assertEquals(63, standart.puanHesapla(acil("H2", 30, TriajSeviyesi.SARI, 35), SIMDI));
        // 10 saat bekleyen yeşil hasta: 20 + en fazla 40 = 60, kırmızıyı (100) asla geçemez
        assertEquals(60, standart.puanHesapla(acil("H3", 30, TriajSeviyesi.YESIL, 600), SIMDI));
    }

    @Test
    @DisplayName("Dekoratör: 65 yaş üstü ve 7 yaş altına ek puan verilir")
    void korunanGrupBonusu() {
        OncelikStratejisi dekore = new KorunanGrupOncelikDekoratoru(standart);
        Hasta yasli = acil("H1", 70, TriajSeviyesi.SARI, 0);
        Hasta cocuk = acil("H2", 4, TriajSeviyesi.SARI, 0);
        Hasta yetiskin = acil("H3", 40, TriajSeviyesi.SARI, 0);

        assertEquals(60 + KorunanGrupOncelikDekoratoru.KORUNAN_GRUP_BONUSU, dekore.puanHesapla(yasli, SIMDI));
        assertEquals(60 + KorunanGrupOncelikDekoratoru.KORUNAN_GRUP_BONUSU, dekore.puanHesapla(cocuk, SIMDI));
        assertEquals(60, dekore.puanHesapla(yetiskin, SIMDI));
    }

    @Test
    @DisplayName("Öncelik kuyruğu: Kırmızı önce gelir, eşit puanda önce kayıt olan önce çağrılır")
    void kuyrukSirasi() {
        Hasta yesil = acil("H1", 30, TriajSeviyesi.YESIL, 0);
        Hasta sariErken = acil("H2", 30, TriajSeviyesi.SARI, 9);  // 9 dk -> bonus 0, puan 60
        Hasta sariGec = acil("H3", 30, TriajSeviyesi.SARI, 2);    // puan 60, daha geç kayıt
        Hasta kirmizi = acil("H4", 30, TriajSeviyesi.KIRMIZI, 0);

        List<Hasta> sira = new MuayeneKuyrugu(standart)
                .oncelikSirasi(List.of(yesil, sariGec, kirmizi, sariErken), SIMDI);

        assertEquals(List.of(kirmizi, sariErken, sariGec, yesil), sira);
    }
}
