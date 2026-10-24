package tr.edu.klu.hastane.model;

import java.time.LocalDateTime;

/** Acil servise başvuran hasta. Önceliği triaj rengine göre belirlenir. */
public class AcilHasta extends Hasta {

    private TriajSeviyesi triaj;

    public AcilHasta(String id, String ad, String soyad, int yas, String sikayet,
                     LocalDateTime kayitZamani, TriajSeviyesi triaj) {
        super(id, ad, soyad, yas, sikayet, kayitZamani);
        setTriaj(triaj);
    }

    public TriajSeviyesi getTriaj() {
        return triaj;
    }

    public final void setTriaj(TriajSeviyesi triaj) {
        if (triaj == null) {
            throw new IllegalArgumentException("Triaj seviyesi boş olamaz.");
        }
        this.triaj = triaj;
    }

    @Override
    public int temelOncelikPuani() {
        return triaj.getTabanPuan();
    }

    @Override
    public String turAdi() {
        return "Acil";
    }

    @Override
    public String rolAciklamasi() {
        return "Acil Hasta: " + tamAd() + " (" + getYas() + " yaş, triaj: " + triaj + ")";
    }
}
