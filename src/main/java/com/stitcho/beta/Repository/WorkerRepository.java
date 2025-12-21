package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Worker;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByShop_ShopId(Long shopId);
    
    @Query("SELECT w FROM Worker w WHERE w.shop.shopId = :shopId AND LOWER(w.user.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Worker> findByShopIdAndUserNameContaining(@Param("shopId") Long shopId, @Param("name") String name);
    
    Optional<Worker> findByUser_Id(Long userId);
}

