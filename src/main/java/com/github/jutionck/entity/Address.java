package com.github.jutionck.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
