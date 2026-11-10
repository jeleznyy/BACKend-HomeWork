package app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<Customer> list(HttpServletRequest http) {
        var params = RequestParameters.query(http, "name", "type");
        return service.getCustomers(params.get("name"), params.get("type"));
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable String id, HttpServletRequest http) {
        RequestParameters.query(http);
        return service.getCustomer(RequestParameters.positiveId(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest request,
                                           HttpServletRequest http) {
        RequestParameters.query(http);
        Customer customer = service.createCustomer(request.name(), request.type());
        return ResponseEntity.created(URI.create("/customers/" + customer.getId())).body(customer);
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public Customer update(@PathVariable String id, @Valid @RequestBody CustomerRequest request,
                           HttpServletRequest http) {
        RequestParameters.query(http);
        return service.updateCustomer(RequestParameters.positiveId(id), request.name(), request.type());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, HttpServletRequest http) {
        RequestParameters.query(http);
        service.deleteCustomer(RequestParameters.positiveId(id));
    }

    public record CustomerRequest(@NotBlank String name, @NotBlank String type) {
    }
}
