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
}
