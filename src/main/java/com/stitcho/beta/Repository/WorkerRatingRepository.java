package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.WorkerRating;

@Repository
public interface WorkerRatingRepository extends JpaRepository<WorkerRating, Long> {
    
    // Find all ratings for a worker
    List<WorkerRating> findByWorker_Id(Long workerId);
    
    // Find rating by worker and order
    Optional<WorkerRating> findByWorker_IdAndOrder_OrderId(Long workerId, Long orderId);
    
    // Check if rating exists for order
    boolean existsByWorker_IdAndOrder_OrderId(Long workerId, Long orderId);
    
    // Get average rating for a worker
    @Query("SELECT AVG(wr.rating) FROM WorkerRating wr WHERE wr.worker.id = :workerId")
    Double getAverageRatingByWorkerId(@Param("workerId") Long workerId);
    
    // Get rating count for a worker
    @Query("SELECT COUNT(wr) FROM WorkerRating wr WHERE wr.worker.id = :workerId")
    Long getRatingCountByWorkerId(@Param("workerId") Long workerId);
}
