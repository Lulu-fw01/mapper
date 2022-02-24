package luka.mapper.converter;

import luka.mapper.converter.exceptions.SameFieldNamesException;
import luka.mapper.testClasses.Gender;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    Person initPerson() {
        var person = new Person("Mark", 32);
        person.lastName = "Zuck";
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
    void objectToJson() {
        var person = initPerson();
        var jsonString = Converter.objectToJson(person);

        assertEquals("{\"name\": \"Mark\", \"age\": \"32\"," +
                " \"surname\": \"Zuck\", \"myLocalDateTime\": \"2001-12-13 23:59:59\"," +
                " \"manyNumbers\": [\"1\", \"5\", \"8\"], \"chars\": [\"t\", \"6\", \"g\"]}", jsonString);
    }


    @Test
    void fieldNameToJson() {

        var person = initPerson();
        HashSet<String> fieldNames = new HashSet<>();
        String jsonString;
        try {
            jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("lastName"), fieldNames);
            assertEquals("\"surname\"", jsonString);

            jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("name"), fieldNames);
            assertEquals("\"name\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }

        try {
            jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("name"), fieldNames);
            fail();
        } catch (NoSuchFieldException e) {
            fail();
        } catch (SameFieldNamesException e) {
            assertEquals("Field name \"name\" has been already used.", e.getMessage());
        }

        try {
            jsonString = Converter.fieldNameToJson(person.getClass().getDeclaredField("lastName"), fieldNames);
            fail();
        } catch (NoSuchFieldException e) {
            fail();
        } catch (SameFieldNamesException e) {
            assertEquals("Property name \"surname\" has been already used.", e.getMessage());
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
        HashSet<String> fieldNames = new HashSet<>();
        HashSet<Object> usedClasses = new HashSet<>();
        try {
            var jsonString = Converter.fieldToJson(person.name, person.getClass().getField("name"), fieldNames, usedClasses);
            assertEquals("\"name\": \"Mark\"", jsonString);

            var field = person.getClass().getDeclaredField("age");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson(field.get(person), field, fieldNames, usedClasses);
            assertEquals("\"age\": \"32\"", jsonString);


            jsonString = Converter.fieldToJson(person.manyNumbers, person.getClass().getField("manyNumbers"), fieldNames, usedClasses);
            assertEquals("\"manyNumbers\": [\"1\", \"5\", \"8\"]", jsonString);

            field = person.getClass().getDeclaredField("manyCharacters");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson((Collection<?>) field.get(person), field, fieldNames, usedClasses);
            assertEquals("\"chars\": [\"t\", \"6\", \"g\"]", jsonString);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }


    @Test
    void collectionValueToJson() {
        var person = initPerson();
        HashSet<Object> usedClasses = new HashSet<>();

        try {
            var jsonString = Converter.collectionValueToJson(person.manyNumbers, person.getClass().getField("manyNumbers"), usedClasses);
            assertEquals("[\"1\", \"5\", \"8\"]", jsonString);

            var field = person.getClass().getDeclaredField("manyCharacters");
            field.setAccessible(true);
            jsonString = Converter.collectionValueToJson((Collection<?>) field.get(person), field, usedClasses);
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