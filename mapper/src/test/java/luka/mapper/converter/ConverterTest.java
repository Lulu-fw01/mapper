package luka.mapper.converter;

import luka.mapper.LuluMapper;
import luka.mapper.testClasses.Gender;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.beans.VetoableChangeSupport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    Person initPerson() {
        var person = new Person("Mark", 32);
        person.date = LocalDateTime.of(2001, 12, 13, 23, 59, 59);
        person.manyNumbers = new ArrayList<>();
        person.manyNumbers.add(1);
        person.manyNumbers.add(5);
        person.manyNumbers.add(8);

        var chars = new ArrayList<Character>();
        chars.add('t');
        chars.add('6');
        chars.add('g');
        person.setManyCharacters(chars);
        return person;
    }

    @Test
    void fieldNameToJson() {

        var person = initPerson();

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
        var person = initPerson();

        try {
            var field = Person.class.getDeclaredField("date");
            var jsonString = Converter.dateValueToJson(person.date, field);
            assertEquals("\"2001-12-13 23:59:59\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void fieldToJson() {

        var person = initPerson();

        try {
            var jsonString = Converter.fieldToJson(person.name, person.getClass().getField("name"));
            assertEquals("\"name\": \"Mark\"", jsonString);

            var field = person.getClass().getDeclaredField("age");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson(field.get(person), field);
            assertEquals("\"age\": \"32\"", jsonString);


            jsonString = Converter.fieldToJson(person.manyNumbers, person.getClass().getField("manyNumbers"));
            assertEquals("\"manyNumbers\": [\"1\", \"5\", \"8\"]", jsonString);

            field = person.getClass().getDeclaredField("manyCharacters");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson((Collection<?>) field.get(person), field);
            assertEquals("\"chars\": [\"t\", \"6\", \"g\"]", jsonString);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }


    @Test
    void collectionValueToJson() {
        var person = initPerson();

        try {
            var jsonString = Converter.collectionValueToJson(person.manyNumbers, person.getClass().getField("manyNumbers"));
            assertEquals("[\"1\", \"5\", \"8\"]", jsonString);

            var field = person.getClass().getDeclaredField("manyCharacters");
            field.setAccessible(true);
            jsonString = Converter.collectionValueToJson((Collection<?>) field.get(person), field);
            assertEquals("[\"t\", \"6\", \"g\"]", jsonString);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    void enumValueToJson() {
        Gender gender = Gender.MALE;

        var jsonString = Converter.enumValueToJson(gender);
        assertEquals("\"MALE\"", jsonString);
    }

}