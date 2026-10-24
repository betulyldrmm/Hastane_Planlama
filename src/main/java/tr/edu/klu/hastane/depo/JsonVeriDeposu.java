package tr.edu.klu.hastane.depo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import tr.edu.klu.hastane.model.AcilHasta;
import tr.edu.klu.hastane.model.Hasta;
import tr.edu.klu.hastane.model.PoliklinikHasta;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Tüm hastane verisini okunabilir bir JSON dosyasında kalıcı olarak saklar.
 * Hasta soyut olduğu için JSON'a "tur" alanı yazılır; okurken bu alana bakılarak
 * doğru alt sınıf (AcilHasta / PoliklinikHasta) oluşturulur.
 */
public class JsonVeriDeposu implements VeriDeposu {

    private static final String TUR_ALANI = "tur";

    private final Path dosyaYolu;
    private final Gson gson;

    public JsonVeriDeposu(Path dosyaYolu) {
        this.dosyaYolu = dosyaYolu;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new TarihAdaptoru().nullSafe())
                .registerTypeAdapter(Hasta.class, new HastaAdaptoru())
                .create();
    }

    @Override
    public HastaneVerisi yukle() {
        if (!Files.exists(dosyaYolu)) {
            return new HastaneVerisi();
        }
        try (Reader okuyucu = Files.newBufferedReader(dosyaYolu, StandardCharsets.UTF_8)) {
            HastaneVerisi veri = gson.fromJson(okuyucu, HastaneVerisi.class);
            return veri != null ? veri : new HastaneVerisi();
        } catch (IOException e) {
            throw new UncheckedIOException("Veri dosyası okunamadı: " + dosyaYolu, e);
        }
    }

    @Override
    public void kaydet(HastaneVerisi veri) {
        try {
            Path klasor = dosyaYolu.toAbsolutePath().getParent();
            if (klasor != null) {
                Files.createDirectories(klasor);
            }
            try (Writer yazici = Files.newBufferedWriter(dosyaYolu, StandardCharsets.UTF_8)) {
                gson.toJson(veri, yazici);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Veri dosyası yazılamadı: " + dosyaYolu, e);
        }
    }

    public Path getDosyaYolu() {
        return dosyaYolu;
    }

    /** LocalDateTime değerlerini ISO biçiminde metin olarak saklar (2026-10-23T09:30). */
    private static class TarihAdaptoru extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime deger) throws IOException {
            out.value(deger.toString());
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }

    /** Soyut Hasta sınıfının alt türlerini JSON'da ayırt eder. */
    private static class HastaAdaptoru implements JsonSerializer<Hasta>, JsonDeserializer<Hasta> {
        @Override
        public JsonElement serialize(Hasta hasta, Type tip, JsonSerializationContext baglam) {
            JsonObject nesne = baglam.serialize(hasta, hasta.getClass()).getAsJsonObject();
            nesne.addProperty(TUR_ALANI, hasta.getClass().getSimpleName());
            return nesne;
        }

        @Override
        public Hasta deserialize(JsonElement json, Type tip, JsonDeserializationContext baglam) {
            JsonObject nesne = json.getAsJsonObject();
            if (!nesne.has(TUR_ALANI)) {
                throw new JsonParseException("Hasta kaydında 'tur' alanı yok.");
            }
            String tur = nesne.get(TUR_ALANI).getAsString();
            if (tur.equals(AcilHasta.class.getSimpleName())) {
                return baglam.deserialize(nesne, AcilHasta.class);
            }
            if (tur.equals(PoliklinikHasta.class.getSimpleName())) {
                return baglam.deserialize(nesne, PoliklinikHasta.class);
            }
            throw new JsonParseException("Bilinmeyen hasta türü: " + tur);
        }
    }
}
