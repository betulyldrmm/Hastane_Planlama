package tr.edu.klu.hastane.oncelik;

import tr.edu.klu.hastane.model.Hasta;

import java.time.LocalDateTime;

/**
 * DEKORATÖR TASARIM KALIBI: Var olan herhangi bir öncelik stratejisini sarar ve
 * 65 yaş üstü ile 7 yaş altı hastalara (korunan grup) ek puan ekler.
 * Sarılan stratejinin kodu hiç değiştirilmez.
 */
public class KorunanGrupOncelikDekoratoru implements OncelikStratejisi {

    public static final int KORUNAN_GRUP_BONUSU = 15;
    public static final int YASLI_SINIRI = 65;
    public static final int COCUK_SINIRI = 7;

    private final OncelikStratejisi icStrateji;

    public KorunanGrupOncelikDekoratoru(OncelikStratejisi icStrateji) {
        if (icStrateji == null) {
            throw new IllegalArgumentException("Sarılacak strateji boş olamaz.");
        }
        this.icStrateji = icStrateji;
    }

    @Override
    public int puanHesapla(Hasta hasta, LocalDateTime simdi) {
        int puan = icStrateji.puanHesapla(hasta, simdi);
        if (korunanGruptaMi(hasta)) {
            puan += KORUNAN_GRUP_BONUSU;
        }
        return puan;
    }

    private boolean korunanGruptaMi(Hasta hasta) {
        return hasta.getYas() >= YASLI_SINIRI || hasta.getYas() <= COCUK_SINIRI;
    }

    @Override
    public String ad() {
        return icStrateji.ad() + " + korunan grup (65+ / 7-) bonusu";
    }
}
