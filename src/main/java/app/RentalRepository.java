package app;

import java.util.List;

interface RentalRepository {
    Bike createBike(String model);
    Bike findBike(long id);
    Bike findBikeForUpdate(long id);
    List<Bike> findAllBikes();
    List<BikeWithCustomer> findAllBikesWithCustomers();
    void saveBike(Bike bike);
    void deleteBike(long id);

    Customer createCustomer(String name, String type);
    Customer findCustomer(long id);
    Customer findCustomerForUpdate(long id);
    List<Customer> findAllCustomers();
    void saveCustomer(Customer customer);
    void deleteCustomer(long id);

    long countRentedBikes(long customerId);
}
