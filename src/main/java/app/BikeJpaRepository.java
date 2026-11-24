package app;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface BikeJpaRepository extends JpaRepository<BikeEntity, Long> {
    @Query("select b from BikeEntity b left join fetch b.customer order by b.id")
    List<BikeEntity> findAllWithCustomers();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BikeEntity b where b.id = :id")
    Optional<BikeEntity> findForUpdate(@Param("id") long id);

    long countByCustomer_Id(long customerId);
}
