package app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("/bikes")
public class BikeController {
    private final RentalService service;

    public BikeController(RentalService service) {
        this.service = service;
    }

    @GetMapping
    public List<Bike> list(
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "available", required = false) @Pattern(regexp = "true|false") String available) {
        return service.getBikes(model, available == null ? null : Boolean.valueOf(available));
    }

    @GetMapping("/with-customers")
    public List<BikeWithCustomer> listWithCustomers(
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "available", required = false) @Pattern(regexp = "true|false") String available) {
        return service.getBikesWithCustomers(model, available == null ? null : Boolean.valueOf(available));
    }

    @GetMapping("/{id}")
    public Bike get(@PathVariable @Positive long id) {
        return service.getBike(id);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Bike> create(@Valid @RequestBody BikeRequest request) {
        Bike bike = service.createBike(request.model());
        return ResponseEntity.created(URI.create("/bikes/" + bike.getId())).body(bike);
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public Bike update(@PathVariable @Positive long id, @Valid @RequestBody BikeRequest request) {
        return service.updateBike(id, request.model());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long id) {
        service.deleteBike(id);
    }

    public record BikeRequest(@NotBlank String model) {
    }
}
