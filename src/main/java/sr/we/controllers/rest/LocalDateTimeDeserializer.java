package sr.we.controllers.rest;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter;

    public LocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateString = json.getAsString();
        return LocalDateTime.parse(dateString, formatter);
    }
}
