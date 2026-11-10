package app;

public class PremiumCustomer extends Customer {
    public PremiumCustomer(long id, String name) {
        super(id, name);
    }

    @Override
    public String getType() {
        return "premium";
    }

    @Override
    public int getBikeLimit() {
        return 3;
    }
}
