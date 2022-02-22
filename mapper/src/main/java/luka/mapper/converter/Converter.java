package luka.mapper.converter;

import ru.hse.homework4.DateFormat;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class Converter {
    /**
     * Convert field name to json string.
     *
     * @param field - object of {@link Field} which should be converted into json name.
     * */
    public static String fieldNameToJson(Field field) {
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

    /**
     * This Function converts date to json string.
     *
     *
     * @param object - {@link LocalDate}, {@link LocalTime} or {@link LocalDateTime} object.
     * @param field - object of {@link Field} with date.
     *
     * @return String which contains field of date in Json format.
     * */
    public static String dateToJson(Object object, Field field) {
        var result = new StringBuilder("");
        result.append(String.format("%s: \"%s\"", fieldNameToJson(field), dateValueToJson(object, field)));
        return result.toString();
    }

    /**
     * Convert field value to Json format.
     * */
    public static String fieldValueToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");

        try {
            // Primitive, wrapper or String.
            if (field.getType() == String.class || field.getType().isPrimitive() || isWrapper(field.getType())) {
                field.setAccessible(true);
                // Get value in string format.
                var value = field.get(object).toString();
                result.append(String.format("\"%s\"", value));
            } else {
                var type = field.getType();

                // LocalDate, LocalTime or LocalDateTime.
                if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
                    result.append(Converter.dateValueToJson(object, field));
                } else if(List.class.equals(type) || Set.class.equals(type)) {

                }

                // Collection, another big class.
            }
        } catch (IllegalAccessException ignored) {
        }

        return result.toString();
    }

    /**
     * Convert date value to Json format.
     * */
    public static String dateValueToJson(Object object, Field field) {
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

            result.append(String.format("\"%s\"", stringDate));
        } catch (IllegalAccessException ignored) {
            // TODO maybe add smth.
        }

        return result.toString();
    }


    /**
     * Method which check if class is a wrapper of primitive.
     * */
    private static boolean isWrapper(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        return clazz == Boolean.class || clazz == Character.class ||
                clazz == Byte.class || clazz == Short.class ||
                clazz == Integer.class || clazz == Long.class ||
                clazz == Float.class || clazz == Double.class;
    }
}
