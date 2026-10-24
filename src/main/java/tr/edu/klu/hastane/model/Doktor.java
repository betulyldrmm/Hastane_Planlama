package tr.edu.klu.hastane.model;

/** Hastanede çalışan doktor. Cerrah olanlar ameliyatlara atanabilir. */
public class Doktor extends Kisi {

    private final Brans brans;
    private boolean cerrah;

    public Doktor(String id, String ad, String soyad, Brans brans, boolean cerrah) {
        super(id, ad, soyad);
        if (brans == null) {
            throw new IllegalArgumentException("Doktor branşı boş olamaz.");
        }
        this.brans = brans;
        this.cerrah = cerrah;
    }

    public Brans getBrans() {
        return brans;
    }

    public boolean isCerrah() {
        return cerrah;
    }

    public void setCerrah(boolean cerrah) {
        this.cerrah = cerrah;
    }

    /** Bu doktor verilen branştaki bir ameliyatı yapabilir mi? */
    public boolean ameliyatYapabilirMi(Brans ameliyatBransi) {
        return cerrah && brans == ameliyatBransi;
    }

    @Override
    public String rolAciklamasi() {
        return "Dr. " + tamAd() + " (" + brans.getGorunenAd() + (cerrah ? ", cerrah" : "") + ")";
    }
}
