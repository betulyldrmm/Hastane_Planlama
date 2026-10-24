package tr.edu.klu.hastane.depo;

/** Interface Segregation: Yalnızca yazma yeteneği. */
public interface VeriYazici {
    void kaydet(HastaneVerisi veri);
}
