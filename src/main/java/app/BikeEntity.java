package app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "bikes")
public class BikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Column(name = "rented_at")
    private Instant rentedAt;

    protected BikeEntity() {
    }

    BikeEntity(String model) {
        this.model = model;
    }

    void update(String model, CustomerEntity customer) {
        if (customer == null) {
            rentedAt = null;
        } else if (this.customer == null) {
            rentedAt = Instant.now();
        }
        this.model = model;
        this.customer = customer;
    }

    Bike toBike() {
        return new Bike(id, model, customer == null ? null : customer.getId());
    }

    BikeWithCustomer toBikeWithCustomer() {
        return new BikeWithCustomer(id, model, customer == null, rentedAt,
                customer == null ? null : customer.toCustomer());
    }
}
