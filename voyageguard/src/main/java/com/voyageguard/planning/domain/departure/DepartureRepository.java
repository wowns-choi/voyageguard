package com.voyageguard.planning.domain.departure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartureRepository extends JpaRepository<Departure, Long> {

    List<Departure> findByStatus(DepartureStatus status);
}
