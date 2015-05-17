# postson

A lightweight ORM for storing Java entities as JSONB in Postgres.

Assumes that the entity has a field named 'id' and that the table is named the same as the entity in snake_case.

Example:
 ```java
      Datastore datastore = new JsonDatastore("localhost", "5432", "dbName", "userName", "password");
      User user = new User(UUID.randomUUID(), "Kent", "Kennedy");
      datastore.save(user);

       Optional<SystemUser> result = datastore.createQuery(User.class)
                      .field("firstName")
                      .equal("Kent")
                      .singleResult();
```
 Dependencies:
 * Postgres version > 9.4.
 * JDK 8