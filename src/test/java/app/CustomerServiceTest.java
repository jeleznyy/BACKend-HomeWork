package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerServiceTest {
    private RentalService service;

    @BeforeEach
    void setUp() {
        service = new RentalService(new RentalRepository());
    }

    @Test
    void createsBothCustomerTypes() {
        Customer regular = service.createCustomer(" Oleg ", "regular");
        Customer premium = service.createCustomer("Anna", "premium");

        assertEquals("Oleg", service.getCustomer(regular.getId()).getName());
        assertEquals("regular", regular.getType());
        assertEquals(1, regular.getBikeLimit());
        assertEquals("premium", premium.getType());
        assertEquals(3, premium.getBikeLimit());
        assertEquals(2, service.getCustomers().size());
    }

    @Test
    void rejectsUnknownCustomerTypes() {
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer("Oleg", null));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer("Oleg", ""));
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer("Oleg", "vip"));
        assertThrows(IllegalArgumentException.class, () -> service.getCustomers(null, "vip"));
        assertTrue(service.getCustomers().isEmpty());
    }

    @Test
    void filtersCustomersByNameAndType() {
        Customer regular = service.createCustomer("Anna", "regular");
        service.createCustomer("Anna Maria", "premium");
        service.createCustomer("Oleg", "premium");

        assertEquals(List.of(regular), service.getCustomers("Anna", "regular"));
        assertEquals(2, service.getCustomers("Anna", null).size());
        assertEquals(2, service.getCustomers(null, "premium").size());
        assertTrue(service.getCustomers("Unknown", null).isEmpty());
    }

    @Test
    void downgradeIsRejectedUntilExtraBikesAreReturned() {
        Customer customer = service.createCustomer("Anna", "premium");
        Bike first = service.createBike("Stels");
        Bike second = service.createBike("Trek");
        service.rentBike(customer.getId(), first.getId());
        service.rentBike(customer.getId(), second.getId());

        assertThrows(IllegalStateException.class,
                () -> service.updateCustomer(customer.getId(), "Changed", "regular"));
        assertEquals("Anna", service.getCustomer(customer.getId()).getName());
        assertEquals("premium", service.getCustomer(customer.getId()).getType());
        assertEquals(2, service.getBikes(null, false).size());

        service.returnBike(customer.getId(), second.getId());
        Customer updated = service.updateCustomer(customer.getId(), " Anna Maria ", "regular");
        assertEquals(customer.getId(), updated.getId());
        assertEquals("Anna Maria", service.getCustomer(customer.getId()).getName());
        assertEquals("regular", service.getCustomer(customer.getId()).getType());
        assertEquals(Long.valueOf(customer.getId()), service.getBike(first.getId()).getCustomerId());
    }

    @Test
    void upgradeAllowsAdditionalRental() {
        Customer customer = service.createCustomer("Oleg", "regular");
        Bike first = service.createBike("Stels");
        Bike second = service.createBike("Trek");
        service.rentBike(customer.getId(), first.getId());

        service.updateCustomer(customer.getId(), "Oleg", "premium");
        service.rentBike(customer.getId(), second.getId());

        assertEquals(2, service.getBikes(null, false).size());
        assertEquals("premium", service.getCustomer(customer.getId()).getType());
    }

    @Test
    void invalidUpdatePreservesCustomer() {
        Customer customer = service.createCustomer("Oleg", "regular");

        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(customer.getId(), " ", "premium"));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCustomer(customer.getId(), "x".repeat(101), "premium"));
        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(customer.getId(), "Changed", "vip"));
        assertEquals("Oleg", service.getCustomer(customer.getId()).getName());
        assertEquals("regular", service.getCustomer(customer.getId()).getType());
    }
}
