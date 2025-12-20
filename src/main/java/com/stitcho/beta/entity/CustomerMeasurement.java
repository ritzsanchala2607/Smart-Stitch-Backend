package com.stitcho.beta.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Double chest;

    private Double shoulder;

    private Double shirtLength;

    private Double waist;

    private Double pantLength;

    private Double hip;

    private String customMeasurements;
}
