package com.danpeter.postson;

import com.danpeter.postson.impl.JsonDatastore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AnnotationTest {

    private static PGPoolingDataSource source;
    private static Datastore datastore;
    private final BookWithIsbn book = new BookWithIsbn("978-3-16-148410-0", "George RR Martin");

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
        connection.createStatement().execute("DELETE FROM book");
        connection.close();
    }

    @Test
    public void testName() throws Exception {
        datastore.save(book);
        assertThat(datastore.get(BookWithIsbn.class, book.isbn()).isPresent(), is(true));
    }
}