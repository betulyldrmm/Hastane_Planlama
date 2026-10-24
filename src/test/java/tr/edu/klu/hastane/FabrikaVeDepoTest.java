package tr.edu.klu.hastane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.klu.hastane.depo.HastaneVerisi;
import tr.edu.klu.hastane.depo.JsonVeriDeposu;
import tr.edu.klu.hastane.fabrika.HastaFabrikasi;
import tr.edu.klu.hastane.fabrika.IdUretici;
import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;
import tr.edu.klu.hastane.model.PoliklinikHasta;
import tr.edu.klu.hastane.model.TriajSeviyesi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabrikaVeDepoTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 10, 23, 9, 0);

    @Test
    @DisplayName("Fabrika: Türe göre doğru alt sınıfı üretir ve sıralı ID verir")
    void fabrikaDogruTurUretir() {
        HastaFabrikasi fabrika = new HastaFabrikasi(new IdUretici("H"));

        Hasta acil = fabrika.olustur(HastaTuru.ACIL, "Ali", "Kaya", 40, "x", "kirmizi", T);
        Hasta poli = fabrika.olustur(HastaTuru.POLIKLINIK, "Ece", "Su", 25, "y", "ORTOPEDI:R", T);

        AcilHasta acilHasta = assertInstanceOf(AcilHasta.class, acil);
        assertEquals(TriajSeviyesi.KIRMIZI, acilHasta.getTriaj());
        PoliklinikHasta poliHasta = assertInstanceOf(PoliklinikHasta.class, poli);
        assertTrue(poliHasta.isRandevulu());
        assertEquals("H001", acil.getId());
        assertEquals("H002", poli.getId());
        assertThrows(IllegalArgumentException.class,
                () -> fabrika.olustur(HastaTuru.ACIL, "Ali", "Kaya", 40, "x", "MOR", T));
    }

    @Test
    @DisplayName("JSON depo: Kaydedilen veri tekrar okununca alt türler ve plan bilgisi korunur")
    void jsonKaydetVeYukle() throws Exception {
        Path dosya = Files.createTempDirectory("hastane-test").resolve("veri.json");
        JsonVeriDeposu depo = new JsonVeriDeposu(dosya);

        HastaneVerisi veri = new HastaneVerisi();
        veri.getHastalar().add(new AcilHasta("H001", "Ali", "Kaya", 40, "Göğüs ağrısı", T, TriajSeviyesi.SARI));
        veri.getHastalar().add(new PoliklinikHasta("H002", "Ece", "Şahin", 25, "Diz", T, Brans.ORTOPEDI, false));
        veri.getDoktorlar().add(new Doktor("D001", "Mehmet", "Öz", Brans.ORTOPEDI, true));
        Ameliyathane salon = new Ameliyathane("AH-1");
        salon.devreDisiBirak();
        veri.getAmeliyathaneler().add(salon);
        Ameliyat ameliyat = new Ameliyat("A001", "H002", Brans.ORTOPEDI, 90);
        ameliyat.planla("D001", "AH-1", T);
        veri.getAmeliyatlar().add(ameliyat);

        depo.kaydet(veri);
        HastaneVerisi okunan = new JsonVeriDeposu(dosya).yukle();

        assertEquals(2, okunan.getHastalar().size());
        AcilHasta acil = assertInstanceOf(AcilHasta.class, okunan.getHastalar().get(0));
        assertEquals(TriajSeviyesi.SARI, acil.getTriaj());
        assertEquals("Şahin", okunan.getHastalar().get(1).getSoyad());
        assertInstanceOf(PoliklinikHasta.class, okunan.getHastalar().get(1));
        assertFalse(okunan.getAmeliyathaneler().get(0).isAktif());
        assertEquals(T, okunan.getAmeliyatlar().get(0).getBaslangic());
        assertEquals("D001", okunan.getAmeliyatlar().get(0).getDoktorId());
    }
}
