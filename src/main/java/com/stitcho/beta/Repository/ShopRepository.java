package com.stitcho.beta.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
}
