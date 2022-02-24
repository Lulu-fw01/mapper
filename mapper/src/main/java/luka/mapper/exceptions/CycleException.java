package luka.mapper.exceptions;


/**
 * Exception which throws if object which should<p>
 * be serialized contains cycle.
 *
 * */
public class CycleException extends RuntimeException {

    Object object;

    public Object getObject() {
        return object;
    }

    /**
     * @param message message.
     * @param object which produces cycle.
     * */
    public CycleException(String message, Object object) {
        super(message);
        this.object = object;
    }
}
