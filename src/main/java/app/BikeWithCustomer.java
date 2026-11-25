package app;

import java.time.Instant;

public record BikeWithCustomer(long id, String model, boolean available, Instant rentedAt, Customer customer) {
}
