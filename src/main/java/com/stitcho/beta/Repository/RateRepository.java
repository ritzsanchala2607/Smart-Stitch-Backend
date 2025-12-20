package com.stitcho.beta.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Rate;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
    List<Rate> findByWorker_Id(Long workerId);
}
