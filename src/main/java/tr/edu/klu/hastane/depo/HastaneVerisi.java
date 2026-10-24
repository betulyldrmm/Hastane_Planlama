package tr.edu.klu.hastane.depo;

import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;

import java.util.ArrayList;
import java.util.List;

/** Kalıcı olarak saklanan tüm hastane verisini bir arada tutan veri taşıyıcı sınıf. */
public class HastaneVerisi {

    private List<Hasta> hastalar = new ArrayList<>();
    private List<Doktor> doktorlar = new ArrayList<>();
    private List<Ameliyathane> ameliyathaneler = new ArrayList<>();
    private List<Ameliyat> ameliyatlar = new ArrayList<>();

    public List<Hasta> getHastalar() {
        if (hastalar == null) {
            hastalar = new ArrayList<>();
        }
        return hastalar;
    }

    public List<Doktor> getDoktorlar() {
        if (doktorlar == null) {
            doktorlar = new ArrayList<>();
        }
        return doktorlar;
    }

    public List<Ameliyathane> getAmeliyathaneler() {
        if (ameliyathaneler == null) {
            ameliyathaneler = new ArrayList<>();
        }
        return ameliyathaneler;
    }

    public List<Ameliyat> getAmeliyatlar() {
        if (ameliyatlar == null) {
            ameliyatlar = new ArrayList<>();
        }
        return ameliyatlar;
    }
}
