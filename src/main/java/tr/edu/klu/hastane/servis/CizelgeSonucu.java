package tr.edu.klu.hastane.servis;

import tr.edu.klu.hastane.model.Ameliyat;

import java.util.Collections;
import java.util.List;

/** Çizelgeleme algoritmasının çıktısı: planlanan ve ertelenen ameliyatlar. */
public class CizelgeSonucu {

    private final List<Ameliyat> planlananlar;
    private final List<Ameliyat> ertelenenler;

    public CizelgeSonucu(List<Ameliyat> planlananlar, List<Ameliyat> ertelenenler) {
        this.planlananlar = Collections.unmodifiableList(planlananlar);
        this.ertelenenler = Collections.unmodifiableList(ertelenenler);
    }

    public List<Ameliyat> getPlanlananlar() {
        return planlananlar;
    }

    public List<Ameliyat> getErtelenenler() {
        return ertelenenler;
    }
}
