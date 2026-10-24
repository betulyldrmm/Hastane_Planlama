package tr.edu.klu.hastane.model;

/** Ameliyat salonu. Arıza/bakım durumunda devre dışı bırakılır ve planlamada kullanılmaz. */
public class Ameliyathane {

    private final String kod;
    private boolean aktif;

    public Ameliyathane(String kod) {
        if (kod == null || kod.isBlank()) {
            throw new IllegalArgumentException("Ameliyathane kodu boş olamaz.");
        }
        this.kod = kod.trim().toUpperCase(java.util.Locale.ROOT);
        this.aktif = true;
    }

    public String getKod() {
        return kod;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void devreDisiBirak() {
        this.aktif = false;
    }

    public void aktifEt() {
        this.aktif = true;
    }

    @Override
    public String toString() {
        return "Ameliyathane " + kod + (aktif ? " (aktif)" : " (ARIZALI / BAKIMDA)");
    }
}
