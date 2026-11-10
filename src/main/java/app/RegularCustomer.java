package app;

public class RegularCustomer extends Customer {
    public RegularCustomer(long id, String name) {
        super(id, name);
    }

    @Override
    public String getType() {
        return "regular";
    }

    @Override
    public int getBikeLimit() {
        return 1;
    }
}
