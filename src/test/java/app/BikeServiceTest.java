package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BikeServiceTest {
    private RentalService service;

    @BeforeEach
    void setUp() {
        service = new RentalService(new RentalRepository());
    }

    @Test
    void newServiceHasNoResources() {
        assertTrue(service.getBikes().isEmpty());
        assertTrue(service.getCustomers().isEmpty());
    }

    @Test
    void createsAndFindsBike() {
        Bike bike = service.createBike("  Stels  ");

        assertEquals(1, bike.getId());
        assertEquals("Stels", service.getBike(bike.getId()).getModel());
        assertTrue(bike.isAvailable());
        assertNull(bike.getCustomerId());
    }

    @Test
    void rejectsEmptyAndNullText() {
        assertThrows(IllegalArgumentException.class, () -> service.createBike(null));
        assertThrows(IllegalArgumentException.class, () -> service.createBike(""));
        assertThrows(IllegalArgumentException.class, () -> service.createBike(" \t "));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer(null, "regular"));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer("", "regular"));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer(" \t ", "regular"));

        assertTrue(service.getBikes().isEmpty());
        assertTrue(service.getCustomers().isEmpty());
    }

    @Test
    void checksLengthAfterTrimming() {
        String text = "x".repeat(100);

        assertEquals(text, service.createBike(" " + text + " ").getModel());
        assertEquals(text, service.createCustomer(" " + text + " ", "regular").getName());
        assertThrows(IllegalArgumentException.class, () -> service.createBike(text + "x"));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer(text + "x", "regular"));
        assertEquals(1, service.getBikes().size());
        assertEquals(1, service.getCustomers().size());
    }

    @Test
    void rejectedCreationDoesNotConsumeIds() {
        assertThrows(IllegalArgumentException.class, () -> service.createBike(""));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer("Oleg", "vip"));

        assertEquals(1, service.createBike("Stels").getId());
        assertEquals(1, service.createCustomer("Oleg", "regular").getId());
    }

    @Test
    void missingResourcesCauseNotFound() {
        assertThrows(NotFoundException.class, () -> service.getBike(99));
        assertThrows(NotFoundException.class, () -> service.updateBike(99, "Stels"));
        assertThrows(NotFoundException.class, () -> service.deleteBike(99));
        assertThrows(NotFoundException.class, () -> service.getCustomer(99));
        assertThrows(NotFoundException.class, () -> service.updateCustomer(99, "Oleg", "regular"));
        assertThrows(NotFoundException.class, () -> service.deleteCustomer(99));
    }

    @Test
    void updatesBikeWithoutChangingRental() {
        Bike bike = service.createBike("Stels");
        Customer customer = service.createCustomer("Oleg", "regular");
        service.rentBike(customer.getId(), bike.getId());

        Bike updated = service.updateBike(bike.getId(), " Trek ");

        assertEquals(bike.getId(), updated.getId());
        assertEquals("Trek", service.getBike(bike.getId()).getModel());
        assertEquals(Long.valueOf(customer.getId()), updated.getCustomerId());
        assertFalse(updated.isAvailable());
    }

    @Test
    void invalidUpdatePreservesBike() {
        Bike bike = service.createBike("Stels");

        assertThrows(IllegalArgumentException.class, () -> service.updateBike(bike.getId(), " "));
        assertThrows(IllegalArgumentException.class, () -> service.updateBike(bike.getId(), "x".repeat(101)));
        assertEquals("Stels", service.getBike(bike.getId()).getModel());
        assertTrue(service.getBike(bike.getId()).isAvailable());
    }

    @Test
    void filtersBikesByModelAndAvailability() {
        Bike first = service.createBike("Stels");
        Bike second = service.createBike("Stels Pilot");
        service.createBike("Trek");
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike rented = service.rentBike(customer.getId(), first.getId());

        assertEquals(List.of(second), service.getBikes("Stels", true));
        assertEquals(List.of(rented), service.getBikes("Stels", false));
        assertEquals(2, service.getBikes("Stels", null).size());
        assertEquals(2, service.getBikes(null, true).size());
        assertTrue(service.getBikes("Unknown", null).isEmpty());
    }

    @Test
    void deletingResourcesDoesNotReuseIds() {
        Bike bike = service.createBike("Stels");
        Customer customer = service.createCustomer("Oleg", "regular");

        service.deleteBike(bike.getId());
        service.deleteCustomer(customer.getId());

        assertTrue(service.getBikes().isEmpty());
        assertTrue(service.getCustomers().isEmpty());
        assertThrows(NotFoundException.class, () -> service.getBike(bike.getId()));
        assertThrows(NotFoundException.class, () -> service.getCustomer(customer.getId()));
        assertEquals(2, service.createBike("Trek").getId());
        assertEquals(2, service.createCustomer("Anna", "premium").getId());
    }

    @Test
    void changingReturnedListsDoesNotChangeStorage() {
        service.createBike("Stels");
        service.createCustomer("Oleg", "regular");

        service.getBikes().clear();
        service.getCustomers().clear();

        assertEquals(1, service.getBikes().size());
        assertEquals(1, service.getCustomers().size());
    }
}
