package tr.edu.klu.hastane.oncelik;

import tr.edu.klu.hastane.model.Hasta;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Varsayılan strateji: Puan = hasta türünün temel puanı + bekleme bonusu.
 * Bekleme bonusu her 10 dakikada 1 puandır ve en fazla 40 olabilir; böylece uzun bekleyen
 * hastalar unutulmaz (açlık / starvation önlenir) ama yeşil alan hastası kırmızıyı geçemez.
 */
public class StandartOncelikStratejisi implements OncelikStratejisi {

    public static final int DAKIKA_BASINA_BONUS_ARALIGI = 10;
    public static final int MAKS_BEKLEME_BONUSU = 40;

    @Override
    public int puanHesapla(Hasta hasta, LocalDateTime simdi) {
        return hasta.temelOncelikPuani() + beklemeBonusu(hasta, simdi);
    }

    int beklemeBonusu(Hasta hasta, LocalDateTime simdi) {
        long bekleyenDakika = Duration.between(hasta.getKayitZamani(), simdi).toMinutes();
        if (bekleyenDakika <= 0) {
            return 0;
        }
        return (int) Math.min(MAKS_BEKLEME_BONUSU, bekleyenDakika / DAKIKA_BASINA_BONUS_ARALIGI);
    }

    @Override
    public String ad() {
        return "Standart (triaj + bekleme süresi)";
    }
}
