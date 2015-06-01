package com.danpeter.postson;

import com.danpeter.postson.impl.JsonDatastore;
import org.junit.*;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PrimitiveQueryTest {

    public static final SystemUser DAN_P = new SystemUser(UUID.randomUUID(), "Dan", "Peterström", new SystemUser.Address("Vintervägen", "17777"), 30);
    private static Datastore datastore;
    private static PGPoolingDataSource source;

    @BeforeClass
    public static void beforeClass() throws Exception {
        source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("test");
        source.setUser("test");
        source.setPassword("test");
        source.setMaxConnections(10);

        datastore = new JsonDatastore(source);
    }

    @Before
    public void before() throws Exception {
        datastore.save(DAN_P);
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
    public void equalsString() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("firstName")
                .equal("Dan")
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void equalsInt() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("age")
                .equal(30)
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void beginsWith() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("firstName")
                .beginsWith("Da")
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void beginsWithIgnoreCase() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("firstName")
                .beginsWithIgnoreCase("da")
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void nestedEquals() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("address.street")
                .equal("Vintervägen")
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void twoFieldsBothMustMatch() throws Exception {
        datastore.save(new SystemUser(UUID.randomUUID(), "Daniel", "Karlsson", new SystemUser.Address("Bergvägen", "17717"), 30));
        List<SystemUser> users = datastore.createPrimitiveQuery(SystemUser.class)
                .field("address.street")
                .equal("Vintervägen")
                .field("firstName")
                .beginsWithIgnoreCase("Dan")
                .asList();
        assertThat(users.size(), is(1));
    }

    @Test
    public void greaterThan() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("age")
                .greaterThan(20)
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void lessThan() throws Exception {
        Optional<SystemUser> user = datastore.createPrimitiveQuery(SystemUser.class)
                .field("age")
                .lessThan(40)
                .singleResult();
        assertThat(user.isPresent(), is(true));
    }

    @Test
    public void count() throws Exception {
        int count = datastore.createPrimitiveQuery(SystemUser.class)
                .field("firstName")
                .equal("Dan")
                .count();
        assertThat(count, is(1));
    }

    @Test
    public void delete() throws Exception {
        datastore.createPrimitiveQuery(SystemUser.class)
                .field("firstName")
                .equal("Dan")
                .delete();
       assertThat(datastore.get(SystemUser.class, DAN_P.id()).isPresent(), is(false));
    }
}
