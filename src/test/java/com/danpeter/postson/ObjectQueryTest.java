package com.danpeter.postson;

import com.danpeter.postson.impl.JsonDatastore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ObjectQueryTest {

    public static final SystemUser DAN_P = new SystemUser(UUID.randomUUID(), "Dan", "Peterstrom", new SystemUser.Address("Vintervagen", "17777"), 30);
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
                .field("address.street", "Vintervagen")
                .asList();

        assertThat(systemUsers.size(), is(1));
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
}
