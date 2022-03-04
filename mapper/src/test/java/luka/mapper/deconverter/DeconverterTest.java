package luka.mapper.deconverter;

import luka.mapper.converter.Converter;
import luka.mapper.deconverter.jsonNode.JsonNode;
import luka.mapper.testClasses.Gender;
import luka.mapper.testClasses.Human;
import luka.mapper.testClasses.Pair;
import luka.mapper.testClasses.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
        var human = new Human();
        try {
            var localDateTime = LocalDateTime.of(2001, 12, 13, 23, 59, 59);
            var field = person.getClass().getDeclaredField("date");
            field.setAccessible(true);
            var val = Deconverter.getDateValue(field, field.getType(), "\"2001-12-13 23:59:59\"");
            assertEquals(localDateTime, val);

            var localDate = LocalDate.of(2001, 12, 13);
            field = human.getClass().getDeclaredField("birthday");
            field.setAccessible(true);
            val = Deconverter.getDateValue(field, field.getType(), "\"2001-12-13\"");
            assertEquals(localDate, val);
        } catch (NoSuchFieldException e) {
            fail();
        }
    }


    @Test
    void getCollectionValue() {
        var person = new Person();
        try {
            var field = person.getClass().getDeclaredField("manyNumbers");
            field.setAccessible(true);
            var val = Deconverter.getCollectionValue(field, "[\"1\", \"5\", \"8\"]");
            var answer = new ArrayList<Integer>();
            answer.add(1);
            answer.add(5);
            answer.add(8);
            assert val != null;
            assertArrayEquals(answer.toArray(), val.toArray());
        } catch (NoSuchFieldException e) {

        }
    }

    @Test
    void getStringWithoutBorders() {
        var res = Deconverter.getStringWithoutBorders("\"5\"", '\"', '\"');
        assertEquals("5", res);

        res = Deconverter.getStringWithoutBorders("{\"a\", \"b\"}", '{', '}');
        assertEquals("\"a\", \"b\"", res);
    }

    @Test
    void getObjectFromString() {
        var jsonString = "{\"name\": \"Mark\", \"age\": \"32\"," +
                " \"surname\": \"Zuck\", \"myLocalDateTime\": \"2001-12-13 23:59:59\"," +
                " \"manyNumbers\": [\"1\", \"5\", \"8\"], \"chars\": [\"t\", \"6\", \"g\"]}";

        Person result = Deconverter.getObjectFromString(Person.class, jsonString);

        Person answer = Person.initPerson();
        assert result != null;
        assertEquals(answer.name, result.name);
        assertEquals(answer.getAge(), result.getAge());
        assertEquals(answer.lastName, result.lastName);
        assertArrayEquals(answer.manyNumbers.toArray(new Integer[0]), result.manyNumbers.toArray(new Integer[0]) );

        var fPair = new Pair();
        fPair.personFirst = Person.initPerson();
        fPair.personSecond = Person.initPerson();
        jsonString = Converter.objectToJson(fPair);

        var hMap = new HashMap<String, Object>();

        var pair = Deconverter.getObjectFromString(Pair.class, jsonString, hMap);

        assert pair != null;
        assertEquals(pair.personFirst, pair.personSecond);
    }


    @Test
    void findRightBorder() {
        var jsonString = "{\"name\": \"Mark\", \"age\": \"32\"," +
                " \"surname\": \"Zuck\", \"myLocalDateTime\": \"2001-12-13 23:59:59\"," +
                " \"manyNumbers\": [\"1\", \"5\", \"8\"], \"chars\": [\"t\", \"6\", \"g\"]}";

        var builder = new StringBuilder(jsonString);
        int res = Deconverter.findRightBorder(builder, 0, jsonString.length());
        assertEquals(jsonString.length(), res);

        res = Deconverter.findRightBorder(builder, 7, jsonString.length());
        assertEquals(15, res);


        jsonString = "{\"manyNumbers\": [\"1\", \"5\", \"8\"]}";
        builder = new StringBuilder(jsonString);
        res = Deconverter.findRightBorder(builder, 14, jsonString.length());
        assertEquals(jsonString.length() - 1, res);
    }

    @Test
    void getJsonNodes() {
        var jsonString = "{\"name\": \"Mark\", \"age\": \"32\"," +
                " \"surname\": \"Zuck\", \"myLocalDateTime\": \"2001-12-13 23:59:59\"," +
                " \"manyNumbers\": [\"1\", \"5\", \"8\"], \"chars\": [\"t\", \"6\", \"g\"]}";

        var nodes = Deconverter.getJsonNodes(jsonString);

        ArrayList<JsonNode> answer = new ArrayList<>();
        answer.add(new JsonNode("\"name\"", "\"Mark\""));
        answer.add(new JsonNode("\"age\"", "\"32\""));
        answer.add(new JsonNode("\"surname\"", "\"Zuck\""));
        answer.add(new JsonNode("\"myLocalDateTime\"", "\"2001-12-13 23:59:59\""));
        answer.add(new JsonNode("\"manyNumbers\"", "[\"1\", \"5\", \"8\"]"));
        answer.add(new JsonNode("\"chars\"", "[\"t\", \"6\", \"g\"]"));

        assertEquals(answer.size(), nodes.size());

        for (int i = 0; i < answer.size(); ++i) {
            assertEquals(answer.get(i).name, nodes.get(i).name);
            assertEquals(answer.get(i).value, nodes.get(i).value);
        }
    }

    @Test
    void getJsonArrayNodes() {
        var jsonString = "[\"1\", \"5\", \"8\"]";

        var nodes = Deconverter.getJsonArrayNodes(jsonString);

        ArrayList<JsonNode> answer = new ArrayList<>();
        answer.add(new JsonNode("", "\"1\""));
        answer.add(new JsonNode("", "\"5\""));
        answer.add(new JsonNode("", "\"8\""));

        assertEquals(answer.size(), nodes.size());

        for (int i = 0; i < answer.size(); ++i) {
            assertEquals(answer.get(i).value, nodes.get(i).value);
        }

        jsonString = "[\"t\", \"6\", \"g\"]";
        nodes = Deconverter.getJsonArrayNodes(jsonString);
        answer = new ArrayList<>();
        answer.add(new JsonNode("", "\"t\""));
        answer.add(new JsonNode("", "\"6\""));
        answer.add(new JsonNode("", "\"g\""));

        for (int i = 0; i < answer.size(); ++i) {
            assertEquals(answer.get(i).value, nodes.get(i).value);
        }
    }


}