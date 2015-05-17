package com.danpeter.postson;

import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class QueryTest {


    public static final SystemUser DAN_P = new SystemUser(UUID.randomUUID(), "Dan", "Peterström", new Address("Vintervägen", "17777"));
    private JdbcDatastore datastore;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        datastore = new JdbcDatastore("localhost", "5432", "test", "test", "test");
        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/test", "test", "test");
    }

    @After
    public void tearDown() throws Exception {
        connection.createStatement().execute("DELETE FROM system_user");
        connection.close();
    }

    @Test
    public void findByRootField() throws Exception {
        datastore.save(DAN_P);
        List<SystemUser> systemUsers = datastore.createQuery(SystemUser.class)
                .field("firstName")
                .equal("Dan")
                .asList();

        assertThat(systemUsers.size(), is(1));
    }

    @Test
    public void findByNestedField() throws Exception {
        datastore.save(DAN_P);
        List<SystemUser> systemUsers = datastore.createQuery(SystemUser.class)
                .field("address.street")
                .equal("Vintervägen")
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
        Optional<SystemUser> systemUserOptional = datastore.createQuery(SystemUser.class)
                .field("id")
                .equal(DAN_P.id())
                .singleResult();
        assertThat(systemUserOptional.isPresent(), is(true));
    }

    @Test
    public void getCountByField() throws Exception {
        datastore.save(DAN_P);
        int count =  datastore.createQuery(SystemUser.class)
                .field("id")
                .equal(DAN_P.id())
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
}