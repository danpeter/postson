package com.danpeter.postson;

import com.danpeter.postson.BookWithIsbn;
import com.danpeter.postson.SystemUser;
import com.danpeter.postson.impl.TableName;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TableNameTest {

    @Test
    public void noAnnotation() throws Exception {
        assertThat(TableName.from(SystemUser.class).toString(), is("system_user"));
    }

    @Test
    public void tableAnnotated() throws Exception {
        assertThat(TableName.from(BookWithIsbn.class).toString(), is("book"));
    }
}