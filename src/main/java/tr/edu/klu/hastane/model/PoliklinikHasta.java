package tr.edu.klu.hastane.model;

import java.time.LocalDateTime;

/** Polikliniğe başvuran hasta. Randevulu hastalar randevusuzlardan biraz önceliklidir. */
public class PoliklinikHasta extends Hasta {

    public static final int RANDEVULU_PUAN = 15;
    public static final int RANDEVUSUZ_PUAN = 10;

    private final Brans brans;
    private final boolean randevulu;

    public PoliklinikHasta(String id, String ad, String soyad, int yas, String sikayet,
                           LocalDateTime kayitZamani, Brans brans, boolean randevulu) {
        super(id, ad, soyad, yas, sikayet, kayitZamani);
        if (brans == null) {
            throw new IllegalArgumentException("Poliklinik branşı boş olamaz.");
        }
        this.brans = brans;
        this.randevulu = randevulu;
    }

    public Brans getBrans() {
        return brans;
    }

    public boolean isRandevulu() {
        return randevulu;
    }

    @Override
    public int temelOncelikPuani() {
        return randevulu ? RANDEVULU_PUAN : RANDEVUSUZ_PUAN;
    }

    @Override
    public String turAdi() {
        return "Poliklinik";
    }

    @Override
    public String rolAciklamasi() {
        return "Poliklinik Hasta: " + tamAd() + " (" + getYas() + " yaş, " + brans.getGorunenAd()
                + (randevulu ? ", randevulu" : ", randevusuz") + ")";
    }
}
