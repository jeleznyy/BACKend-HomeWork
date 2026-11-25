package app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final RentalService service;

    public CustomerController(RentalService service) {
        this.service = service;
    }

    @GetMapping
    public List<Customer> list(@RequestParam(name = "name", required = false) String name,
                               @RequestParam(name = "type", required = false) String type) {
        return service.getCustomers(name, type);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable @Positive long id) {
        return service.getCustomer(id);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = service.createCustomer(request.name(), request.type());
        return ResponseEntity.created(URI.create("/customers/" + customer.getId())).body(customer);
    }

    @PostMapping(path = "/with-rental", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<RentalService.RentalReceipt> createWithRental(
            @Valid @RequestBody CustomerRentalRequest request) {
        var receipt = service.createCustomerWithRental(request.name(), request.type(), request.bikeId());
        return ResponseEntity.created(URI.create("/customers/" + receipt.customer().getId())).body(receipt);
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public Customer update(@PathVariable @Positive long id, @Valid @RequestBody CustomerRequest request) {
        return service.updateCustomer(id, request.name(), request.type());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long id) {
        service.deleteCustomer(id);
    }

    @PostMapping(path = "/{id}/rentals", consumes = APPLICATION_JSON_VALUE)
    public Bike rent(@PathVariable @Positive long id, @Valid @RequestBody RentalRequest request) {
        return service.rentBike(id, request.bikeId());
    }

    @PostMapping(path = "/{id}/returns", consumes = APPLICATION_JSON_VALUE)
    public Bike returnBike(@PathVariable @Positive long id, @Valid @RequestBody RentalRequest request) {
        return service.returnBike(id, request.bikeId());
    }

    public record CustomerRequest(@NotBlank String name, @NotBlank String type) {
    }

    public record CustomerRentalRequest(@NotBlank String name, @NotBlank String type,
                                        @NotNull @Positive Long bikeId) {
    }

    public record RentalRequest(@NotNull @Positive Long bikeId) {
    }
}
