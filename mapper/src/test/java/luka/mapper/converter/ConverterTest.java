package luka.mapper.converter;

import luka.mapper.LuluMapper;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void fieldNameToJsonString() {

        var person = new Person("Mark", 32);

        try {
            var jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("lastName"));
            assertEquals("\"surname\"", jsonString);

            jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("name"));
            assertEquals("\"name\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void dateToJson() {
        var person = new Person("Mark", 32);
        person.date = LocalDateTime.of(2001, 12, 13, 23, 59, 59);

        try {
            var field = Person.class.getDeclaredField("date");
            var jsonString = Converter.dateToJson(person, field);
            assertEquals("\"myLocalDateTime\": \"2001-12-13 23:59:59\"", jsonString);

        } catch (NoSuchFieldException e) {
            fail();
        }
    }

}