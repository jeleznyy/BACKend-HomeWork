package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InMemoryRentalRepository implements RentalRepository {
    private final Map<Long, Bike> bikes = new HashMap<>();
    private final Map<Long, Customer> customers = new HashMap<>();
    private long nextBikeId = 1;
    private long nextCustomerId = 1;

    @Override
    public Bike createBike(String model) {
        Bike bike = new Bike(nextBikeId++, model);
        saveBike(bike);
        return bike;
    }

    @Override
    public Bike findBike(long id) {
        return bikes.get(id);
    }

    @Override
    public Bike findBikeForUpdate(long id) {
        return findBike(id);
    }

    @Override
    public List<Bike> findAllBikes() {
        return new ArrayList<>(bikes.values());
    }

    @Override
    public void saveBike(Bike bike) {
        bikes.put(bike.getId(), bike);
    }

    @Override
    public void deleteBike(long id) {
        bikes.remove(id);
    }

    @Override
    public Customer createCustomer(String name, String type) {
        Customer customer = Customer.of(nextCustomerId++, name, type);
        saveCustomer(customer);
        return customer;
    }

    @Override
    public Customer findCustomer(long id) {
        return customers.get(id);
    }

    @Override
    public Customer findCustomerForUpdate(long id) {
        return findCustomer(id);
    }

    @Override
    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public void saveCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    @Override
    public void deleteCustomer(long id) {
        customers.remove(id);
    }

    @Override
    public long countRentedBikes(long customerId) {
        return bikes.values().stream()
                .filter(bike -> Long.valueOf(customerId).equals(bike.getCustomerId()))
                .count();
    }
}
