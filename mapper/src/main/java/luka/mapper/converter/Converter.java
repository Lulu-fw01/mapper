package luka.mapper.converter;

import luka.mapper.deconverter.jsonNode.JsonNode;
import luka.mapper.exceptions.CycleException;
import luka.mapper.exceptions.SameFieldNamesException;
import ru.hse.homework4.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Converter {


    /**
     * Convert object to Json string.
     *
     * @param object - object that should be written as Json string.
     */
    public static String objectToJson(Object object) {
        // TODO Maybe change to ArrayList.
        HashSet<Object> usedClasses = new HashSet<>();
        return objectToJson(object, usedClasses);
    }

    /**
     * Convert object to Json string.
     *
     * @param object      object that should be written as Json string.
     * @param usedClasses set of classes which were used before.
     */
    public static String objectToJson(Object object, HashSet<Object> usedClasses) {
        // Maybe move this if.
        if (object == null || !object.getClass().isAnnotationPresent(Exported.class)) {
            return "";
        }
        if (object.getClass().isRecord()) {
            throw new UnsupportedOperationException("Record classes are not supported");
        }

        if (usedClasses.contains(object)) {
            throw new CycleException("Cycle was detected.", object);
        }

        HashSet<String> fieldNames = new HashSet<>();
        usedClasses.add(object);

        // Result string.
        StringBuilder result = new StringBuilder("{");
        var objClass = object.getClass();

        boolean includeNull = objClass.getAnnotation(Exported.class).nullHandling() == NullHandling.INCLUDE;

        var objFields = objClass.getDeclaredFields();
        for (int i = 0; i < objFields.length; ++i) {
            if (!objFields[i].isAnnotationPresent(Ignored.class) ||
                    !objFields[i].isSynthetic() ||
                    !Modifier.isStatic(objFields[i].getModifiers())) {

                objFields[i].setAccessible(true);
                Object fieldVal;
                try {
                    fieldVal = objFields[i].get(object);
                    var jsonString = fieldToJson(fieldVal, objFields[i], fieldNames, new HashSet<>(usedClasses), includeNull);
                    result.append(String.format("%s%s", jsonString, (i == objFields.length - 1 || Objects.equals(jsonString, "")) ? "" : ", "));
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        // Remove extra characters.
        if (result.charAt(result.length() - 2) == ',') {
            result.deleteCharAt(result.length() - 1);
            result.deleteCharAt(result.length() - 1);
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Convert field to Json string.
     *
     * @param object object which value should be returned in Json format.
     * @param field  field which contains object from first param.
     */
    public static String fieldToJson(Object object, Field field, HashSet<String> fieldNames, HashSet<Object> usedClasses, boolean includeNull) {

        if (object == null) {
            if (includeNull) {
                return String.format("%s: %s", Converter.fieldNameToJson(field, fieldNames), "null");
            } else {
                return "";
            }
        }
        return String.format("%s: %s", Converter.fieldNameToJson(field, fieldNames), fieldValueToJson(object, field, usedClasses));
    }

    /**
     * Convert field name to json string.
     *
     * @param field - object of {@link Field} which should be converted into Json name.
     * @throws SameFieldNamesException if field name has been used before in class.
     */
    public static String fieldNameToJson(Field field, HashSet<String> fieldNames) {
        StringBuilder result = new StringBuilder("\"");
        String name = "";
        if (field.isAnnotationPresent(PropertyName.class)) {
            name = field.getAnnotation(PropertyName.class).value();
            if (fieldNames.contains(name)) {
                // If this field name has been sed throw exception.
                throw new SameFieldNamesException(String.format("Property name \"%s\" has been already used.", name), field);
            }
        } else {
            name = field.getName();
            if (fieldNames.contains(name)) {
                // If this field name has been sed throw exception.
                throw new SameFieldNamesException(String.format("Field name \"%s\" has been already used.", name), field);
            }
        }

        fieldNames.add(name);
        result.append(String.format("%s\"", name));
        return result.toString();
    }

    /**
     * Convert field value to Json format.
     *
     * @param object object which value should be returned in Json format.
     * @param field  field which contains object from first param.
     */
    public static String fieldValueToJson(Object object, Field field, HashSet<Object> usedClasses) {
        StringBuilder result = new StringBuilder("");

        var type = object.getClass();

        if (String.class.equals(type) || type.isPrimitive() || isWrapper(type)) {
            // Primitive, wrapper or String.
            // Get value in string format.
            var value = object.toString();
            result.append(String.format("\"%s\"", value));

        } else if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
            // LocalDate, LocalTime or LocalDateTime.
            result.append(dateValueToJson(object, field));
        } else if (object instanceof List<?> || object instanceof Set<?>) {
            // Instance of List or Set.
            Collection<?> collection;
            if (object instanceof List<?>) {
                collection = (List<?>) object;
            } else {
                collection = (Set<?>) object;
            }
            result.append(collectionValueToJson(collection, field, new HashSet<>(usedClasses)));

        } else if (type.isEnum()) {
            // Enum.
            // TODO add tests.
            result.append(enumValueToJson(object));
        } else {
            result.append(objectToJson(object, new HashSet<>(usedClasses)));
        }

        return result.toString();
    }

    /**
     * Convert date value to Json format.
     *
     * @param object object which value should be returned in Json format.
     * @param field  field which contains object from first param.
     * @throws IllegalArgumentException if wrong date format.
     */
    public static String dateValueToJson(Object object, Field field) {
        StringBuilder result = new StringBuilder("");
        field.setAccessible(true);

        StringBuilder stringDate = new StringBuilder("");
        if (field.isAnnotationPresent(DateFormat.class)) {
            var annotation = field.getAnnotation(DateFormat.class);
            var formatter = DateTimeFormatter.ofPattern(annotation.value());
            // TODO add tests.
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
    public static String collectionValueToJson(Collection<?> collection, Field field, HashSet<Object> usedClasses) {

        if (usedClasses.contains(collection)) {
            throw new CycleException("Cycle was detected.", collection);
        }
        // TODO check if not correct type of collection elem.
        usedClasses.add(collection);
        StringBuilder result = new StringBuilder("[");
        var arr = collection.toArray();
        for (int i = 0; i < arr.length; ++i) {
            result.append(String.format("%s%s", fieldValueToJson(arr[i], field, new HashSet<>(usedClasses)), i == arr.length - 1 ? "" : ", "));
        }
        result.append("]");
        return result.toString();
    }

    /**
     * Convert enum value to Json format.
     *
     * @param object object which value should be returned in Json format.
     */
    public static String enumValueToJson(Object object) {
        StringBuilder result = new StringBuilder("");
        result.append(String.format("\"%s\"", object.toString()));
        return result.toString();
    }


    /**
     * Method which check if class is a wrapper of primitive.
     */
    public static boolean isWrapper(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        return clazz == Boolean.class || clazz == Character.class ||
                clazz == Byte.class || clazz == Short.class ||
                clazz == Integer.class || clazz == Long.class ||
                clazz == Float.class || clazz == Double.class;
    }
}
