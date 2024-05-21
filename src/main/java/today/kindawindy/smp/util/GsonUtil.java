package today.kindawindy.smp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;

@UtilityClass
public class GsonUtil {

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .create();

    public String to(Object object) {
        return gson.toJson(object);
    }

    public <T> T from(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public <T> T from(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public Gson get() {
        return gson;
    }
}