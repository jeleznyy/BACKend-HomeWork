package app;

public class Bike {
    private final long id;
    private String model;
    private Long customerId;

    public Bike(long id, String model) {
        this.id = id;
        this.model = model;
    }

    public long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public boolean isAvailable() {
        return customerId == null;
    }

    void rename(String model) {
        this.model = model;
    }

    void rentTo(long customerId) {
        this.customerId = customerId;
    }

    void returnToShop() {
        customerId = null;
    }
}
