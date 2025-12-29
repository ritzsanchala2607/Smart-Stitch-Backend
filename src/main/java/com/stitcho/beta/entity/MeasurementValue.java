package com.stitcho.beta.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "measurement_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "measurement_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private MeasurementProfile profile;

    @Column(name = "measurement_key", nullable = false, length = 100)
    private String measurementKey;

    @Column(name = "measurement_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal measurementValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for convenience
    public MeasurementValue(String measurementKey, BigDecimal measurementValue) {
        this.measurementKey = measurementKey.toLowerCase();
        this.measurementValue = measurementValue;
    }

    public MeasurementValue(String measurementKey, Double measurementValue) {
        this.measurementKey = measurementKey.toLowerCase();
        this.measurementValue = BigDecimal.valueOf(measurementValue);
    }

    // Ensure key is always lowercase
    public void setMeasurementKey(String measurementKey) {
        this.measurementKey = measurementKey != null ? measurementKey.toLowerCase() : null;
    }
}
