package app;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
class RentalRepository {
    private final Map<Long, Bike> bikes = new HashMap<>();
    private final Map<Long, Customer> customers = new HashMap<>();
    private long nextBikeId = 1;
    private long nextCustomerId = 1;

    Bike createBike(String model) {
        Bike bike = new Bike(nextBikeId++, model);
        saveBike(bike);
        return bike;
    }

    Bike findBike(long id) {
        return bikes.get(id);
    }

    List<Bike> findAllBikes() {
        return new ArrayList<>(bikes.values());
    }

    void saveBike(Bike bike) {
        bikes.put(bike.getId(), bike);
    }

    void deleteBike(long id) {
        bikes.remove(id);
    }

    long nextCustomerId() {
        return nextCustomerId;
    }

    void addCustomer(Customer customer) {
        saveCustomer(customer);
        nextCustomerId++;
    }

    Customer findCustomer(long id) {
        return customers.get(id);
    }

    List<Customer> findAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    void saveCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    void deleteCustomer(long id) {
        customers.remove(id);
    }
}
