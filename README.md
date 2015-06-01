# postson

A lightweight ORM for storing Java entities as JSONB in Postgres.

Assumes that the entity has a field named 'id' and that the table is named the same as the entity in snake_case.

## Setup
```java
    DataSource source = ...

    Datastore datastore = new JsonDatastore(source);
```

## Basic entity operations
```java
    Address address = new Address("Main street", "11772");
    User user = new User(UUID.randomUUID(), "Kent", "Kennedy", address, 30);

    //Saving a entity
    datastore.save(user);

    //Get a entity
    Optional<User> user = datastore.get(User.class, user.id());

    //Deleting a entity
    datasore.delete(User.class, user.id());
```

## Object query
The object query takes advantage of the query possibilities of JSONB in postgres.
```java
    Optional<User> result = datastore.createObjectQuery(User.class)
                  .field("address", address)
                  .singleResult();
```
## Primitive queries
The primitive queries are less efficient but allows for more fine grained querying.

```java
    List<User> users = datastore.createPrimitiveQuery(User.class)
                  .field("age")
                  .greaterThan(20)
                  .asList();

    List<User> users = datastore.createPrimitiveQuery(User.class)
                        .field("firstName")
                        .equalsIgnoreCase("Ken")
                        .field("address.zip")
                        .equal("11772")
                        .asList();
```

 Dependencies:
 * Postgres version > 9.4.
 * JDK 8