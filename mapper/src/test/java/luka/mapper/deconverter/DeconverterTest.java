package luka.mapper.deconverter;

import luka.mapper.testClasses.Gender;
import luka.mapper.testClasses.Human;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeconverterTest {

    @Test
    void setEasyField() {
        var person = new Person();
        try {
            var field = person.getClass().getDeclaredField("age");
            field.setAccessible(true);
            Deconverter.setEasyField(person, field, "\"10\"");
            assertEquals(10, person.getAge());
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void setEnumField() {
        var human = new Human();

        try {
            var field = human.getClass().getDeclaredField("gender");
            field.setAccessible(true);
            Deconverter.setEnumField(human, field, "\"FEMALE\"");
            assertEquals(human.getGender(), Gender.FEMALE);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void setDateField() {
        var person = new Person();
        try {
            var localDateTime = LocalDateTime.of(2001, 12, 13, 23, 59, 59);
            var field = person.getClass().getDeclaredField("date");
            field.setAccessible(true);
            Deconverter.setDateField(person, field, "\"2001-12-13 23:59:59\"");
            assertEquals(localDateTime, person.date);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }


    @Test
    void setCollectionField() {
        var person = new Person();
        try {
            var field = person.getClass().getDeclaredField("manyNumbers");
            field.setAccessible(true);
            Deconverter.setCollectionField(person, field, "[\"1\", \"5\", \"8\"]");
        } catch (NoSuchFieldException e) {

        }
    }

}