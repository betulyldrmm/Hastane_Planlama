package tr.edu.klu.hastane.servis;

import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.ZamanAraligi;
import tr.edu.klu.hastane.oncelik.OncelikStratejisi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * PROJENİN ANA ALGORİTMASI: Öncelik tabanlı açgözlü (greedy) liste çizelgeleme.
 *
 * Adımlar:
 *  1) Ameliyatlar, ait oldukları hastanın öncelik puanına göre büyükten küçüğe sıralanır
 *     (eşitlikte uzun süren ameliyat önce yerleştirilir).
 *  2) Her ameliyat için aday başlangıç saatleri: gün başlangıcı + uygun doktorların ve aktif
 *     ameliyathanelerin mevcut ameliyatlarının bitiş saatleri. (En erken boş an her zaman bu
 *     noktalardan biridir.)
 *  3) Adaylar küçükten büyüğe denenir; o aralıkta hem branşı uyan boş bir cerrah hem boş ve
 *     aktif bir ameliyathane varsa ameliyat oraya yerleştirilir.
 *  4) Gün bitimine kadar yer bulunamazsa ameliyat ERTELENDI olarak işaretlenir.
 *
 * Sonuç: Hiçbir doktor ve hiçbir salon aynı anda iki ameliyata atanmaz; acil olan önce yerleşir.
 * Karmaşıklık: n ameliyat, d doktor, s salon için yaklaşık O(n² · (d + s)).
 */
public class AmeliyatCizelgeleyici {

    private final OncelikStratejisi strateji;

    public AmeliyatCizelgeleyici(OncelikStratejisi strateji) {
        this.strateji = strateji;
    }

    public CizelgeSonucu planla(Collection<Ameliyat> ameliyatlar,
                                Map<String, Hasta> hastalar,
                                List<Doktor> doktorlar,
                                List<Ameliyathane> ameliyathaneler,
                                LocalDateTime gunBaslangici,
                                LocalDateTime gunBitisi,
                                LocalDateTime simdi) {

        List<Ameliyat> sirali = new ArrayList<>(ameliyatlar);
        sirali.forEach(Ameliyat::planiSifirla);
        sirali.sort(Comparator
                .comparingInt((Ameliyat a) -> hastaPuani(a, hastalar, simdi)).reversed()
                .thenComparing(Comparator.comparingInt(Ameliyat::getSureDakika).reversed())
                .thenComparing(Ameliyat::getId));

        Map<String, List<ZamanAraligi>> doktorTakvimi = new HashMap<>();
        Map<String, List<ZamanAraligi>> salonTakvimi = new HashMap<>();
        List<Ameliyat> planlananlar = new ArrayList<>();
        List<Ameliyat> ertelenenler = new ArrayList<>();

        for (Ameliyat ameliyat : sirali) {
            List<Doktor> uygunDoktorlar = new ArrayList<>();
            for (Doktor d : doktorlar) {
                if (d.ameliyatYapabilirMi(ameliyat.getBrans())) {
                    uygunDoktorlar.add(d);
                }
            }
            List<Ameliyathane> aktifSalonlar = new ArrayList<>();
            for (Ameliyathane s : ameliyathaneler) {
                if (s.isAktif()) {
                    aktifSalonlar.add(s);
                }
            }

            boolean yerlesti = false;
            if (!uygunDoktorlar.isEmpty() && !aktifSalonlar.isEmpty()) {
                TreeSet<LocalDateTime> adaySaatler = new TreeSet<>();
                adaySaatler.add(gunBaslangici);
                for (Doktor d : uygunDoktorlar) {
                    doktorTakvimi.getOrDefault(d.getId(), List.of()).forEach(z -> adaySaatler.add(z.getBitis()));
                }
                for (Ameliyathane s : aktifSalonlar) {
                    salonTakvimi.getOrDefault(s.getKod(), List.of()).forEach(z -> adaySaatler.add(z.getBitis()));
                }

                for (LocalDateTime aday : adaySaatler) {
                    ZamanAraligi aralik = new ZamanAraligi(aday, aday.plusMinutes(ameliyat.getSureDakika()));
                    if (aralik.getBitis().isAfter(gunBitisi)) {
                        break; // Sonraki adaylar daha da geç olacağı için aramayı bitir.
                    }
                    Doktor bosDoktor = ilkBosDoktor(uygunDoktorlar, doktorTakvimi, aralik);
                    Ameliyathane bosSalon = ilkBosSalon(aktifSalonlar, salonTakvimi, aralik);
                    if (bosDoktor != null && bosSalon != null) {
                        ameliyat.planla(bosDoktor.getId(), bosSalon.getKod(), aday);
                        doktorTakvimi.computeIfAbsent(bosDoktor.getId(), k -> new ArrayList<>()).add(aralik);
                        salonTakvimi.computeIfAbsent(bosSalon.getKod(), k -> new ArrayList<>()).add(aralik);
                        Hasta hasta = hastalar.get(ameliyat.getHastaId());
                        if (hasta != null) {
                            hasta.ameliyatPlanlandi();
                        }
                        planlananlar.add(ameliyat);
                        yerlesti = true;
                        break;
                    }
                }
            }
            if (!yerlesti) {
                ameliyat.ertele();
                Hasta hasta = hastalar.get(ameliyat.getHastaId());
                if (hasta != null) {
                    hasta.ameliyatErtelendi();
                }
                ertelenenler.add(ameliyat);
            }
        }

        planlananlar.sort(Comparator.comparing(Ameliyat::getBaslangic).thenComparing(Ameliyat::getAmeliyathaneKodu));
        return new CizelgeSonucu(planlananlar, ertelenenler);
    }

    private int hastaPuani(Ameliyat ameliyat, Map<String, Hasta> hastalar, LocalDateTime simdi) {
        Hasta hasta = hastalar.get(ameliyat.getHastaId());
        return hasta == null ? 0 : strateji.puanHesapla(hasta, simdi);
    }

    private Doktor ilkBosDoktor(List<Doktor> adaylar, Map<String, List<ZamanAraligi>> takvim, ZamanAraligi aralik) {
        for (Doktor d : adaylar) {
            if (bosMu(takvim.get(d.getId()), aralik)) {
                return d;
            }
        }
        return null;
    }

    private Ameliyathane ilkBosSalon(List<Ameliyathane> adaylar, Map<String, List<ZamanAraligi>> takvim, ZamanAraligi aralik) {
        for (Ameliyathane s : adaylar) {
            if (bosMu(takvim.get(s.getKod()), aralik)) {
                return s;
            }
        }
        return null;
    }

    private boolean bosMu(List<ZamanAraligi> doluAraliklar, ZamanAraligi aralik) {
        if (doluAraliklar == null) {
            return true;
        }
        for (ZamanAraligi dolu : doluAraliklar) {
            if (dolu.cakisirMi(aralik)) {
                return false;
            }
        }
        return true;
    }
}
