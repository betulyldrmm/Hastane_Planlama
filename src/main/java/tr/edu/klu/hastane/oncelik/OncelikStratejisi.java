package tr.edu.klu.hastane.oncelik;

import tr.edu.klu.hastane.model.Hasta;

import java.time.LocalDateTime;

/**
 * STRATEJİ TASARIM KALIBI: Hasta öncelik puanının nasıl hesaplanacağını tanımlar.
 * Yeni bir önceliklendirme kuralı eklemek için mevcut kodu değiştirmeden bu arayüzü
 * uygulayan yeni bir sınıf yazmak yeterlidir (Open/Closed prensibi).
 */
public interface OncelikStratejisi {

    /** Verilen anda hastanın öncelik puanını hesaplar. Puan büyüdükçe öncelik artar. */
    int puanHesapla(Hasta hasta, LocalDateTime simdi);

    /** Stratejinin kullanıcıya gösterilecek adı. */
    String ad();
}
