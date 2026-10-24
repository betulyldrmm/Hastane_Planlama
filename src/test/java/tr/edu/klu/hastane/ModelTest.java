package tr.edu.klu.hastane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.PoliklinikHasta;
import tr.edu.klu.hastane.model.TriajSeviyesi;
import tr.edu.klu.hastane.model.ZamanAraligi;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 10, 23, 9, 0);

    @Test
    @DisplayName("Kapsülleme: Geçersiz yaş ve boş ad kabul edilmez")
    void gecersizVeriReddedilir() {
        assertThrows(IllegalArgumentException.class,
                () -> new AcilHasta("H1", "Ali", "Kaya", -3, "x", T, TriajSeviyesi.SARI));
        assertThrows(IllegalArgumentException.class,
                () -> new AcilHasta("H1", " ", "Kaya", 30, "x", T, TriajSeviyesi.SARI));
        assertThrows(IllegalArgumentException.class,
                () -> new Ameliyat("A1", "H1", Brans.ORTOPEDI, 0));
    }

    @Test
    @DisplayName("Çok biçimlilik: Her hasta türü temel puanını kendi kuralıyla hesaplar")
    void hastaTurleriFarkliPuanUretir() {
        Hasta kirmizi = new AcilHasta("H1", "Ali", "Kaya", 30, "x", T, TriajSeviyesi.KIRMIZI);
        Hasta yesil = new AcilHasta("H2", "Can", "Ak", 30, "x", T, TriajSeviyesi.YESIL);
        Hasta poliklinik = new PoliklinikHasta("H3", "Ece", "Su", 30, "x", T, Brans.DAHILIYE, true);

        assertEquals(100, kirmizi.temelOncelikPuani());
        assertEquals(20, yesil.temelOncelikPuani());
        assertEquals(PoliklinikHasta.RANDEVULU_PUAN, poliklinik.temelOncelikPuani());
        assertTrue(kirmizi.temelOncelikPuani() > yesil.temelOncelikPuani());
        assertTrue(yesil.temelOncelikPuani() > poliklinik.temelOncelikPuani());
    }

    @Test
    @DisplayName("Zaman aralığı: Örtüşen aralıklar çakışır, uç uca olanlar çakışmaz")
    void zamanAraligiCakismaKontrolu() {
        ZamanAraligi a = new ZamanAraligi(T, T.plusMinutes(60));        // 09:00-10:00
        ZamanAraligi b = new ZamanAraligi(T.plusMinutes(30), T.plusMinutes(90)); // 09:30-10:30
        ZamanAraligi c = new ZamanAraligi(T.plusMinutes(60), T.plusMinutes(120)); // 10:00-11:00

        assertTrue(a.cakisirMi(b));
        assertTrue(b.cakisirMi(a));
        assertFalse(a.cakisirMi(c));
    }
}
