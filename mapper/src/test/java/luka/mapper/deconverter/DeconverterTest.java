package luka.mapper.deconverter;

import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

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

    }

}