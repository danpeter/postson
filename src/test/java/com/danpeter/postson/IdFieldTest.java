package com.danpeter.postson;

import com.danpeter.postson.BookWithIsbn;
import com.danpeter.postson.SystemUser;
import com.danpeter.postson.impl.IdField;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IdFieldTest {

    @Test
    public void noIdAnnotationReturnId() throws Exception {
        assertThat(IdField.from(SystemUser.class).toString(), is("id"));
    }

    @Test
    public void otherFieldIdAnnotated() throws Exception {
        assertThat(IdField.from(BookWithIsbn.class).toString(), is("isbn"));
    }
}