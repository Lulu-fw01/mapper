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

        strBuilder.deleteCharAt(0);
        strBuilder.deleteCharAt(fieldsString.length() - 1);

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
            setField(object, field, nameVal[1]);
        }
    }

    public static void setField(Object object, Field field, String value) {
        var type = field.getType();
        field.setAccessible(true);

        if (String.class.equals(type) || type.isPrimitive() || Converter.isWrapper(type)) {
            setEasyField(object, field, value);
        } else if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
            setDateField(object, field, value);
        } else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            setCollectionField(object, field, value);
        } else if (type.isEnum()) {
            setEnumField(object, field, value);
        } else {

        }
    }


    public static void setCollectionField(Object object, Field field, String value) {
        var type = field.getType();
        var elemType = field.getGenericType();
        Collection collection;
        if (Set.class.isAssignableFrom(type)) {

        } else {

        }
    }

    /**
     * Method for setting class fields such as LocalDate, LocalTime and LocalDateTime.
     *
     * @param object object of which field should be set.
     * @param field  field.
     * @param value  value string format.
     */
    public static void setDateField(Object object, Field field, String value) {
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        var type = field.getType();
        try {
            if (field.isAnnotationPresent(DateFormat.class)) {
                var annotation = field.getAnnotation(DateFormat.class);

                var formatter = DateTimeFormatter.ofPattern(annotation.value());
                if (LocalDate.class.equals(type)) {
                    var date = LocalDate.parse(valBuilder.toString(), formatter);
                    field.set(object, date);
                } else if (LocalTime.class.equals(type)) {
                    var date = LocalTime.parse(valBuilder.toString(), formatter);
                    field.set(object, date);
                } else if (LocalDateTime.class.equals(type)) {
                    var date = LocalDateTime.parse(valBuilder.toString(), formatter);
                    field.set(object, date);
                }
            } else {
                if (LocalDate.class.equals(type)) {
                    var date = LocalDate.parse(valBuilder.toString());
                    field.set(object, date);
                } else if (LocalTime.class.equals(type)) {
                    var date = LocalTime.parse(valBuilder.toString());
                    field.set(object, date);
                } else if (LocalDateTime.class.equals(type)) {
                    var date = LocalDateTime.parse(valBuilder.toString());
                    field.set(object, date);
                }
            }
        } catch (IllegalAccessException ignored) {
            // TODO smth.
        }
    }

    /**
     * Method for setting enum fields.
     *
     * @param object object of which field should be set.
     * @param field  field.
     * @param value  value in string format.
     */
    public static void setEnumField(Object object, Field field, String value) {
        var type = field.getType();
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        var constants = type.getEnumConstants();
        // Find enum constant with such value.
        var constant = Arrays
                .stream(constants)
                .filter(elem -> elem.toString().equals(valBuilder.toString())).findFirst();
        if (constant.isPresent()) {
            try {
                field.set(object, constant.get());
            } catch (IllegalAccessException ex) {
                // TODO add smth.
            }
        }
    }

    /**
     * Method for setting fields such as string, primitive and wrapper.
     *
     * @param object object of which field should be set.
     * @param field  field.
     * @param value  in string format.
     */
    public static void setEasyField(Object object, Field field, String value) {
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        var type = field.getType();
        try {
            field.set(object, toObject(type, valBuilder.toString()));
        } catch (IllegalAccessException e) {

        }
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
