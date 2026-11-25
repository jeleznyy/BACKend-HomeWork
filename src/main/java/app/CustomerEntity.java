package app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String type;

    protected CustomerEntity() {
    }

    CustomerEntity(String name, String type) {
        update(name, type);
    }

    public Long getId() {
        return id;
    }

    void update(String name, String type) {
        this.name = name;
        this.type = type;
    }

    Customer toCustomer() {
        return Customer.of(id, name, type);
    }
}
