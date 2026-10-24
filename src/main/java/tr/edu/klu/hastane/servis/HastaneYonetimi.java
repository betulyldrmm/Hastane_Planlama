package tr.edu.klu.hastane.servis;

import tr.edu.klu.hastane.depo.HastaneVerisi;
import tr.edu.klu.hastane.depo.VeriDeposu;
import tr.edu.klu.hastane.fabrika.HastaFabrikasi;
import tr.edu.klu.hastane.fabrika.IdUretici;
import tr.edu.klu.hastane.model.Ameliyat;
import tr.edu.klu.hastane.model.Ameliyathane;
import tr.edu.klu.hastane.model.Brans;
import tr.edu.klu.hastane.model.Doktor;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.HastaTuru;
import tr.edu.klu.hastane.model.Kisi;
import tr.edu.klu.hastane.oncelik.OncelikStratejisi;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Uygulama servis katmanı. Konsol arayüzü yalnızca bu sınıfla konuşur.
 * Bağımlılıklarını (depo, strateji, saat) dışarıdan alır: Dependency Inversion.
 * Her değişiklikten sonra veri kalıcı depoya yazılır.
 */
public class HastaneYonetimi {

    public static final LocalTime MESAI_BASLANGIC = LocalTime.of(8, 0);
    public static final LocalTime MESAI_BITIS = LocalTime.of(18, 0);

    private final VeriDeposu depo;
    private final Clock saat;
    private final HastaneVerisi veri;
    private final HastaFabrikasi hastaFabrikasi;
    private final IdUretici doktorIdUretici = new IdUretici("D");
    private final IdUretici ameliyatIdUretici = new IdUretici("A");
    private OncelikStratejisi strateji;
    private LocalDate sonPlanTarihi;

    public HastaneYonetimi(VeriDeposu depo, OncelikStratejisi strateji, Clock saat) {
        this.depo = depo;
        this.strateji = strateji;
        this.saat = saat;
        this.veri = depo.yukle();

        IdUretici hastaIdUretici = new IdUretici("H");
        hastaIdUretici.mevcutlaraGoreGuncelle(veri.getHastalar().stream().map(Kisi::getId).collect(Collectors.toList()));
        doktorIdUretici.mevcutlaraGoreGuncelle(veri.getDoktorlar().stream().map(Kisi::getId).collect(Collectors.toList()));
        ameliyatIdUretici.mevcutlaraGoreGuncelle(veri.getAmeliyatlar().stream().map(Ameliyat::getId).collect(Collectors.toList()));
        this.hastaFabrikasi = new HastaFabrikasi(hastaIdUretici);
    }

    // ---------------- Kayıt işlemleri ----------------

    public Hasta hastaKaydet(HastaTuru tur, String ad, String soyad, int yas, String sikayet, String ekBilgi) {
        return hastaKaydet(tur, ad, soyad, yas, sikayet, ekBilgi, simdi());
    }

    public Hasta hastaKaydet(HastaTuru tur, String ad, String soyad, int yas, String sikayet,
                             String ekBilgi, LocalDateTime kayitZamani) {
        Hasta hasta = hastaFabrikasi.olustur(tur, ad, soyad, yas, sikayet, ekBilgi, kayitZamani);
        veri.getHastalar().add(hasta);
        kaydet();
        return hasta;
    }

    public Doktor doktorEkle(String ad, String soyad, Brans brans, boolean cerrah) {
        Doktor doktor = new Doktor(doktorIdUretici.sonrakiId(), ad, soyad, brans, cerrah);
        veri.getDoktorlar().add(doktor);
        kaydet();
        return doktor;
    }

    public Ameliyathane ameliyathaneEkle(String kod) {
        Ameliyathane salon = new Ameliyathane(kod);
        if (ameliyathaneBul(salon.getKod()).isPresent()) {
            throw new IllegalArgumentException("Bu kodla bir ameliyathane zaten var: " + salon.getKod());
        }
        veri.getAmeliyathaneler().add(salon);
        kaydet();
        return salon;
    }

    public Ameliyat ameliyatTalebiOlustur(String hastaId, Brans brans, int sureDakika) {
        if (hastaBul(hastaId).isEmpty()) {
            throw new IllegalArgumentException("Hasta bulunamadı: " + hastaId);
        }
        Ameliyat ameliyat = new Ameliyat(ameliyatIdUretici.sonrakiId(), hastaId.toUpperCase(java.util.Locale.ROOT), brans, sureDakika);
        veri.getAmeliyatlar().add(ameliyat);
        kaydet();
        return ameliyat;
    }

