package app;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {
    private final RentalRepository repository;

    RentalService(RentalRepository repository) {
        this.repository = repository;
    }

    public synchronized Bike createBike(String model) {
        return repository.createBike(checkText(model, "model"));
    }

    public synchronized Bike getBike(long id) {
        Bike bike = repository.findBike(id);
        if (bike == null) {
            throw new NotFoundException("Велосипед не найден");
        }
        return bike;
    }

    public List<Bike> getBikes() {
        return getBikes(null, null);
    }

    public synchronized List<Bike> getBikes(String model, Boolean available) {
        List<Bike> result = new ArrayList<>();
        for (Bike bike : repository.findAllBikes()) {
            if (model != null && !bike.getModel().contains(model)) {
                continue;
            }
            if (available != null && bike.isAvailable() != available) {
                continue;
            }
            result.add(bike);
        }
        return result;
    }

    public synchronized Bike updateBike(long id, String model) {
        Bike bike = getBike(id);
        Bike updated = bike.withModel(checkText(model, "model"));
        repository.saveBike(updated);
        return updated;
    }

    public synchronized void deleteBike(long id) {
        Bike bike = getBike(id);
        if (!bike.isAvailable()) {
            throw new IllegalStateException("Нельзя удалить велосипед, пока он в прокате");
        }
        repository.deleteBike(id);
    }

    public synchronized Customer createCustomer(String name, String type) {
        Customer customer = makeCustomer(repository.nextCustomerId(), name, type);
        repository.addCustomer(customer);
        return customer;
    }

    public synchronized Customer getCustomer(long id) {
        Customer customer = repository.findCustomer(id);
        if (customer == null) {
            throw new NotFoundException("Клиент не найден");
        }
        return customer;
    }

    public synchronized List<Customer> getCustomers() {
        return repository.findAllCustomers();
    }

    public synchronized List<Customer> getCustomers(String name, String type) {
        if (type != null && !type.equals("regular") && !type.equals("premium")) {
            throw new IllegalArgumentException("type должен быть regular или premium");
        }
        List<Customer> result = new ArrayList<>();
        for (Customer customer : repository.findAllCustomers()) {
            if (name != null && !customer.getName().contains(name)) {
                continue;
            }
            if (type != null && !customer.getType().equals(type)) {
                continue;
            }
            result.add(customer);
        }
        return result;
    }

    public synchronized Customer updateCustomer(long id, String name, String type) {
        getCustomer(id);
        Customer customer = makeCustomer(id, name, type);
        if (countRentedBikes(id) > customer.getBikeLimit()) {
            throw new IllegalStateException("Сначала верните лишние велосипеды, затем меняйте тариф");
        }
        repository.saveCustomer(customer);
        return customer;
    }

    public synchronized void deleteCustomer(long id) {
        getCustomer(id);
        if (countRentedBikes(id) > 0) {
            throw new IllegalStateException("Нельзя удалить клиента, пока он не вернул велосипеды");
        }
        repository.deleteCustomer(id);
    }

    public synchronized Bike rentBike(long customerId, long bikeId) {
        Customer customer = getCustomer(customerId);
        Bike bike = getBike(bikeId);
        if (!bike.isAvailable()) {
            throw new IllegalStateException("Велосипед уже выдан");
        }
        if (countRentedBikes(customerId) >= customer.getBikeLimit()) {
            throw new IllegalStateException("Достигнут лимит велосипедов для этого тарифа");
        }
        Bike rented = bike.rentTo(customerId);
        repository.saveBike(rented);
        return rented;
    }

    public synchronized Bike returnBike(long customerId, long bikeId) {
        getCustomer(customerId);
        Bike bike = getBike(bikeId);
        if (bike.isAvailable()) {
            throw new IllegalStateException("Этот велосипед уже находится в пункте проката");
        }
        if (bike.getCustomerId() != customerId) {
            throw new IllegalStateException("Велосипед выдан другому клиенту");
        }
        Bike returned = bike.returnToShop();
        repository.saveBike(returned);
        return returned;
    }

    private int countRentedBikes(long customerId) {
        int count = 0;
        for (Bike bike : repository.findAllBikes()) {
            if (bike.getCustomerId() != null && bike.getCustomerId() == customerId) {
                count++;
            }
        }
        return count;
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
        String normalized = text == null ? "" : text.trim();
        if (normalized.isBlank() || normalized.length() > 100) {
            throw new IllegalArgumentException(field + ": нужна непустая строка до 100 символов");
        }
        return normalized;
    }
}
