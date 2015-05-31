package com.danpeter.postson;

import com.danpeter.postson.impl.JsonDatastore;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonDatastoreTest {


    public static final SystemUser DAN_P = new SystemUser(UUID.randomUUID(), "Dan", "Peterström", new SystemUser.Address("Vintervägen", "17777"));
    private static Datastore datastore;
    private static PGPoolingDataSource source;

    @BeforeClass
    public static void setUp() throws Exception {
        source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("test");
        source.setUser("test");
        source.setPassword("test");
        source.setMaxConnections(10);

        datastore = new JsonDatastore(source);
    }

    @AfterClass
    public static void end() {
        source.close();
    }

    @After
    public void tearDown() throws Exception {
        Connection connection = source.getConnection();
        connection.createStatement().execute("DELETE FROM system_user");
        connection.close();
    }

    @Test
    public void findByRootField() throws Exception {
        datastore.save(DAN_P);
        List<SystemUser> systemUsers = datastore.createObjectQuery(SystemUser.class)
                .field("firstName", "Dan")
                .asList();

        assertThat(systemUsers.size(), is(1));
    }

    @Test
    public void findByNestedField() throws Exception {
        datastore.save(DAN_P);
        List<SystemUser> systemUsers = datastore.createObjectQuery(SystemUser.class)
                .field("address.street", "Vintervägen")
                .asList();

        assertThat(systemUsers.size(), is(1));
    }

    @Test(expected = DatastoreException.class)
    public void idIsUnique() throws Exception {
        datastore.save(DAN_P);
        datastore.save(DAN_P);
    }

    @Test
    public void getSingleByField() throws Exception {
        datastore.save(DAN_P);
        Optional<SystemUser> systemUserOptional = datastore.createObjectQuery(SystemUser.class)
                .field("id", DAN_P.id())
                .singleResult();
        assertThat(systemUserOptional.isPresent(), is(true));
    }

    @Test
    public void getCountByField() throws Exception {
        datastore.save(DAN_P);
        int count = datastore.createObjectQuery(SystemUser.class)
                .field("id", DAN_P.id())
                .count();
        assertThat(count, is(1));
    }

    @Test
    public void getById() throws Exception {
        datastore.save(DAN_P);
        Optional<SystemUser> user = datastore.get(SystemUser.class, DAN_P.id());
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void getByCompundId() throws Exception {
        Trip trip = new Trip(new Trip.TripId("Sweden", "Bahamas", LocalDateTime.now()), "Super vacation!");
        datastore.save(trip);
        assertThat(datastore.get(Trip.class, trip.id()).isPresent(), is(true));
    }

    @Test
    public void deleteEntityReturnsTrue() throws Exception {
        datastore.save(DAN_P);
        assertThat(datastore.delete(SystemUser.class, DAN_P.id()), is(true));
        assertThat(datastore.get(SystemUser.class, DAN_P.id()).isPresent(), is(false));
    }

    @Test
    public void deleteEntityThatDoesNotExistReturnsFalse() throws Exception {
        assertThat(datastore.delete(SystemUser.class, "does_not_exist"), is(false));
    }

    @Test
    public void addTypeAdapter() throws Exception {
        Datastore datastoreWithAdapter = new JsonDatastore(source, ImmutableMap.of(SystemUser.class, new SystemUserSerializer()));
        datastoreWithAdapter.save(DAN_P);
        Statement statement = source.getConnection().createStatement();
        statement.execute("SELECT data from system_user");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        assertThat(resultSet.getString(1), is("{\"displayName\": \"Dan Peterström\"}"));
    }
}