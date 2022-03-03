package luka.mapper.deconverter;

import luka.mapper.converter.Converter;
import ru.hse.homework4.DateFormat;
import ru.hse.homework4.Exported;
import ru.hse.homework4.Ignored;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Deconverter {

    public static <T> void getObjectFromString(T clearObject, String string) {
        if (!clearObject.getClass().isAnnotationPresent(Exported.class)) {
            return;
        }
        setFields(clearObject, string);
    }

    public static void setFields(Object object, String fieldsString) {
        if (!object.getClass().isAnnotationPresent(Exported.class)) {
            return;
        }
        if (fieldsString.charAt(0) != '{' || fieldsString.charAt(fieldsString.length() - 1) != '}') {
            // TODO throw smth.
        }
        var strBuilder = new StringBuilder(fieldsString);
        removeFirstAndLast(strBuilder);

        var clazz = object.getClass();

        // Filter all class fields.
        var classFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(elem -> !elem.isSynthetic()
                        && !elem.isAnnotationPresent(Ignored.class)
                        && !Modifier.isStatic(elem.getModifiers()))
                .toList();

        var strFields = strBuilder.toString().split(",");
        for (var strField : strFields) {
            // Get array where 0 elem is field name
            // and 1 elem it's value in string format.
            var nameVal = strField.split(": ");
            var fieldName = new StringBuilder(nameVal[0]);
            // Remove " and ".
            fieldName.deleteCharAt(0);
            fieldName.deleteCharAt(fieldName.length() - 1);
            // Find field with this name or propertyName annotation.
            var optField = classFields
                    .stream()
                    .filter(elem ->
                            elem.getName().equals(fieldName.toString())
                                    || (elem.isAnnotationPresent(PropertyName.class)
                                    && elem.getAnnotation(PropertyName.class).value().equals(fieldName.toString())))
                    .findFirst();
            Field field;
            if (optField.isPresent()) {
                field = optField.get();
            } else {
                continue;
            }
            // Set field value.
            try {
                field.set(object, getValueFromString(field, field.getType(), nameVal[1]));
            } catch (IllegalAccessException e) {

            }
        }
    }

    public static Object getValueFromString(Field field, Class<?> type, String value) {
        field.setAccessible(true);

        if (String.class.equals(type) || type.isPrimitive() || Converter.isWrapper(type)) {
            return getEasyValue(field.getType(), value);
        } else if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
            return getDateValue(field, field.getType(), value);
        } else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            return getCollectionValue(field, value);
        } else if (type.isEnum()) {
            return getEnumValue(field.getType(), value);
        } else {

        }
        return null;
    }


    /**
     * Method for getting values of List or Set from String.
     *
     * @param field field.
     * @param value value in string format.
     */
    public static Object getCollectionValue(Field field, String value) {
        // TODO check first and last symbol.
        var type = field.getType();
        var elemType = field.getGenericType().getClass();
        // TODO exported checking.
        ArrayList<Object> result = new ArrayList<>();

        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);

        var elements = valBuilder.toString().split(",");

        for (var element : elements) {
            result.add(getValueFromString(field, elemType, element));
        }

        if (Set.class.isAssignableFrom(type)) {
            return Set.of(result);
        } else if (List.class.isAssignableFrom(type)) {
            return List.of(result);
        }
        return null;
    }

    /**
     * Method for getting values of LocalDate, LocalTime or LocalDateTime from string.
     *
     * @param field field.
     * @param type  type of field.
     * @param value value in string format.
     */
    public static Object getDateValue(Field field, Class<?> type, String value) {
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);

        if (field.isAnnotationPresent(DateFormat.class)) {
            var annotation = field.getAnnotation(DateFormat.class);

            var formatter = DateTimeFormatter.ofPattern(annotation.value());
            if (LocalDate.class.equals(type)) {
                return LocalDate.parse(valBuilder.toString(), formatter);
            } else if (LocalTime.class.equals(type)) {
                return LocalTime.parse(valBuilder.toString(), formatter);
            } else if (LocalDateTime.class.equals(type)) {
                return LocalDateTime.parse(valBuilder.toString(), formatter);
            }
        } else {
            if (LocalDate.class.equals(type)) {
                return LocalDate.parse(valBuilder.toString());
            } else if (LocalTime.class.equals(type)) {
                return LocalTime.parse(valBuilder.toString());
            } else if (LocalDateTime.class.equals(type)) {
                return LocalDateTime.parse(valBuilder.toString());
            }
        }
        return null;
    }

    /**
     * Method for getting enum values from string.
     *
     * @param type  field type.
     * @param value value in string format.
     */
    public static Enum<?> getEnumValue(Class<?> type, String value) {
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        var constants = type.getEnumConstants();
        // Find enum constant with such value.
        var constant = Arrays
                .stream(constants)
                .filter(elem -> elem.toString().equals(valBuilder.toString())).findFirst();
        return (Enum<?>) constant.orElse(null);
    }

    /**
     * Method for getting values of string, primitive and wrapper from string.
     *
     * @param type  field value
     * @param value value in string format.
     */
    public static Object getEasyValue(Class<?> type, String value) {
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        return toObject(type, valBuilder.toString());

    }

    /**
     * Convert string into primitives, wrappers.
     *
     * @param clazz output object type.
     * @param value value in string format.
     */
    public static Object toObject(Class<?> clazz, String value) {
        if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return Boolean.parseBoolean(value);
        }
        if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
            return Byte.parseByte(value);
        }
        if (Short.class.equals(clazz) || short.class.equals(clazz)) {
            return Short.parseShort(value);
        }
        if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return Integer.parseInt(value);
        }
        if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            return Long.parseLong(value);
        }
        if (Float.class.equals(clazz) || float.class.equals(clazz)) {
            return Float.parseFloat(value);
        }
        if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            return Double.parseDouble(value);
        }
        return value;
    }

    private static void removeFirstAndLast(StringBuilder stringBuilder) {
        stringBuilder.deleteCharAt(0);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }
}
