package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentalService {
    private final Map<Long, Bike> bikes = new HashMap<>();
    private final Map<Long, Customer> customers = new HashMap<>();
    private long nextBikeId = 1;
    private long nextCustomerId = 1;

    public Bike createBike(String model) {
        Bike bike = new Bike(nextBikeId, checkText(model, "model"));
        bikes.put(nextBikeId, bike);
        nextBikeId++;
        return bike;
    }

    public Bike getBike(long id) {
        Bike bike = bikes.get(id);
        if (bike == null) {
            throw new NotFoundException("Велосипед не найден");
        }
        return bike;
    }

    public List<Bike> getBikes() {
        return new ArrayList<>(bikes.values());
    }

    public Bike updateBike(long id, String model) {
        Bike bike = getBike(id);
        bike.rename(checkText(model, "model"));
        return bike;
    }

    public void deleteBike(long id) {
        getBike(id);
        bikes.remove(id);
    }

    public Customer createCustomer(String name, String type) {
        Customer customer = makeCustomer(nextCustomerId, name, type);
        customers.put(nextCustomerId, customer);
        nextCustomerId++;
        return customer;
    }

    public Customer getCustomer(long id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new NotFoundException("Клиент не найден");
        }
        return customer;
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Customer updateCustomer(long id, String name, String type) {
        getCustomer(id);
        Customer customer = makeCustomer(id, name, type);
        customers.put(id, customer);
        return customer;
    }

    public void deleteCustomer(long id) {
        getCustomer(id);
        customers.remove(id);
    }

    private Customer makeCustomer(long id, String name, String type) {
        name = checkText(name, "name");
        if ("regular".equals(type)) {
            return new RegularCustomer(id, name);
        }
        if ("premium".equals(type)) {
            return new PremiumCustomer(id, name);
        }
        throw new IllegalArgumentException("type должен быть regular или premium");
    }

    private String checkText(String text, String field) {
        if (text == null || text.isBlank() || text.length() > 100) {
            throw new IllegalArgumentException(field + ": нужна непустая строка до 100 символов");
        }
        return text.trim();
    }
}
