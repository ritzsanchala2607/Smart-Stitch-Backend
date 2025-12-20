package com.stitcho.beta.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkerRequest {
    @NotNull(message = "User details are required")
    @Valid
    private UserDto user;

    @NotNull(message = "Worker details are required")
    @Valid
    private WorkerDto worker;

    @NotEmpty(message = "At least one rate is required")
    @Valid
    private List<RateDto> rates;
}
