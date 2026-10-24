package tr.edu.klu.hastane.depo;

/** Interface Segregation: Yalnızca okuma yeteneği gereken sınıflar bu küçük arayüze bağlanır. */
public interface VeriOkuyucu {
    HastaneVerisi yukle();
}
