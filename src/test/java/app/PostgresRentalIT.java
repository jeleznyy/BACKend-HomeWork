package app;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class PostgresRentalIT {
    @Autowired
    private RentalService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DynamicPropertySource
    static void testDatabase(DynamicPropertyRegistry properties) {
        String url = System.getenv("TEST_DB_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Укажи TEST_DB_URL для отдельной базы тестов");
        }
        properties.add("spring.datasource.url", () -> url);
    }

    @Test
    void bikeListLoadsFortyCustomersInOneQuery() {
        String model = "Test list " + UUID.randomUUID();
        Bike free = service.createBike(model);
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        long before = statistics.getPrepareStatementCount();

        var smallList = service.getBikesWithCustomers(model, null);

        assertEquals(1L, statistics.getPrepareStatementCount() - before);
        assertEquals(1, smallList.size());

        var rentals = new ArrayList<RentalService.RentalReceipt>();
        for (int i = 0; i < 40; i++) {
            Bike bike = service.createBike(model);
            rentals.add(service.createCustomerWithRental("Rider " + i, "regular", bike.getId()));
        }
        before = statistics.getPrepareStatementCount();

        var bikes = service.getBikesWithCustomers(model, null);

        assertEquals(1L, statistics.getPrepareStatementCount() - before);
        assertEquals(41, bikes.size());
        assertEquals(free.getId(), bikes.getFirst().id());
        assertNull(bikes.getFirst().customer());
        assertNull(bikes.getFirst().rentedAt());
        for (int i = 0; i < rentals.size(); i++) {
            var rental = rentals.get(i);
            var item = bikes.get(i + 1);
            assertEquals(rental.bike().getId(), item.id());
            assertEquals(rental.customer().getId(), item.customer().getId());
            assertEquals("Rider " + i, item.customer().getName());
            assertNotNull(item.rentedAt());

            service.returnBike(rental.customer().getId(), rental.bike().getId());
            service.deleteBike(rental.bike().getId());
            service.deleteCustomer(rental.customer().getId());
        }
        service.deleteBike(free.getId());
    }

    @Test
    void rentalAndReturnSaveCustomerLinkInDatabase() {
        long customerId = service.createCustomer("Anna", "regular").getId();
        long bikeId = service.createBike("Stels").getId();

        service.rentBike(customerId, bikeId);

        assertEquals(Long.valueOf(customerId), service.getBike(bikeId).getCustomerId());
        assertEquals("Anna", jdbc.queryForObject("""
                select c.name from bikes b join customers c on b.customer_id = c.id where b.id = ?
                """, String.class, bikeId));
        assertThrows(IllegalStateException.class, () -> service.deleteCustomer(customerId));
        OffsetDateTime rentedAt = rentedAt(bikeId);
        assertNotNull(rentedAt);

        service.updateBike(bikeId, "Trek");
        assertEquals(rentedAt, rentedAt(bikeId));

        service.returnBike(customerId, bikeId);

        assertNull(jdbc.queryForObject("select customer_id from bikes where id = ?", Long.class, bikeId));
        assertNull(rentedAt(bikeId));
        service.deleteBike(bikeId);
        service.deleteCustomer(customerId);
    }

    @Test
    void occupiedBikeRollsBackNewCustomer() {
        long bikeId = service.createBike("Stels").getId();
        var rental = service.createCustomerWithRental("First rider", "regular", bikeId);
        long customerId = rental.customer().getId();
        OffsetDateTime rentedAt = rentedAt(bikeId);
        assertEquals(Long.valueOf(customerId), service.getBike(bikeId).getCustomerId());
        assertNotNull(rentedAt);

        assertThrows(IllegalStateException.class,
                () -> service.createCustomerWithRental("Rejected rider", "premium", bikeId));

        assertTrue(service.getCustomers("Rejected rider", null).isEmpty());
        assertEquals(Long.valueOf(customerId), service.getBike(bikeId).getCustomerId());
        assertEquals(rentedAt, rentedAt(bikeId));
        service.returnBike(customerId, bikeId);
        service.deleteBike(bikeId);
        service.deleteCustomer(customerId);
    }

    @Test
    void concurrentRentalsRespectCustomerLimit() throws Exception {
        long customerId = service.createCustomer("Oleg", "regular").getId();
        long firstBike = service.createBike("Stels").getId();
        long secondBike = service.createBike("Trek").getId();
        CyclicBarrier start = new CyclicBarrier(2);

        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> tryRent(start, customerId, firstBike));
            var second = pool.submit(() -> tryRent(start, customerId, secondBike));

            boolean firstRented = first.get(15, TimeUnit.SECONDS);
            boolean secondRented = second.get(15, TimeUnit.SECONDS);
            assertNotEquals(firstRented, secondRented);
            assertEquals(1L, jdbc.queryForObject(
                    "select count(*) from bikes where customer_id = ?", Long.class, customerId));

            service.returnBike(customerId, firstRented ? firstBike : secondBike);
            service.deleteBike(firstBike);
            service.deleteBike(secondBike);
            service.deleteCustomer(customerId);
        }
    }

    private boolean tryRent(CyclicBarrier start, long customerId, long bikeId) throws Exception {
        start.await(5, TimeUnit.SECONDS);
        try {
            service.rentBike(customerId, bikeId);
            return true;
        } catch (IllegalStateException rejected) {
            return false;
        }
    }

    private OffsetDateTime rentedAt(long bikeId) {
        return jdbc.queryForObject("select rented_at from bikes where id = ?",
                OffsetDateTime.class, bikeId);
    }
}
