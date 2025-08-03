package com.people.manager.application.module.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address", schema = "people")
public class Address {

    @Id
    @Column(name = "person_id")
    private Long id;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(length = 255)
    private String street;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    private Double latitude;
    private Double longitude;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
