package com.danpeter.postson;

import java.util.UUID;

public class SystemUser {

    private UUID id;
    private final String firstName;
    private final String lastName;
    private final Address address;

    public SystemUser(UUID id, String firstName, String lastName, Address address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public UUID id() {
        return id;
    }
}
