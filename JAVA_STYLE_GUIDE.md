# Java Style Guide
This guide defines the coding standards for source code in the Java™ Programming Language for IcedTeaWeb.

## Why do we need this
When writing source code one main goal should be to create classes and code blocks that are easy to read, understand and maintain.
By doing so bugs can be found much easier in source code and new developers will understand the functionality of the code faster.
This style guide contains common rules that are known by mostly all Java developers.

## Source files
All source files (*.java) must have case-sensitive name of the top-level class it contains. Source files are always encoded in UTF-8. If the file belongs to an open source project a licence header must be added to the top of the file.

## Naming and declaration

### Package names
Package names are all lowercase, with consecutive words simply concatenated together (no underscores). For example, `com.example.deepspace`,
not `com.example.deepSpace` or `com.example.deep_space`.

### Class names
Class names are written in UpperCamelCase. Test classes are named starting with the name of the class they are testing, and ending with
`Test`. For example, `HashTest` or `HashIntegrationTest`.

### Method names
Method names are written in lowerCamelCase.

### Constant names
Constant names use CONSTANT_CASE: all uppercase letters, with each word separated from the next by a single underscore. Constants are static final fields whose contents are deeply immutable and whose methods have no detectable side effects. This includes primitives, Strings, immutable types, and immutable collections of immutable types. If any of the instance's observable state can change, it is not a constant. Merely intending to never mutate the object is not enough.

### Non-constant field names
Non-constant field names (static or otherwise) are written in lowerCamelCase.

### Parameter names
Parameter names are written in lowerCamelCase. One-character parameter names in public methods should be avoided.

### Local variable names
Local variable names are written in lowerCamelCase. Even when final and immutable, local variables are not considered to be constants, and should not be styled as constants.

## Programming Practices

### Declaration of variables
Every variable declaration (field or local) declares only one variable: declarations such as `int a, b;` are not used.

### Usage of @Override
A method is marked with the @Override annotation whenever it is legal. This includes a class method overriding a superclass method, a class method implementing an interface method, and an interface method respecifying a superinterface method.

### No wildcard imports
Wildcard imports make the source of an imported class less clear.

### Avoid assert
We avoid the assert statement since it can be disabled at execution time.

### Minimize visibility
In a class API, you should support access to any methods and fields that you make accessible. Therefore, only expose what you intend the caller to use. This can be imperative when writing thread-safe code.

### Favor immutability
Mutable objects carry a burden - you need to make sure that those who are able to mutate it are not violating expectations of other users of the object, and that it's even safe for them to modify. Based on this the `final` keyword must be added to any field, paramater and variable if possible.

Negative example:

```java
public class SpecificService {

    private String name;

    public SpecificService(String name) {
        this.name = name;
        ServiceProvider provider = new ServiceProvider();
        provider.register(this);
    }
}
```

Good example:

```java
public final class SpecificService {

    private final String name;

    public SpecificService(final String name) {
        this.name = name;
        final ServiceProvider provider = new ServiceProvider();
        provider.register(this);
    }
}
```

Next to general immutability all collections that are returned by classes should be immutable. If a collection should be
mutated from the outside methods can be added to the class.

Negative example:

```java
public class SpecificService {

    private List<String> names = new ArrayList();

    public List<String> getNames() {return names;}

    public void setNames(List<String> names) {this.names = names;}

}

//somewhere else:
SpecificService service = ....
service.getNames().add("Duke");

```

Good example:

```java
public class SpecificService {

    private final List<String> names = new ArrayList();

    public List<String> getNames() {return Collections.unmodifiableList(names);}

    public void addName(final String name) {this.names.add(name);}

    public void removeName(final String name) {this.names.remove(name);}

}

//somewhere else:
final SpecificService service = ....
service.addName("Duke");

// service.getNames().add("Duke"); <- This call would throw an exception now


```


### Do null checks
Each parameter, variable & field that is accessed after initalization / change must be directly checked for a `null` value.

Example:

```java
public class Service {

    private Handler handler;

    public Service(final Handler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public void setHandler(final Handler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public void start() {
        handler.start();
    }
}
```