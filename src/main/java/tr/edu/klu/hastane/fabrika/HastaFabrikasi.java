package tr.edu.klu.hastane.fabrika;

import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;
import tr.edu.klu.hastane.model.PoliklinikHasta;
import tr.edu.klu.hastane.model.TriajSeviyesi;

import java.time.LocalDateTime;

/**
 * FABRİKA TASARIM KALIBI: Hasta nesnelerinin hangi alt sınıftan oluşturulacağına
 * tek bir yerde karar verir. Arayüz katmanı AcilHasta / PoliklinikHasta sınıflarını
 * doğrudan tanımak zorunda kalmaz; yeni bir hasta türü eklendiğinde yalnızca burası değişir.
 */
public class HastaFabrikasi {

    private final IdUretici idUretici;

    public HastaFabrikasi(IdUretici idUretici) {
        this.idUretici = idUretici;
    }

    /**
     * @param ekBilgi ACIL için triaj rengi (KIRMIZI/SARI/YESIL),
     *                POLIKLINIK için "BRANS" veya "BRANS:R" (R = randevulu) biçiminde.
     */
    public Hasta olustur(HastaTuru tur, String ad, String soyad, int yas, String sikayet,
                         String ekBilgi, LocalDateTime kayitZamani) {
        if (tur == null) {
            throw new IllegalArgumentException("Hasta türü boş olamaz.");
        }
        String id = idUretici.sonrakiId();
        switch (tur) {
            case ACIL:
                TriajSeviyesi triaj = TriajSeviyesi.valueOf(ekBilgi.trim().toUpperCase(java.util.Locale.ROOT));
                return new AcilHasta(id, ad, soyad, yas, sikayet, kayitZamani, triaj);
            case POLIKLINIK:
                String[] parcalar = ekBilgi.trim().toUpperCase(java.util.Locale.ROOT).split(":");
                Brans brans = Brans.valueOf(parcalar[0]);
                boolean randevulu = parcalar.length > 1 && parcalar[1].equals("R");
                return new PoliklinikHasta(id, ad, soyad, yas, sikayet, kayitZamani, brans, randevulu);
            default:
                throw new IllegalArgumentException("Desteklenmeyen hasta türü: " + tur);
        }
    }
}
