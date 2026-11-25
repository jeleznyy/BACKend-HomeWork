package app;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RentalService {
    private final RentalRepository repository;

    RentalService(RentalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Bike createBike(String model) {
        return repository.createBike(checkText(model, "model"));
    }

    public Bike getBike(long id) {
        return requireBike(repository.findBike(id));
    }

    public List<Bike> getBikes() {
        return getBikes(null, null);
    }

    public List<Bike> getBikes(String model, Boolean available) {
        List<Bike> result = new ArrayList<>();
        for (Bike bike : repository.findAllBikes()) {
            if (matchesBike(bike.getModel(), bike.isAvailable(), model, available)) {
                result.add(bike);
            }
        }
        return result;
    }

    public List<BikeWithCustomer> getBikesWithCustomers(String model, Boolean available) {
        return repository.findAllBikesWithCustomers().stream()
                .filter(bike -> matchesBike(bike.model(), bike.available(), model, available))
                .toList();
    }

    @Transactional
    public Bike updateBike(long id, String model) {
        Bike bike = requireBike(repository.findBikeForUpdate(id));
        Bike updated = bike.withModel(checkText(model, "model"));
        repository.saveBike(updated);
        return updated;
    }

    @Transactional
    public void deleteBike(long id) {
        Bike bike = requireBike(repository.findBikeForUpdate(id));
        if (!bike.isAvailable()) {
            throw new IllegalStateException("Нельзя удалить велосипед, пока он в прокате");
        }
        repository.deleteBike(id);
    }

    @Transactional
    public Customer createCustomer(String name, String type) {
        return repository.createCustomer(checkText(name, "name"), checkType(type));
    }

    @Transactional
    public RentalReceipt createCustomerWithRental(String name, String type, long bikeId) {
        Customer customer = createCustomer(name, type);
        Bike bike = rentBike(customer.getId(), bikeId);
        return new RentalReceipt(customer, bike);
    }

    public record RentalReceipt(Customer customer, Bike bike) {
    }

    public Customer getCustomer(long id) {
        return requireCustomer(repository.findCustomer(id));
    }

    public List<Customer> getCustomers() {
        return getCustomers(null, null);
    }

    public List<Customer> getCustomers(String name, String type) {
        if (type != null) {
            checkType(type);
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

    @Transactional
    public Customer updateCustomer(long id, String name, String type) {
        requireCustomer(repository.findCustomerForUpdate(id));
        Customer customer = Customer.of(id, checkText(name, "name"), checkType(type));
        if (repository.countRentedBikes(id) > customer.getBikeLimit()) {
            throw new IllegalStateException("Сначала верните лишние велосипеды, затем меняйте тариф");
        }
        repository.saveCustomer(customer);
        return customer;
    }

    @Transactional
    public void deleteCustomer(long id) {
        requireCustomer(repository.findCustomerForUpdate(id));
        if (repository.countRentedBikes(id) > 0) {
            throw new IllegalStateException("Нельзя удалить клиента, пока он не вернул велосипеды");
        }
        repository.deleteCustomer(id);
    }

    @Transactional
    public Bike rentBike(long customerId, long bikeId) {
        Customer customer = requireCustomer(repository.findCustomerForUpdate(customerId));
        Bike bike = requireBike(repository.findBikeForUpdate(bikeId));
        if (!bike.isAvailable()) {
            throw new IllegalStateException("Велосипед уже выдан");
        }
        if (repository.countRentedBikes(customerId) >= customer.getBikeLimit()) {
            throw new IllegalStateException("Достигнут лимит велосипедов для этого тарифа");
        }
        Bike rented = bike.rentTo(customerId);
        repository.saveBike(rented);
        return rented;
    }

    @Transactional
    public Bike returnBike(long customerId, long bikeId) {
        requireCustomer(repository.findCustomerForUpdate(customerId));
        Bike bike = requireBike(repository.findBikeForUpdate(bikeId));
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

    private boolean matchesBike(String bikeModel, boolean bikeAvailable, String model, Boolean available) {
        return (model == null || bikeModel.contains(model))
                && (available == null || bikeAvailable == available);
    }

    private Bike requireBike(Bike bike) {
        if (bike == null) {
            throw new NotFoundException("Велосипед не найден");
        }
        return bike;
    }

    private Customer requireCustomer(Customer customer) {
        if (customer == null) {
            throw new NotFoundException("Клиент не найден");
        }
        return customer;
    }

    private String checkType(String type) {
        if (!"regular".equals(type) && !"premium".equals(type)) {
            throw new IllegalArgumentException("type должен быть regular или premium");
        }
        return type;
    }

    private String checkText(String text, String field) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.isBlank() || normalized.length() > 100) {
            throw new IllegalArgumentException(field + ": нужна непустая строка до 100 символов");
        }
        return normalized;
    }
}
