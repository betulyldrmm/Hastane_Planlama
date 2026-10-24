package tr.edu.klu.hastane.model;

/** Acil servis triaj renk kodları ve her birinin taban öncelik puanı. */
public enum TriajSeviyesi {
    KIRMIZI(100, "Hayati tehlike, hemen müdahale"),
    SARI(60, "Acil, kısa sürede müdahale"),
    YESIL(20, "Ertelenebilir, ayaktan tedavi");

    private final int tabanPuan;
    private final String aciklama;

    TriajSeviyesi(int tabanPuan, String aciklama) {
        this.tabanPuan = tabanPuan;
        this.aciklama = aciklama;
    }

    public int getTabanPuan() {
        return tabanPuan;
    }

    public String getAciklama() {
        return aciklama;
    }
}
