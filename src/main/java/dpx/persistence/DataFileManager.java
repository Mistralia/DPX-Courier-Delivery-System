package dpx.persistence;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

public class DataFileManager {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/dpx-data.json";

    private final Gson gson;

    public DataFileManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public void saveData(DpxData data) {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(DATA_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + DATA_FILE, e);
        }
    }

    public DpxData loadData() {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(DATA_FILE), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, DpxData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + DATA_FILE, e);
        }
    }

    public boolean dataFileExists() {
        return new File(DATA_FILE).exists();
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(formatter));
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString(), formatter);
        }
    }
}
