package tr.edu.klu.hastane.depo;

/**
 * DEPO (REPOSITORY) TASARIM KALIBI: Verinin nerede ve nasıl saklandığını (JSON dosyası,
 * bellek, ileride veritabanı) iş mantığından gizler. Üst katmanlar yalnızca bu arayüzü bilir
 * (Dependency Inversion prensibi).
 */
public interface VeriDeposu extends VeriOkuyucu, VeriYazici {
}
