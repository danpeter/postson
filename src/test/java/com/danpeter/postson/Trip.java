package com.danpeter.postson;

import java.time.LocalDateTime;

public class Trip {

    private final TripId id;

    private final String description;

    public Trip(TripId id, String description) {
        this.id = id;
        this.description = description;
    }

    public TripId id() {
        return id;
    }

    public static class TripId {
        private final String location;
        private final String destination;
        private final LocalDateTime date;

        public TripId(String location, String destination, LocalDateTime date) {
            this.location = location;
            this.destination = destination;
            this.date = date;
        }
    }
}
