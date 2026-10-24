package tr.edu.klu.hastane.model;

import java.time.LocalDateTime;

/**
 * Tüm hasta türlerinin soyut üst sınıfı (KALITIM: Kisi'den türer).
 * Her hasta türü kendi temel öncelik puanını farklı hesaplar (ÇOK BİÇİMLİLİK).
 */
public abstract class Hasta extends Kisi {

    private int yas;
    private String sikayet;
    private final LocalDateTime kayitZamani;
    private HastaDurumu durum;

    protected Hasta(String id, String ad, String soyad, int yas, String sikayet, LocalDateTime kayitZamani) {
        super(id, ad, soyad);
        setYas(yas);
        setSikayet(sikayet);
        if (kayitZamani == null) {
            throw new IllegalArgumentException("Kayıt zamanı boş olamaz.");
        }
        this.kayitZamani = kayitZamani;
        this.durum = HastaDurumu.BEKLIYOR;
    }

    /** Hasta türüne özgü, beklemeden bağımsız temel öncelik puanı. */
    public abstract int temelOncelikPuani();

    /** Hasta türünün ekranda görünen adı. */
    public abstract String turAdi();

    public int getYas() {
        return yas;
    }

    public final void setYas(int yas) {
        if (yas < 0 || yas > 130) {
            throw new IllegalArgumentException("Yaş 0 ile 130 arasında olmalıdır: " + yas);
        }
        this.yas = yas;
    }

    public String getSikayet() {
        return sikayet;
    }

    public final void setSikayet(String sikayet) {
        this.sikayet = (sikayet == null || sikayet.isBlank()) ? "-" : sikayet.trim();
    }

    public LocalDateTime getKayitZamani() {
        return kayitZamani;
    }

    public HastaDurumu getDurum() {
        return durum;
    }

    public void muayeneEdildi() {
        this.durum = HastaDurumu.MUAYENE_EDILDI;
    }

    public void ameliyatPlanlandi() {
        this.durum = HastaDurumu.AMELIYAT_PLANLANDI;
    }

    public void ameliyatErtelendi() {
        this.durum = HastaDurumu.AMELIYAT_ERTELENDI;
    }

    public void taburcuEt() {
        this.durum = HastaDurumu.TABURCU;
    }

    public boolean muayeneBekliyorMu() {
        return durum == HastaDurumu.BEKLIYOR;
    }
}
