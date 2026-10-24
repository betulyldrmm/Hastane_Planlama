package tr.edu.klu.hastane.servis;

import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;

import java.time.LocalDateTime;

/** Sunum ve deneme için sisteme hazır örnek veri ekler. Yalnızca veri boşken kullanılır. */
public class OrnekVeriYukleyici {

    private final HastaneYonetimi yonetim;

    public OrnekVeriYukleyici(HastaneYonetimi yonetim) {
        this.yonetim = yonetim;
    }

    public void yukle() {
        if (!yonetim.veriBosMu()) {
            throw new IllegalStateException("Örnek veri yalnızca sistem boşken yüklenebilir.");
        }
        LocalDateTime simdi = yonetim.simdi();

        yonetim.doktorEkle("Ahmet", "Yılmaz", Brans.GENEL_CERRAHI, true);
        yonetim.doktorEkle("Elif", "Demir", Brans.GENEL_CERRAHI, true);
        yonetim.doktorEkle("Mehmet", "Kaya", Brans.ORTOPEDI, true);
        yonetim.doktorEkle("Zeynep", "Arslan", Brans.KARDIYOLOJI, true);
        yonetim.doktorEkle("Can", "Öztürk", Brans.DAHILIYE, false);

        yonetim.ameliyathaneEkle("AH-1");
        yonetim.ameliyathaneEkle("AH-2");

        Hasta h1 = yonetim.hastaKaydet(HastaTuru.ACIL, "Mustafa", "Çelik", 72, "Göğüs ağrısı", "KIRMIZI", simdi.minusMinutes(15));
        Hasta h2 = yonetim.hastaKaydet(HastaTuru.ACIL, "Ayşe", "Koç", 34, "Akut karın ağrısı", "SARI", simdi.minusMinutes(50));
        Hasta h3 = yonetim.hastaKaydet(HastaTuru.ACIL, "Emre", "Şahin", 19, "Bilek burkulması", "YESIL", simdi.minusMinutes(130));
        Hasta h4 = yonetim.hastaKaydet(HastaTuru.POLIKLINIK, "Fatma", "Aydın", 58, "Safra kesesi taşı", "GENEL_CERRAHI:R", simdi.minusMinutes(40));
        yonetim.hastaKaydet(HastaTuru.ACIL, "Deniz", "Kurt", 5, "Yüksek ateş", "SARI", simdi.minusMinutes(10));
        Hasta h6 = yonetim.hastaKaydet(HastaTuru.POLIKLINIK, "Hüseyin", "Polat", 45, "Diz ağrısı", "ORTOPEDI", simdi.minusMinutes(200));
        Hasta h7 = yonetim.hastaKaydet(HastaTuru.ACIL, "Selin", "Yıldız", 29, "Kol kırığı", "SARI", simdi.minusMinutes(30));
        yonetim.hastaKaydet(HastaTuru.POLIKLINIK, "Kemal", "Aksoy", 67, "Tansiyon kontrolü", "DAHILIYE:R", simdi.minusMinutes(25));

        yonetim.ameliyatTalebiOlustur(h2.getId(), Brans.GENEL_CERRAHI, 90);
        yonetim.ameliyatTalebiOlustur(h4.getId(), Brans.GENEL_CERRAHI, 120);
        yonetim.ameliyatTalebiOlustur(h1.getId(), Brans.KARDIYOLOJI, 180);
        yonetim.ameliyatTalebiOlustur(h7.getId(), Brans.ORTOPEDI, 120);
        yonetim.ameliyatTalebiOlustur(h6.getId(), Brans.ORTOPEDI, 150);
        yonetim.ameliyatTalebiOlustur(h3.getId(), Brans.ORTOPEDI, 60);
    }
}
