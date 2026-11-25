package app;

public abstract class Customer {
    private final long id;
    private final String name;

    public Customer(long id, String name) {
        this.id = id;
        this.name = name;
    }

    static Customer of(long id, String name, String type) {
        return switch (type) {
            case "regular" -> new RegularCustomer(id, name);
            case "premium" -> new PremiumCustomer(id, name);
            default -> throw new IllegalArgumentException("type должен быть regular или premium");
        };
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String getType();

    public abstract int getBikeLimit();
}
