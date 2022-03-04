package luka.mapper;

import luka.mapper.converter.Converter;
import luka.mapper.deconverter.Deconverter;
import ru.hse.homework4.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Optional;

public class LuluMapper implements Mapper {

    private boolean retainIdentity;

    public LuluMapper() {
        retainIdentity = false;
    }

    public LuluMapper(boolean retainIdentity) {
        this.retainIdentity = retainIdentity;
    }

    public void setRetainIdentity(boolean retainIdentity) {
        this.retainIdentity = retainIdentity;
    }

    /**
     * Читает сохранённый экземпляр класса {@code clazz} из строки {@code input}
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * String input = """
     *
     * {"comment":"Хорошая работа","resolved":false}""";
     *
     * ReviewComment reviewComment =
     * mapper.readFromString(ReviewComment.class, input);
     *
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz класс, сохранённый экземпляр которого находится в {@code input}
     * @param input строковое представление сохранённого экземпляра класса {@code
     *              clazz}
     * @return восстановленный экземпляр {@code clazz}
     */
    @Override
    public <T> T readFromString(Class<T> clazz, String input) {
        if (retainIdentity) {
            return Deconverter.getObjectFromString(clazz, input, new IdentityHashMap<String, Object>());
        }
        return Deconverter.getObjectFromString(clazz, input);
    }

    /**
     * Читает объект класса {@code clazz} из {@code InputStream}'а
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Данный метод закрывает {@code inputStream}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * String input = """
     *
     * {"comment":"Хорошая работа","resolved":false}""";
     *
     * ReviewComment reviewComment = mapper.read(ReviewComment.class,
     *
     * new
     * ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
     *
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz       класс, сохранённый экземпляр которого находится в {@code
     *                    inputStream}
     * @param inputStream поток ввода, содержащий строку в {@link
     *                    StandardCharsets#UTF_8} кодировке
     *                    5* @param <T> возвращаемый тип метода
     * @return восстановленный экземпляр класса {@code clazz}
     * @throws IOException в случае ошибки ввода-вывода
     */
    @Override
    public <T> T read(Class<T> clazz, InputStream inputStream) throws IOException {
        var inputStr = Arrays.toString(inputStream.readAllBytes());
        return readFromString(clazz, inputStr);
    }

    /**
     * Читает сохранённое представление экземпляра класса {@code clazz} из {@code
     * File}'а
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * ReviewComment reviewComment = mapper.read(ReviewComment.class, new
     * File("/tmp/review"));
     *
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz класс, сохранённый экземпляр которого находится в файле
     * @param file  файл, содержимое которого - строковое представление экземпляра
     *              {@code clazz}
     *              в {@link StandardCharsets#UTF_8} кодировке
     * @return восстановленный экземпляр {@code clazz}
     * @throws IOException в случае ошибки ввода-вывода
     */
    @Override
    public <T> T read(Class<T> clazz, File file) throws IOException {
        try (var stream = new FileInputStream(file)) {
            return read(clazz, stream);
        }
    }

    /**
     * Сохраняет {@code object} в строку
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * ReviewComment reviewComment = new ReviewComment();
     *
     * reviewComment.setComment("Хорошая работа");
     *
     * reviewComment.setResolved(false);
     *
     *
     * String string = mapper.writeToString(reviewComment);
     *
     * System.out.println(string);
     * </pre>
     *
     * @param object объект для сохранения
     * @return строковое представление объекта в выбранном формате
     */
    @Override
    public String writeToString(Object object) {

        var constructors = object.getClass().getConstructors();
        if (Deconverter.checkPublicClearConstructor(constructors)) {
            return Converter.objectToJson(object);
        }
        return "";
    }

    /**
     * Сохраняет {@code object} в {@link OutputStream}.
     * <p>
     * 6* То есть после вызова этого метода в {@link OutputStream} должны оказаться
     * байты, соответствующие строковому
     * представлению {@code object}'а в кодировке {@link
     * StandardCharsets#UTF_8}
     * <p>
     * Данный метод закрывает {@code outputStream}
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * ReviewComment reviewComment = new ReviewComment();
     *
     * reviewComment.setComment("Хорошая работа");
     *
     * reviewComment.setResolved(false);
     *
     *
     * mapper.write(reviewComment, new FileOutputStream("/tmp/review"));
     * </pre>
     *
     * @param object       объект для сохранения
     * @param outputStream
     * @throws IOException в случае ошибки ввода-вывода
     */
    @Override
    public void write(Object object, OutputStream outputStream) throws IOException {
        outputStream.write(writeToString(object).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Сохраняет {@code object} в {@link File}.
     * <p>
     * То есть после вызова этого метода в {@link File} должны оказаться байты,
     * соответствующие строковому
     * представлению {@code object}'а в кодировке {@link
     * StandardCharsets#UTF_8}
     * <p>
     * Данный метод закрывает {@code outputStream}
     * <p>
     * Пример вызова:
     *
     * <pre>
     *
     * ReviewComment reviewComment = new ReviewComment();
     *
     * reviewComment.setComment("Хорошая работа");
     *
     * reviewComment.setResolved(false);
     *
     *
     * mapper.write(reviewComment, new File("/tmp/review"));
     * </pre>
     *
     * @param object объект для сохранения
     * @param file
     * @throws IOException в случае ошибки ввода-вывода
     */
    @Override
    public void write(Object object, File file) throws IOException {
        try (var stream = new FileOutputStream(file)) {
            write(object, stream);
        }
    }

}
