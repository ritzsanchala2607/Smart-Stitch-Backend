package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long ratingId;
    private Long orderId;
    private String customerName;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
}
