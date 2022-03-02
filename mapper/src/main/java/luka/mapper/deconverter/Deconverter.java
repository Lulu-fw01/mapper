package luka.mapper.deconverter;

import luka.mapper.converter.Converter;
import ru.hse.homework4.Exported;
import ru.hse.homework4.Ignored;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            var optField = classFields.stream()
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

        } else if (object instanceof List<?> || object instanceof Set<?>) {

        } else if (type.isEnum()) {

        } else {

        }
    }

    public static void setEnumField(Object object, Field field, String value) {
        var type = field.getType();
        var valBuilder = new StringBuilder(value);
        removeFirstAndLast(valBuilder);
        field.set(object, Enum.valueOf( type, valBuilder.toString()));
    }


    /**
     * Method for setting fields such as string, primitive and wrapper.
     *
     * @param object object of which field should be set.
     * @param field field.
     * @param value in string format.
     * */
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
     * */
    public static Object toObject(Class<?> clazz, String value ) {
        if( Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return Boolean.parseBoolean(value);
        }
        if( Byte.class.equals(clazz) || byte.class.equals(clazz)) {
            return Byte.parseByte(value);
        }
        if( Short.class.equals(clazz) || short.class.equals(clazz)) {
            return Short.parseShort(value);
        }
        if( Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return Integer.parseInt(value);
        }
        if( Long.class.equals(clazz) || long.class.equals(clazz)) {
            return Long.parseLong(value);
        }
        if( Float.class.equals(clazz) || float.class.equals(clazz)) {
            return Float.parseFloat(value);
        }
        if( Double.class.equals(clazz) || double.class.equals(clazz)) {
            return Double.parseDouble(value);
        }
        return value;
    }

    private static void removeFirstAndLast(StringBuilder stringBuilder) {
        stringBuilder.deleteCharAt(0);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }
}
