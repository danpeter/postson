package com.danpeter.postson;

import com.danpeter.postson.annotations.Id;
import com.danpeter.postson.annotations.Table;

import java.util.UUID;

public class SystemUser {

    private UUID id;
    private final String firstName;
    private final String lastName;
    private final Address address;
    private final int age;

    public SystemUser(UUID id, String firstName, String lastName, Address address, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.age = age;
    }

    public UUID id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public static class Address {

        private final String street;
        private final String zip;

        public Address(String street, String zip) {
            this.street = street;
            this.zip = zip;
        }
    }
}
