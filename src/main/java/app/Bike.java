package app;

public class Bike {
    private final long id;
    private final String model;
    private final Long customerId;

    public Bike(long id, String model) {
        this(id, model, null);
    }

    private Bike(long id, String model, Long customerId) {
        this.id = id;
        this.model = model;
        this.customerId = customerId;
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

    Bike withModel(String model) {
        return new Bike(id, model, customerId);
    }

    Bike rentTo(long customerId) {
        return new Bike(id, model, customerId);
    }

    Bike returnToShop() {
        return new Bike(id, model, null);
    }
}