    // ---------------- Muayene kuyruğu ----------------

    public List<Hasta> muayeneSirasi() {
        return new MuayeneKuyrugu(strateji).oncelikSirasi(veri.getHastalar(), simdi());
    }

    public Optional<Hasta> siradakiHastayiCagir() {
        Optional<Hasta> siradaki = new MuayeneKuyrugu(strateji).siradakiHasta(veri.getHastalar(), simdi());
        siradaki.ifPresent(h -> {
            h.muayeneEdildi();
            kaydet();
        });
        return siradaki;
    }

    public int oncelikPuani(Hasta hasta) {
        return strateji.puanHesapla(hasta, simdi());
    }

    // ---------------- Ameliyat çizelgeleme ----------------

    public CizelgeSonucu ameliyatlariPlanla(LocalDate tarih) {
        sonPlanTarihi = tarih;
        Map<String, Hasta> hastaHaritasi = new LinkedHashMap<>();
        veri.getHastalar().forEach(h -> hastaHaritasi.put(h.getId(), h));

        CizelgeSonucu sonuc = new AmeliyatCizelgeleyici(strateji).planla(
                veri.getAmeliyatlar(), hastaHaritasi, veri.getDoktorlar(), veri.getAmeliyathaneler(),
                tarih.atTime(MESAI_BASLANGIC), tarih.atTime(MESAI_BITIS), simdi());
        kaydet();
        return sonuc;
    }

    /**
     * Ameliyathane arızalandığında / bakımdan çıktığında durumunu değiştirir ve
     * daha önce plan yapılmışsa planı otomatik olarak yeniden hesaplar.
     */
    public Optional<CizelgeSonucu> ameliyathaneDurumuDegistir(String kod, boolean aktif) {
        Ameliyathane salon = ameliyathaneBul(kod)
                .orElseThrow(() -> new IllegalArgumentException("Ameliyathane bulunamadı: " + kod));
        if (aktif) {
            salon.aktifEt();
        } else {
            salon.devreDisiBirak();
        }
        kaydet();
        boolean planVar = veri.getAmeliyatlar().stream().anyMatch(a -> a.getBaslangic() != null);
        if (!planVar) {
            return Optional.empty();
        }
        LocalDate tarih = sonPlanTarihi != null ? sonPlanTarihi : planliTarih();
        return Optional.of(ameliyatlariPlanla(tarih));
    }

    private LocalDate planliTarih() {
        return veri.getAmeliyatlar().stream()
                .filter(a -> a.getBaslangic() != null)
                .map(a -> a.getBaslangic().toLocalDate())
                .findFirst()
                .orElse(LocalDate.now(saat));
    }

    // ---------------- Strateji ----------------

    public void stratejiDegistir(OncelikStratejisi yeniStrateji) {
        this.strateji = yeniStrateji;
    }

    public OncelikStratejisi getStrateji() {
        return strateji;
    }

    // ---------------- Sorgular ----------------

    public Optional<Hasta> hastaBul(String id) {
        return veri.getHastalar().stream().filter(h -> h.getId().equalsIgnoreCase(id)).findFirst();
    }

    public Optional<Doktor> doktorBul(String id) {
        return veri.getDoktorlar().stream().filter(d -> d.getId().equalsIgnoreCase(id)).findFirst();
    }

    public Optional<Ameliyathane> ameliyathaneBul(String kod) {
        return veri.getAmeliyathaneler().stream().filter(s -> s.getKod().equalsIgnoreCase(kod.trim())).findFirst();
    }

    public List<Hasta> getHastalar() {
        return Collections.unmodifiableList(veri.getHastalar());
    }

    public List<Doktor> getDoktorlar() {
        return Collections.unmodifiableList(veri.getDoktorlar());
    }

    public List<Ameliyathane> getAmeliyathaneler() {
        return Collections.unmodifiableList(veri.getAmeliyathaneler());
    }

    public List<Ameliyat> getAmeliyatlar() {
        return Collections.unmodifiableList(veri.getAmeliyatlar());
    }

    public boolean veriBosMu() {
        return veri.getHastalar().isEmpty() && veri.getDoktorlar().isEmpty()
                && veri.getAmeliyathaneler().isEmpty() && veri.getAmeliyatlar().isEmpty();
    }

    public LocalDateTime simdi() {
        return LocalDateTime.now(saat);
    }

    private void kaydet() {
        depo.kaydet(veri);
    }
}
