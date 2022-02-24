package luka.mapper.testClasses;

import ru.hse.homework4.Exported;

@Exported
public class SomeClass {

    public SomeClass() {

    }
    public String someString;

    private SomeClass link;

    public void setLink(SomeClass link) {
        this.link = link;
    }

    public SomeClass getLink() {
        return link;
    }
}
