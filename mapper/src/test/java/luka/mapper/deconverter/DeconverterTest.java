package luka.mapper.deconverter;

import luka.mapper.testClasses.Gender;
import luka.mapper.testClasses.Human;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeconverterTest {

    @Test
    void getEasyValue() {
        var person = new Person();
        try {
            var field = person.getClass().getDeclaredField("age");
            field.setAccessible(true);
            var val = Deconverter.getEasyValue(field.getType(), "\"10\"");
            assertEquals(10, val);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void getEnumValue() {
        var human = new Human();

        try {
            var field = human.getClass().getDeclaredField("gender");
            field.setAccessible(true);
            var val = Deconverter.getEnumValue(field.getType(), "\"FEMALE\"");
            assertEquals(Gender.FEMALE, val);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void getDateValue() {
        var person = new Person();
        try {
            var localDateTime = LocalDateTime.of(2001, 12, 13, 23, 59, 59);
            var field = person.getClass().getDeclaredField("date");
            field.setAccessible(true);
            var val = Deconverter.getDateValue(field, field.getType(), "\"2001-12-13 23:59:59\"");
            assertEquals(localDateTime, val);
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
            Deconverter.getCollectionValue(field, "[\"1\", \"5\", \"8\"]");
        } catch (NoSuchFieldException e) {

        }
    }

}