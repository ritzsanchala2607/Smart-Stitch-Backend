package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {
    private Long workerId;
    private Long userId;
    private String name;
    private String email;
    private String contactNumber;
    private String workType;
    private Integer experience;
    private Double ratings;
    private List<RateInfo> rates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateInfo {
        private String workType;
        private Double rate;
    }
}
