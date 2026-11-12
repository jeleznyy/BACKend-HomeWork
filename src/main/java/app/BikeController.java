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
@RequestMapping("/bikes")
public class BikeController {
    private final RentalService service;

    public BikeController(RentalService service) {
        this.service = service;
    }

    @GetMapping
    public List<Bike> list(HttpServletRequest http) {
        var params = RequestParameters.query(http, "model", "available");
        String available = params.get("available");
        if (available != null && !available.equals("true") && !available.equals("false")) {
            throw new IllegalArgumentException("available должен быть true или false");
        }
        return service.getBikes(params.get("model"), available == null ? null : Boolean.valueOf(available));
    }

    @GetMapping("/{id}")
    public Bike get(@PathVariable String id, HttpServletRequest http) {
        RequestParameters.query(http);
        return service.getBike(RequestParameters.positiveId(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Bike> create(@Valid @RequestBody BikeRequest request,
                                       HttpServletRequest http) {
        RequestParameters.query(http);
        Bike bike = service.createBike(request.model());
        return ResponseEntity.created(URI.create("/bikes/" + bike.getId())).body(bike);
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public Bike update(@PathVariable String id, @Valid @RequestBody BikeRequest request,
                       HttpServletRequest http) {
        RequestParameters.query(http);
        return service.updateBike(RequestParameters.positiveId(id), request.model());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, HttpServletRequest http) {
        RequestParameters.query(http);
        service.deleteBike(RequestParameters.positiveId(id));
    }

    public record BikeRequest(@NotBlank String model) {
    }
}
