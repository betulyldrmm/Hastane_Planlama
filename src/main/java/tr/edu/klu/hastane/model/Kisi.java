package tr.edu.klu.hastane.model;

/**
 * Sistemdeki tüm kişilerin (hasta, doktor) ortak soyut üst sınıfı.
 * SOYUTLAMA: Kisi tek başına nesnesi oluşturulamayan bir kavramdır.
 * KAPSÜLLEME: Alanlar private'tır, değişiklik yalnızca doğrulama yapan metotlarla olur.
 */
public abstract class Kisi {

    private final String id;
    private String ad;
    private String soyad;

    protected Kisi(String id, String ad, String soyad) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Kimlik (id) boş olamaz.");
        }
        this.id = id;
        setAd(ad);
        setSoyad(soyad);
    }

    public String getId() {
        return id;
    }

    public String getAd() {
        return ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public final void setAd(String ad) {
        this.ad = metinDogrula(ad, "Ad");
    }

    public final void setSoyad(String soyad) {
        this.soyad = metinDogrula(soyad, "Soyad");
    }

    public String tamAd() {
        return ad + " " + soyad;
    }

    /** ÇOK BİÇİMLİLİK: Her alt sınıf kendini farklı şekilde tanıtır. */
    public abstract String rolAciklamasi();

    private static String metinDogrula(String deger, String alanAdi) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz.");
        }
        return deger.trim();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + rolAciklamasi();
    }
}
