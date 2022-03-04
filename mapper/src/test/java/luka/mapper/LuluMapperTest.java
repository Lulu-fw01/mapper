package luka.mapper;

import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;
import ru.hse.homework4.Mapper;

import java.io.File;
import java.io.IOException;

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
        var file = new File("output.json");
        Mapper myMapper = new LuluMapper();
        try {
            var result = myMapper.read(Person.class, file);
            Person answer = Person.initPerson();
            assert result != null;
            assertEquals(answer.name, result.name);
            assertEquals(answer.getAge(), result.getAge());
            assertEquals(answer.lastName, result.lastName);
            assertArrayEquals(answer.manyNumbers.toArray(new Integer[0]), result.manyNumbers.toArray(new Integer[0]) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void writeToString() {
    }

    @Test
    void write() {
    }

    @Test
    void fileWrite() {
        var file = new File("output.json");
        var person = Person.initPerson();
        var mapper = new LuluMapper();
        try {
            mapper.write(person, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}