package tr.edu.klu.hastane.fabrika;

import java.util.Collection;

/**
 * Önekli, sıralı ve okunabilir kimlik üretir (ör. H001, H002...).
 * Dosyadan veri yüklendiğinde mevcut en büyük numaradan devam eder.
 */
public class IdUretici {

    private final String onek;
    private int sayac;

    public IdUretici(String onek) {
        this.onek = onek;
        this.sayac = 0;
    }

    public String sonrakiId() {
        sayac++;
        return String.format("%s%03d", onek, sayac);
    }

    /** Var olan kimliklere bakarak sayacı en büyük numaraya getirir. */
    public void mevcutlaraGoreGuncelle(Collection<String> mevcutIdler) {
        for (String id : mevcutIdler) {
            if (id != null && id.startsWith(onek)) {
                try {
                    int numara = Integer.parseInt(id.substring(onek.length()));
                    sayac = Math.max(sayac, numara);
                } catch (NumberFormatException ignored) {
                    // Biçimi farklı kimlikler sayaca etki etmez.
                }
            }
        }
    }
}
