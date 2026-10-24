package tr.edu.klu.hastane.servis;

import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.oncelik.OncelikStratejisi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Muayene bekleyen hastaları öncelik puanına göre sıralar.
 * Algoritma: Öncelik kuyruğu (ikili yığın / binary heap). Ekleme ve çıkarma O(log n).
 * Eşit puanda önce gelen hasta önce muayene edilir (FIFO).
 */
public class MuayeneKuyrugu {

    private final OncelikStratejisi strateji;

    public MuayeneKuyrugu(OncelikStratejisi strateji) {
        this.strateji = strateji;
    }

    /** Bekleyen hastaları en öncelikliden en aza doğru sıralı döndürür. */
    public List<Hasta> oncelikSirasi(Collection<Hasta> hastalar, LocalDateTime simdi) {
        Comparator<Hasta> karsilastirici = Comparator
                .comparingInt((Hasta h) -> strateji.puanHesapla(h, simdi)).reversed()
                .thenComparing(Hasta::getKayitZamani);

        PriorityQueue<Hasta> kuyruk = new PriorityQueue<>(karsilastirici);
        for (Hasta hasta : hastalar) {
            if (hasta.muayeneBekliyorMu()) {
                kuyruk.offer(hasta);
            }
        }
        List<Hasta> sirali = new ArrayList<>(kuyruk.size());
        while (!kuyruk.isEmpty()) {
            sirali.add(kuyruk.poll());
        }
        return sirali;
    }

    /** Sıradaki (en öncelikli) hastayı döndürür; bekleyen yoksa boş Optional. */
    public Optional<Hasta> siradakiHasta(Collection<Hasta> hastalar, LocalDateTime simdi) {
        List<Hasta> sirali = oncelikSirasi(hastalar, simdi);
        return sirali.isEmpty() ? Optional.empty() : Optional.of(sirali.get(0));
    }

    public int puan(Hasta hasta, LocalDateTime simdi) {
        return strateji.puanHesapla(hasta, simdi);
    }
}
