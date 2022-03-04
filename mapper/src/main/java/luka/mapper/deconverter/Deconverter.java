package luka.mapper.deconverter;

import luka.mapper.converter.Converter;
import luka.mapper.deconverter.jsonNode.JsonNode;
import ru.hse.homework4.DateFormat;
import ru.hse.homework4.Exported;
import ru.hse.homework4.Ignored;
import ru.hse.homework4.PropertyName;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Deconverter {

    /**
     * Method which convert string into object.
     *
     * @param clazz object class.
     * @param input object in string format.
     * */
    public static <T> T getObjectFromString(Class<T> clazz, String input) {
        var clearObject = getClearObject(clazz);
        if (clearObject == null) {
            return null;
        }
        setFields(clearObject, input);
        return clearObject;
    }

    /**
     * Method which convert string into object.
     *
     * @param clazz object class.
     * @param input object in string format.
     * @param strObj HashMap for retainIdentity condition.
     * */
    public static <T> T getObjectFromString(Class<T> clazz, String input, IdentityHashMap<String, Object> strObj) {
        var clearObject = getClearObject(clazz);
        if (clearObject == null) {
            return null;
        }
        strObj.put(input, clearObject);
        // setFields(clearObject, input);
        return clearObject;
    }

    /**
     * Method for setting fields.
     *
     * @param object object e=which fields should be set.
     * @param fieldsString object in string format.
     * */
    public static void setFields(Object object, String fieldsString) {
        if (!object.getClass().isAnnotationPresent(Exported.class)) {
            return;
        }
        if (fieldsString.charAt(0) != '{' || fieldsString.charAt(fieldsString.length() - 1) != '}') {
            // TODO throw smth.
        }

        var content = getStringWithoutBorders(fieldsString, '{', '}');

        var clazz = object.getClass();

        // Filter all class fields.
        var classFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(elem -> !elem.isSynthetic()
                        && !elem.isAnnotationPresent(Ignored.class)
                        && !Modifier.isStatic(elem.getModifiers()))
                .toList();

        var strFields = content.split(",");
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

    /**
     * Method which returns value of field.
     *
     * @param field field.
     * @param type type of object
     * */
    public static Object getValueFromString(Field field, Class<?> type, String value) {
        field.setAccessible(true);
        if (Objects.equals(value, "null")) {
            if (type.isPrimitive()) {
                // TODO throw smth.
            }
            return null;
        }

        if (String.class.equals(type) || type.isPrimitive() || Converter.isWrapper(type)) {
            return getEasyValue(type, value);
        } else if (LocalDate.class.equals(type) || LocalTime.class.equals(type) || LocalDateTime.class.equals(type)) {
            return getDateValue(field, type, value);
        } else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            return getCollectionValue(field, value);
        } else if (type.isEnum()) {
            return getEnumValue(type, value);
        } else {
            return getObjectFromString(type, value);
        }
    }

    /**
     * Method for getting values of List or Set from String.
     *
     * @param field field.
     * @param value value in string format.
     */
    public static Collection<?> getCollectionValue(Field field, String value) {
        var type = field.getType();
        var elemType = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
        // TODO exported checking.
        ArrayList<Object> result = new ArrayList<>();

        var elementsString = getStringWithoutBorders(value, '[', ']');
        var elements = elementsString.split(",");

        for (var element : elements) {
            var elem = getValueFromString(field, elemType, element);
            result.add(elem);
        }

        if (Set.class.isAssignableFrom(type)) {
            return Set.copyOf(result);
        } else if (List.class.isAssignableFrom(type)) {
            return result.stream().toList();
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
        var content = getStringWithoutBorders(value, '\"', '\"');

        if (field.isAnnotationPresent(DateFormat.class)) {
            var annotation = field.getAnnotation(DateFormat.class);

            var formatter = DateTimeFormatter.ofPattern(annotation.value());
            if (LocalDate.class.equals(type)) {
                return LocalDate.parse(content, formatter);
            } else if (LocalTime.class.equals(type)) {
                return LocalTime.parse(content, formatter);
            } else if (LocalDateTime.class.equals(type)) {
                return LocalDateTime.parse(content, formatter);
            }
        } else {
            if (LocalDate.class.equals(type)) {
                return LocalDate.parse(content);
            } else if (LocalTime.class.equals(type)) {
                return LocalTime.parse(content);
            } else if (LocalDateTime.class.equals(type)) {
                return LocalDateTime.parse(content);
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

        var content = getStringWithoutBorders(value, '\"', '\"');

        var constants = type.getEnumConstants();
        // Find enum constant with such value.
        var constant = Arrays
                .stream(constants)
                .filter(elem -> elem.toString().equals(content)).findFirst();
        return (Enum<?>) constant.orElse(null);
    }

    /**
     * Method for getting values of string, primitive and wrapper from string.
     *
     * @param type  field value
     * @param value value in string format.
     */
    public static Object getEasyValue(Class<?> type, String value) {
        var content = getStringWithoutBorders(value, '\"', '\"');
        return toObject(type, content);
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

    /**
     * Method which remove two symbols in the beginning and end of string.
     *
     * @param str original string.
     * @param leftBorder first character which should be removed.
     * @param rightBorder last character which should be removed.
     * */
    public static String getStringWithoutBorders(String str, Character leftBorder, Character rightBorder) {
        var left = str.indexOf(leftBorder);
        var right = str.lastIndexOf(rightBorder);
        if (left == -1 || right == -1) {
            // TODO throw smth.
        }
        return str.substring(left + 1, right);
    }

    /**
     * Check if class has standard constructor without parameters.
     *
     * @param constructors array of constructors.
     * */
    public static boolean checkPublicClearConstructor(Constructor<?>[] constructors) {
        for (var constructor : constructors) {
            if (constructor.getParameterCount() == 0 && Modifier.isPublic(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method which creates clear object of T class.
     *
     * @param clazz class of object.
     *
     * @return object which was created with public constructor without parameters <p>
     * or return null if no such constructor or class was not marked as Exported.
     * */
    public static <T> T getClearObject(Class<T> clazz) {
        T clearObject;
        var constructors = clazz.getConstructors();
        if (checkPublicClearConstructor(constructors)) {
            try {
                var constructor = clazz.getConstructor();
                // Crate clear object.
                clearObject = constructor.newInstance();
                if (!clearObject.getClass().isAnnotationPresent(Exported.class)) {
                    return null;
                }
                return clearObject;
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
            }
        }
        return null;
    }




    public static ArrayList<JsonNode> getJsonNodes(String input) {
        int left = 1;
        int right = input.length() - 1;
        var inBuilder = new StringBuilder(input);
        ArrayList<JsonNode> result = new ArrayList<>();
        while (left < right) {
            // Index of : .
            int fieldMid = inBuilder.indexOf(":" ,left);
            if (fieldMid == -1) {
                return  result;
            }

            var node = new JsonNode();
            if (inBuilder.charAt(left) == ',') {
                left++;
            }
            if (inBuilder.charAt(left) == ' ') {
                left++;
            }
            node.name = inBuilder.substring(left, fieldMid);

            fieldMid++;
            if (inBuilder.charAt(fieldMid) == ' ') {
                fieldMid++;
            }
            int rightB = findRightBorder(inBuilder, fieldMid, right);
            node.value = inBuilder.substring(fieldMid, rightB);

            left = rightB;
            result.add(node);
        }
        return result;
    }

    public static Integer findRightBorder(StringBuilder inBuilder, int left, int right) {
        Stack<Character> borders = new Stack<>();
        char leftChar = inBuilder.charAt(left);
        while (left < right) {
            if (leftChar == '\"' || leftChar == '{' || leftChar == '[' || leftChar == 'n') {
                break;
            }
            left++;
            leftChar = inBuilder.charAt(left);
        }

        if (leftChar == 'n') {
            return left + 4;
        }
        left++;
        borders.push(leftChar);
        char currChar;

        while (left < right && !borders.empty()) {
            currChar = inBuilder.charAt(left);
            if (currChar == '}') {
                if (borders.peek() == '{') {
                    borders.pop();
                } else {
                    return -1;
                }
            } else if (currChar == ']') {
                if (borders.peek() == '[') {
                    borders.pop();
                } else {
                    return -1;
                }
            } else if (currChar == '\"') {
                if (borders.peek() == '\"') {
                    borders.pop();
                } else {
                    borders.push(currChar);
                }
            } else {
                if (currChar == '{' || currChar == '[')
                borders.push(currChar);
            }
            left++;
        }
        return left;
    }
}
