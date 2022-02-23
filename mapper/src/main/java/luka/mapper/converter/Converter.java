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
     * @param object - object that should be written as Json string.
     */
    public static String objectToJson(Object object) {
        if (object == null || !object.getClass().isAnnotationPresent(Exported.class)) {
            return "";
        }

        StringBuilder result = new StringBuilder("{");

        var objClass = object.getClass();
        var objFields = objClass.getFields();
        for (int i = 0; i < objFields.length; ++i) {
            if (!objFields[i].isAnnotationPresent(Ignored.class)) {
                objFields[i].setAccessible(true);
                Object fieldVal;
                try {
                    fieldVal = objFields[i].get(object);
                    var jsonString = fieldToJson(fieldVal, objFields[i]);
                    result.append(String.format("%s%s", jsonString, i == objFields.length - 1 ? "" : ", "));
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        result.append("}");
        return result.toString();
    }

    /**
     * Convert field to Json string.
     */
    public static String fieldToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");

        result.append(String.format("%s: %s", Converter.fieldNameToJson(field), fieldValueToJson(object, field)));

        return result.toString();
    }

    /**
     * Convert field name to json string.
     *
     * @param field - object of {@link Field} which should be converted into json name.
     */
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
     *
     * @param object object which value should be returned in Json format.
     * @param field  field which contains object from first param.
     */
    public static String fieldValueToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");

        var type = object.getClass();
        // Primitive, wrapper or String.
        if (String.class.equals(type) || type.isPrimitive() || isWrapper(type)) {
            // Get value in string format.
            var value = object.toString();
            result.append(String.format("\"%s\"", value));

        } else if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
            // LocalDate, LocalTime or LocalDateTime.
            result.append(dateValueToJson(object, field));
        } else if (object instanceof List<?> || object instanceof Set<?>) {
            Collection<?> collection;
            if (object instanceof List<?>) {
                collection = (List<?>) object;
            } else if (object instanceof Set<?>) {
                collection = (Set<?>) object;
            } else {
                // TODO throw smth.
                return "ex";
            }
            result.append(collectionValueToJson(collection, field));
        } else if (type.isEnum()) {
            return "enum";
            // TODO enum
        } else {
            if (!object.getClass().isAnnotationPresent(Exported.class)) {
                // TODO throw smth.
                return "class";
            }
            //TODO class
        }


        return result.toString();
    }

    /**
     * Convert date value to Json format.
     *
     * @param object object which value should be returned in Json format.
     * @param field  field which contains object from first param.
     */
    public static String dateValueToJson(Object object, Field field) {
        // TODO add comments.
        StringBuilder result = new StringBuilder("");
        field.setAccessible(true);

        StringBuilder stringDate = new StringBuilder("");
        if (field.isAnnotationPresent(DateFormat.class)) {
            var annotation = field.getAnnotation(DateFormat.class);
            // TODO here can be exceptions.
            var formatter = DateTimeFormatter.ofPattern(annotation.value());

            if (LocalDate.class.equals(object.getClass())) {
                var date = (LocalDate) object;
                stringDate.append(date.format(formatter));
            } else if (LocalTime.class.equals(object.getClass())) {
                var date = (LocalTime) object;
                stringDate.append(date.format(formatter));
            } else if (LocalDateTime.class.equals(object.getClass())) {
                var date = (LocalDateTime) object;
                stringDate.append(date.format(formatter));
            }
            // TODO check if not correct argument.
        } else {
            stringDate.append(object.toString());
        }

        result.append(String.format("\"%s\"", stringDate));

        return result.toString();
    }

    /**
     * Convert collection to Json.
     *
     * @param collection collection which should be returned in Json format.
     * @param field      field which contains object from first param.
     */
    public static String collectionValueToJson(Collection<?> collection, Field field) {
        StringBuilder result = new StringBuilder("[");
        var arr = collection.toArray();
        for (int i = 0; i < arr.length; ++i) {
            // TODO ended here.
            result.append(String.format("%s%s", fieldValueToJson(arr[i], field), i == arr.length - 1 ? "" : ", "));
        }

        result.append("]");
        return result.toString();
    }


    /**
     * Method which check if class is a wrapper of primitive.
     */
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
