package tr.edu.klu.hastane;

import tr.edu.klu.hastane.arayuz.KonsolArayuzu;
import tr.edu.klu.hastane.depo.JsonVeriDeposu;
import tr.edu.klu.hastane.depo.VeriDeposu;
import tr.edu.klu.hastane.oncelik.StandartOncelikStratejisi;
import tr.edu.klu.hastane.servis.HastaneYonetimi;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;

/**
 * Uygulamanın giriş noktası. Nesneler burada oluşturulup birbirine bağlanır
 * (bağımlılıklar dışarıdan verilir — Dependency Injection).
 */
public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Path veriDosyasi = Path.of(args.length > 0 ? args[0] : "veri/hastane.json");
        VeriDeposu depo = new JsonVeriDeposu(veriDosyasi);
        HastaneYonetimi yonetim = new HastaneYonetimi(depo, new StandartOncelikStratejisi(), Clock.systemDefaultZone());

        System.out.println("Veri dosyası: " + veriDosyasi.toAbsolutePath());
        Scanner girdi = new Scanner(System.in, StandardCharsets.UTF_8);
        new KonsolArayuzu(yonetim, girdi).baslat();
    }
}
