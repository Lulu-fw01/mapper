package luka.mapper.converter;

import ru.hse.homework4.DateFormat;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converter {
    /**
     * Convert field name to json string.
     *
     * @param field - object of {@link Field} which should be converted into json name.
     * */
    public static String fieldNameToJsonString(Field field) {
        StringBuilder result = new StringBuilder("\"");

        if (field.isAnnotationPresent(PropertyName.class)) {
            var annotation = field.getAnnotation(PropertyName.class);
            // TODO maybe need check value in PropertyName.
            result.append(String.format("%s\"", annotation.value()));
        } else {
            result.append(String.format("%s\"", field.getName()));
        }
        return result.toString();
    }

    /*
    String collectionToString() {

    }

    String classToString(Object object) {

    }
    */

    /**
     * This Function converts date to json string.
     *
     *
     * @param object - {@link LocalDate}, {@link LocalTime} or {@link LocalDateTime} object.
     * @param field - object of {@link Field} with date.
     *
     * @return String which contains field of date in Json format.
     * */
    public static String dateToJsonString(Object object, Field field) {
        // TODO add comments.
        StringBuilder result = new StringBuilder("");
        field.setAccessible(true);
        try {
            var value = field.get(object);
            StringBuilder stringDate = new StringBuilder("");
            if (field.isAnnotationPresent(DateFormat.class)) {
                var annotation = field.getAnnotation(DateFormat.class);
                // TODO here can be exceptions.
                var formatter = DateTimeFormatter.ofPattern(annotation.value());

                if (LocalDate.class.equals(value.getClass())) {
                    var date = (LocalDate) value;
                    stringDate.append(date.format(formatter));
                } else if (LocalTime.class.equals(value.getClass())) {
                    var date = (LocalTime) value;
                    stringDate.append(date.format(formatter));
                } else if (LocalDateTime.class.equals(value.getClass())) {
                    var date = (LocalDateTime) value;
                    stringDate.append(date.format(formatter));
                }
                // TODO check if not correct argument.
            } else {
                stringDate.append(value.toString());
            }

            result.append(String.format("%s: \"%s\"", fieldNameToJsonString(field), stringDate));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
