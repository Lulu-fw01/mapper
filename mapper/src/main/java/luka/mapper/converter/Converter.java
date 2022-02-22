package luka.mapper.converter;

import ru.hse.homework4.DateFormat;
import ru.hse.homework4.Exported;
import ru.hse.homework4.Ignored;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Converter {


    /**
     * Convert object to Json string.
     *
     * */
    public static String objectToJson(Object object) {
        if (object == null || !object.getClass().isAnnotationPresent(Exported.class)) {
            return "";
        }

        StringBuilder result = new StringBuilder("{");

        var objClass = object.getClass();
        var objFields = objClass.getFields();
        for (int i = 0; i < objFields.length; ++i) {
            if (!objFields[i].isAnnotationPresent(Ignored.class)) {
                var jsonString = fieldToJson(object, objFields[i]);
                result.append(String.format("%s%s", jsonString, i == objFields.length - 1 ? "" : ", "));
            }
        }

        result.append("}");
        return result.toString();
    }

    /**
     * Convert field to Json string.
     * */
    public static String fieldToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");

        result.append(String.format("%s: %s", Converter.fieldNameToJson(field), Converter.fieldValueToJson(object, field)));

        return result.toString();
    }

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
     * Convert field value to Json format.
     * */
    public static String fieldValueToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");

        try {
            field.setAccessible(true);
            // Primitive, wrapper or String.
            if (field.getType() == String.class || field.getType().isPrimitive() || isWrapper(field.getType())) {
                // Get value in string format.
                var value = field.get(object).toString();
                result.append(String.format("\"%s\"", value));
            } else {
                var type = field.getType();

                // LocalDate, LocalTime or LocalDateTime.
                if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
                    result.append(dateValueToJson(object, field));
                } else if(List.class.equals(type) || Set.class.equals(type)) {

                } else {
                    // TODO class
                }
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
     * Convert collection to Json.
     * */
    public static String collectionValueToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder(Converter.fieldNameToJson(field) + "[");

        Collection<?> collection;
        try {
            var value = field.get(object);
            var type = field.getType();
            if (List.class.equals(type)) {
                collection = (List<?>) object;
            } else if (Set.class.equals(type)) {
                collection = (Set<?>) object;
            } else {
                // TODO maybe need check.
                return "[]";
            }

            for (int i = 0; i < collection.size(); ++i) {
                // TODO ended here.
                //result.append(String.format("%s%s", fieldValueToJson(), i == collection.size() - 1 ? "" : ", "));
            }
        } catch(IllegalAccessException ignored) {
            // TODO maybe need smth.
        }

        result.append("]");
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
