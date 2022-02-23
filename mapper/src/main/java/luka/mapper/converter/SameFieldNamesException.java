package luka.mapper.converter;

import java.lang.reflect.Field;

/**
 * Exception which throws if class fields have same names <p>
 * or {@link ru.hse.homework4.PropertyName} value.
 *
 * */
public class SameFieldNamesException extends RuntimeException{

    Field field;

    /**
     *
     * @param message exception message.
     * @param field field which has used name.
     * */
    public SameFieldNamesException(String message, Field field) {
        super(message);
        this.field = field;
    }
}
