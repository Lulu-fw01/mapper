package luka.mapper.testClasses;

import ru.hse.homework4.DateFormat;
import ru.hse.homework4.Exported;
import ru.hse.homework4.PropertyName;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Exported
public class Person {

    public Person() {
        name = "";
        age = 0;
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name;

    private Integer age;

    public Integer getAge() {
        return age;
    }

    @PropertyName(value = "surname")
    public String lastName;

    @PropertyName(value = "myLocalDateTime")
    @DateFormat("uuuu-MM-dd HH:mm:ss")
    public LocalDateTime date;

    public ArrayList<Integer> manyNumbers;

    @PropertyName(value = "chars")
    private ArrayList<Character> manyCharacters;

    public void setManyCharacters(ArrayList<Character> chars) {
        manyCharacters = chars;
    }


    public static Person initPerson() {
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
}
