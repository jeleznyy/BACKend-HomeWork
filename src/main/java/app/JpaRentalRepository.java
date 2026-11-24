package app;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaRentalRepository implements RentalRepository {
    private final BikeJpaRepository bikes;
    private final CustomerJpaRepository customers;

    JpaRentalRepository(BikeJpaRepository bikes, CustomerJpaRepository customers) {
        this.bikes = bikes;
        this.customers = customers;
    }

    @Override
    public Bike createBike(String model) {
        return bikes.save(new BikeEntity(model)).toBike();
    }

    @Override
    public Bike findBike(long id) {
        return bikes.findById(id).map(BikeEntity::toBike).orElse(null);
    }

    @Override
    public Bike findBikeForUpdate(long id) {
        return bikes.findForUpdate(id).map(BikeEntity::toBike).orElse(null);
    }

    @Override
    public List<Bike> findAllBikes() {
        return bikes.findAllWithCustomers().stream().map(BikeEntity::toBike).toList();
    }

    @Override
    public void saveBike(Bike bike) {
        BikeEntity entity = bikes.findById(bike.getId()).orElseThrow();
        Long customerId = bike.getCustomerId();
        CustomerEntity customer = customerId == null ? null : customers.getReferenceById(customerId);
        entity.update(bike.getModel(), customer);
    }

    @Override
    public void deleteBike(long id) {
        bikes.deleteById(id);
    }

    @Override
    public Customer createCustomer(String name, String type) {
        return customers.save(new CustomerEntity(name, type)).toCustomer();
    }

    @Override
    public Customer findCustomer(long id) {
        return customers.findById(id).map(CustomerEntity::toCustomer).orElse(null);
    }

    @Override
    public Customer findCustomerForUpdate(long id) {
        return customers.findForUpdate(id).map(CustomerEntity::toCustomer).orElse(null);
    }

    @Override
    public List<Customer> findAllCustomers() {
        return customers.findAll(Sort.by("id")).stream().map(CustomerEntity::toCustomer).toList();
    }

    @Override
    public void saveCustomer(Customer customer) {
        CustomerEntity entity = customers.findById(customer.getId()).orElseThrow();
        entity.update(customer.getName(), customer.getType());
    }

    @Override
    public void deleteCustomer(long id) {
        customers.deleteById(id);
    }

    @Override
    public long countRentedBikes(long customerId) {
        return bikes.countByCustomer_Id(customerId);
    }
}
