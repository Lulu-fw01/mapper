# task-4-mapper
4th task of software design course.
_____
This task was directed to work with annotations and reflection.<p>
I had to implement an interface for serialization and deserialization of different objects marked with the appropriate annotations<p>
In task i could choose what type of serialization i want realize, but it should be human readable. I developed something like JSON.<p>
Futhermore, i wrote many tests.
_____
#### Mapper interface 
Full Mapper interface code with comments you can see [here](https://github.com/Lulu-fw01/task-4-mapper/blob/main/mapper/src/main/java/luka/mapper/LuluMapper.java)
  ```java
public interface Mapper {
   
    <T> T readFromString(Class<T> clazz, String input);
    
    <T> T read(Class<T> clazz, InputStream inputStream) throws IOException;
    
    <T> T read(Class<T> clazz, File file) throws IOException;
    
    String writeToString(Object object);
   
    void write(Object object, OutputStream outputStream) throws IOException;
    
    void write(Object object, File file) throws IOException;
}
```
I have implemented this interface in my class, [here](https://github.com/Lulu-fw01/task-4-mapper/blob/main/mapper/src/main/java/luka/mapper/LuluMapper.java). 
_____
In addition, two more classes were implemented. The [first](https://github.com/Lulu-fw01/task-4-mapper/blob/main/mapper/src/main/java/luka/mapper/converter/Converter.java) is for serialization, the [second](https://github.com/Lulu-fw01/task-4-mapper/blob/main/mapper/src/main/java/luka/mapper/deconverter/Deconverter.java) is for deserialization.
_____
All tests you can find [here](https://github.com/Lulu-fw01/task-4-mapper/tree/main/mapper/src/test/java/luka/mapper).
### Run tests process
- Maven version 3.8.5
- From [directory](https://github.com/Lulu-fw01/task-4-mapper/tree/main/mapper) with pom.xml.
```bash
mvn test
```

      
   

      
