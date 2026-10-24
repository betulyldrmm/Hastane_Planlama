package tr.edu.klu.hastane.model;

import java.time.LocalDateTime;

/**
 * Değiştirilemez (immutable) zaman aralığı: [baslangic, bitis).
 * Çizelgeleme algoritmasında çakışma kontrolü için kullanılır.
 */
public final class ZamanAraligi {

    private final LocalDateTime baslangic;
    private final LocalDateTime bitis;

    public ZamanAraligi(LocalDateTime baslangic, LocalDateTime bitis) {
        if (baslangic == null || bitis == null || !bitis.isAfter(baslangic)) {
            throw new IllegalArgumentException("Bitiş zamanı başlangıçtan sonra olmalıdır.");
        }
        this.baslangic = baslangic;
        this.bitis = bitis;
    }

    /** İki aralık çakışıyor mu? Uç uca eklenen aralıklar (10:00 biter, 10:00 başlar) çakışmaz. */
    public boolean cakisirMi(ZamanAraligi diger) {
        return this.baslangic.isBefore(diger.bitis) && diger.baslangic.isBefore(this.bitis);
    }

    public LocalDateTime getBaslangic() {
        return baslangic;
    }

    public LocalDateTime getBitis() {
        return bitis;
    }
}
