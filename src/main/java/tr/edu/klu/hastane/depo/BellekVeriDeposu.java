package tr.edu.klu.hastane.depo;

/** Veriyi yalnızca bellekte tutan depo. Birim testlerinde dosya sistemine dokunmamak için kullanılır. */
public class BellekVeriDeposu implements VeriDeposu {

    private HastaneVerisi veri = new HastaneVerisi();
    private int kayitSayisi = 0;

    @Override
    public HastaneVerisi yukle() {
        return veri;
    }

    @Override
    public void kaydet(HastaneVerisi veri) {
        this.veri = veri;
        kayitSayisi++;
    }

    public int getKayitSayisi() {
        return kayitSayisi;
    }
}
