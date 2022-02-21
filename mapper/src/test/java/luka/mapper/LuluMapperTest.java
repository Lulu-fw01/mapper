package luka.mapper;

import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;
import ru.hse.homework4.Mapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LuluMapperTest {

    @Test
    void readFromString() {
    }

    @Test
    void read() {
    }

    @Test
    void testRead() {
    }

    @Test
    void writeToString() {
    }

    @Test
    void write() {
    }

    @Test
    void testWrite() {
    }

    @Test
    void fieldToJSONString() {
        var mapper = new LuluMapper();

        var person = new Person("Mark", 32);

        try {
            var jsonString = mapper.fieldToJsonString(person, person.getClass().getField("name"));
            assertEquals("\"name\": \"Mark\"", jsonString);

            jsonString = mapper.fieldToJsonString(person, person.getClass().getDeclaredField("age"));
            assertEquals("\"age\": \"32\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }

    }



}