package luka.mapper.converter;

import luka.mapper.exceptions.CycleException;
import luka.mapper.exceptions.SameFieldNamesException;
import luka.mapper.testClasses.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    SomeClass initComeClass() {
        var someClass = new SomeClass();
        someClass.someString = "this is some string.";

        var someClass2 = new SomeClass();
        someClass2.someString = "this is link";
        someClass.setLink(someClass2);

        return someClass;
    }

    @Test
    void objectToJson() {
        var person = Person.initPerson();
        var jsonString = Converter.objectToJson(person);

        assertEquals("{\"name\": \"Mark\", \"age\": \"32\"," +
                " \"surname\": \"Zuck\", \"myLocalDateTime\": \"2001-12-13 23:59:59\"," +
                " \"manyNumbers\": [\"1\", \"5\", \"8\"], \"chars\": [\"t\", \"6\", \"g\"]}", jsonString);


        var someClass = initComeClass();
        jsonString = Converter.objectToJson(someClass);
        assertEquals("{\"someString\": \"this is some string.\", \"link\": {\"someString\": \"this is link\"}}", jsonString);

        someClass.setLink(someClass);
        try {
            jsonString = Converter.objectToJson(someClass);
        } catch (CycleException e) {
            assertEquals(e.getObject(), someClass);
        }

        someClass = initComeClass();
        someClass.getLink().setLink(someClass);
        try {
            jsonString = Converter.objectToJson(someClass);
        } catch (CycleException e) {
            assertEquals(e.getObject(), someClass);
        }

        Integer integer = 1;
        jsonString = Converter.objectToJson(integer);
        assertEquals("", jsonString);

    }


    @Test
    void fieldNameToJson() {

        var person = Person.initPerson();
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
        var person = Person.initPerson();
        Field field;
        try {
            field = Person.class.getDeclaredField("date");
            var jsonString = Converter.dateValueToJson(person.date, field);
            assertEquals("\"2001-12-13 23:59:59\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }

        var dateTestC = new DateTestClass();
        dateTestC.testLocalDate = LocalDate.of(2001, 12, 13);
        try {
            field = DateTestClass.class.getDeclaredField("testLocalDate");
            var jsonString = Converter.dateValueToJson(dateTestC.testLocalDate, field);
            assertEquals("\"2001-12-13\"", jsonString);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void fieldToJson() {

        var person = Person.initPerson();
        HashSet<String> fieldNames = new HashSet<>();
        HashSet<Object> usedClasses = new HashSet<>();
        String jsonString;
        try {
            jsonString = Converter.fieldToJson(person.name, person.getClass().getField("name"), fieldNames, usedClasses, false);
            assertEquals("\"name\": \"Mark\"", jsonString);

            var field = person.getClass().getDeclaredField("age");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson(field.get(person), field, fieldNames, usedClasses, false);
            assertEquals("\"age\": \"32\"", jsonString);


            jsonString = Converter.fieldToJson(person.manyNumbers, person.getClass().getField("manyNumbers"), fieldNames, usedClasses, false);
            assertEquals("\"manyNumbers\": [\"1\", \"5\", \"8\"]", jsonString);

            field = person.getClass().getDeclaredField("manyCharacters");
            field.setAccessible(true);
            jsonString = Converter.fieldToJson((Collection<?>) field.get(person), field, fieldNames, usedClasses, false);
            assertEquals("\"chars\": [\"t\", \"6\", \"g\"]", jsonString);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }

        try {
            var someClass = initComeClass();
            someClass.setLink(null);

            jsonString = Converter.fieldToJson(someClass.getLink(), someClass.getClass().getDeclaredField("link"), fieldNames, usedClasses, false);
            assertEquals("", jsonString);

            jsonString = Converter.fieldToJson(someClass.getLink(), someClass.getClass().getDeclaredField("link"), fieldNames, usedClasses, true);
            assertEquals("\"link\": null", jsonString);

        } catch (NoSuchFieldException e) {
            fail();
        }


    }


    @Test
    void collectionValueToJson() {
        var person = Person.initPerson();
        HashSet<Object> usedClasses = new HashSet<>();

        String jsonString;
        try {
            jsonString = Converter.collectionValueToJson(person.manyNumbers, person.getClass().getField("manyNumbers"), usedClasses);
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