package luka.mapper;

import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;
import ru.hse.homework4.Mapper;

import java.io.File;
import java.io.IOException;
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