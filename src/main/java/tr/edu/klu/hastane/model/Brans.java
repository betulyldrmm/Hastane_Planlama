package tr.edu.klu.hastane.model;

/** Hastanedeki tıbbi branşlar. */
public enum Brans {
    GENEL_CERRAHI("Genel Cerrahi"),
    ORTOPEDI("Ortopedi"),
    KARDIYOLOJI("Kardiyoloji"),
    NOROLOJI("Nöroloji"),
    DAHILIYE("Dahiliye"),
    KBB("Kulak Burun Boğaz");

    private final String gorunenAd;

    Brans(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }
}
