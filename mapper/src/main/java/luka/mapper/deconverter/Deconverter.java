package luka.mapper.deconverter;

import ru.hse.homework4.Exported;

import java.util.Arrays;

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
        var classFields = Arrays.stream(clazz.getDeclaredFields()).filter(elem -> )

        var strFields = strBuilder.toString().split(",");
        for (var strField : strFields) {

        }

        var fields =
    }



}
