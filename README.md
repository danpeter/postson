# postson

A lightweight ORM for storing Java entities as JSONB in Postgres.

Assumes that the entity has a field named 'id' and that the table is named the same as the entity in snake_case.

Example:
 ```java
      PGPoolingDataSource source = new PGPoolingDataSource();
              source.setDataSourceName("A Data Source");
              source.setServerName("localhost");
              source.setDatabaseName("test");
              source.setUser("test");
              source.setPassword("test");
              source.setMaxConnections(10);

      Datastore datastore = new JsonDatastore(source);
      User user = new User(UUID.randomUUID(), "Kent", "Kennedy", 30);
      datastore.save(user);

      Optional<User> result = datastore.createObjectQuery(User.class)
                      .field("firstName", "Kent")
                      .singleResult();

      List<User> users = datastore.createPrimitiveQuery(User.class)
                      .field("age")
                      .greaterThan(20)
                      .asList();

```
 Dependencies:
 * Postgres version > 9.4.
 * JDK 8