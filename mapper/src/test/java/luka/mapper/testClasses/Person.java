package luka.mapper.testClasses;

import ru.hse.homework4.DateFormat;
import ru.hse.homework4.PropertyName;

import java.time.LocalDateTime;

public class Person {

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
}
