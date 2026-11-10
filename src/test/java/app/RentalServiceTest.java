package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalServiceTest {
    private RentalService service;

    @BeforeEach
    void setUp() {
        service = new RentalService(new RentalRepository());
    }

    @Test
    void rentalAndReturnChangeStoredState() {
        Bike bike = service.createBike("Stels");
        Customer customer = service.createCustomer("Oleg", "regular");

        service.rentBike(customer.getId(), bike.getId());
        assertFalse(service.getBike(bike.getId()).isAvailable());
        assertEquals(Long.valueOf(customer.getId()), service.getBike(bike.getId()).getCustomerId());

        service.returnBike(customer.getId(), bike.getId());
        assertTrue(service.getBike(bike.getId()).isAvailable());
        assertNull(service.getBike(bike.getId()).getCustomerId());
    }

    @Test
    void regularCustomerCanRentAgainAfterReturn() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike first = service.createBike("Stels");
        Bike second = service.createBike("Trek");
        service.rentBike(customer.getId(), first.getId());

        assertThrows(IllegalStateException.class, () -> service.rentBike(customer.getId(), second.getId()));
        assertTrue(service.getBike(second.getId()).isAvailable());

        service.returnBike(customer.getId(), first.getId());
        service.rentBike(customer.getId(), second.getId());
        assertFalse(service.getBike(second.getId()).isAvailable());
        assertTrue(service.getBike(first.getId()).isAvailable());
    }

    @Test
    void premiumCustomerCannotRentFourthBike() {
        Customer customer = service.createCustomer("Anna", "premium");
        Bike first = service.createBike("First");
        Bike second = service.createBike("Second");
        Bike third = service.createBike("Third");
        Bike fourth = service.createBike("Fourth");
        service.rentBike(customer.getId(), first.getId());
        service.rentBike(customer.getId(), second.getId());
        service.rentBike(customer.getId(), third.getId());

        assertThrows(IllegalStateException.class, () -> service.rentBike(customer.getId(), fourth.getId()));
        assertEquals(3, service.getBikes(null, false).size());
        assertTrue(service.getBike(fourth.getId()).isAvailable());
    }

    @Test
    void occupiedBikeCannotBeRentedAgain() {
        Customer first = service.createCustomer("Oleg", "regular");
        Customer second = service.createCustomer("Anna", "premium");
        Bike bike = service.createBike("Stels");
        service.rentBike(first.getId(), bike.getId());

        assertThrows(IllegalStateException.class, () -> service.rentBike(first.getId(), bike.getId()));
        assertThrows(IllegalStateException.class, () -> service.rentBike(second.getId(), bike.getId()));
        assertEquals(Long.valueOf(first.getId()), service.getBike(bike.getId()).getCustomerId());
    }

    @Test
    void anotherCustomerCannotReturnBike() {
        Customer first = service.createCustomer("Oleg", "regular");
        Customer second = service.createCustomer("Anna", "premium");
        Bike bike = service.createBike("Stels");
        service.rentBike(first.getId(), bike.getId());

        assertThrows(IllegalStateException.class, () -> service.returnBike(second.getId(), bike.getId()));
        assertFalse(service.getBike(bike.getId()).isAvailable());
        assertEquals(Long.valueOf(first.getId()), service.getBike(bike.getId()).getCustomerId());
    }

    @Test
    void availableBikeCannotBeReturned() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike bike = service.createBike("Stels");

        assertThrows(IllegalStateException.class, () -> service.returnBike(customer.getId(), bike.getId()));
        service.rentBike(customer.getId(), bike.getId());
        service.returnBike(customer.getId(), bike.getId());
        assertThrows(IllegalStateException.class, () -> service.returnBike(customer.getId(), bike.getId()));
        assertTrue(service.getBike(bike.getId()).isAvailable());
    }

    @Test
    void rentalNeedsExistingBikeAndCustomer() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike bike = service.createBike("Stels");

        assertThrows(NotFoundException.class, () -> service.rentBike(99, bike.getId()));
        assertThrows(NotFoundException.class, () -> service.rentBike(customer.getId(), 99));
        assertThrows(NotFoundException.class, () -> service.returnBike(99, bike.getId()));
        assertThrows(NotFoundException.class, () -> service.returnBike(customer.getId(), 99));
        assertTrue(service.getBike(bike.getId()).isAvailable());
    }

    @Test
    void rentedBikeAndItsCustomerCannotBeDeleted() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike bike = service.createBike("Stels");
        service.rentBike(customer.getId(), bike.getId());

        assertThrows(IllegalStateException.class, () -> service.deleteBike(bike.getId()));
        assertThrows(IllegalStateException.class, () -> service.deleteCustomer(customer.getId()));
        assertEquals(1, service.getBikes().size());
        assertEquals(1, service.getCustomers().size());

        service.returnBike(customer.getId(), bike.getId());
        service.deleteBike(bike.getId());
        service.deleteCustomer(customer.getId());
        assertTrue(service.getBikes().isEmpty());
        assertTrue(service.getCustomers().isEmpty());
    }

    @Test
    void rentalDoesNotChangeOtherResourcesOrPreviousResponses() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike first = service.createBike("Stels");
        Bike second = service.createBike("Trek");
        List<Bike> before = service.getBikes();

        Bike rented = service.rentBike(customer.getId(), first.getId());
        service.returnBike(customer.getId(), first.getId());

        assertTrue(first.isAvailable());
        assertTrue(before.stream().allMatch(Bike::isAvailable));
        assertFalse(rented.isAvailable());
        assertEquals("Trek", service.getBike(second.getId()).getModel());
        assertTrue(service.getBike(second.getId()).isAvailable());
    }
}
