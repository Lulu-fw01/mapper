package luka.mapper.testClasses;

import ru.hse.homework4.Exported;

import java.time.LocalDate;

@Exported
public class Human {
    public Human() {

    }

    private Gender gender;

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate birthday;

}
