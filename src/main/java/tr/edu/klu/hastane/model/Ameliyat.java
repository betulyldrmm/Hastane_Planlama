package tr.edu.klu.hastane.model;

import java.time.LocalDateTime;

/** Bir hasta için oluşturulan ameliyat talebi ve (planlandıysa) atanmış doktor, salon ve saati. */
public class Ameliyat {

    private final String id;
    private final String hastaId;
    private final Brans brans;
    private final int sureDakika;
    private AmeliyatDurumu durum;
    private String doktorId;
    private String ameliyathaneKodu;
    private LocalDateTime baslangic;

    public Ameliyat(String id, String hastaId, Brans brans, int sureDakika) {
        if (id == null || id.isBlank() || hastaId == null || hastaId.isBlank()) {
            throw new IllegalArgumentException("Ameliyat ve hasta kimliği boş olamaz.");
        }
        if (brans == null) {
            throw new IllegalArgumentException("Ameliyat branşı boş olamaz.");
        }
        if (sureDakika <= 0 || sureDakika > 720) {
            throw new IllegalArgumentException("Ameliyat süresi 1-720 dakika arasında olmalıdır.");
        }
        this.id = id;
        this.hastaId = hastaId;
        this.brans = brans;
        this.sureDakika = sureDakika;
        this.durum = AmeliyatDurumu.BEKLIYOR;
    }

    /** Ameliyatı doktor, salon ve başlangıç saatiyle planlar. */
    public void planla(String doktorId, String ameliyathaneKodu, LocalDateTime baslangic) {
        this.doktorId = doktorId;
        this.ameliyathaneKodu = ameliyathaneKodu;
        this.baslangic = baslangic;
        this.durum = AmeliyatDurumu.PLANLANDI;
    }

    /** Uygun kaynak bulunamadığında ameliyat ertelenir. */
    public void ertele() {
        planiSifirla();
        this.durum = AmeliyatDurumu.ERTELENDI;
    }

    /** Yeniden planlama öncesi eski atamaları temizler. */
    public void planiSifirla() {
        this.doktorId = null;
        this.ameliyathaneKodu = null;
        this.baslangic = null;
        this.durum = AmeliyatDurumu.BEKLIYOR;
    }

    public ZamanAraligi zamanAraligi() {
        if (baslangic == null) {
            return null;
        }
        return new ZamanAraligi(baslangic, baslangic.plusMinutes(sureDakika));
    }

    public String getId() {
        return id;
    }

    public String getHastaId() {
        return hastaId;
    }

    public Brans getBrans() {
        return brans;
    }

    public int getSureDakika() {
        return sureDakika;
    }

    public AmeliyatDurumu getDurum() {
        return durum;
    }

    public String getDoktorId() {
        return doktorId;
    }

    public String getAmeliyathaneKodu() {
        return ameliyathaneKodu;
    }

    public LocalDateTime getBaslangic() {
        return baslangic;
    }
}
