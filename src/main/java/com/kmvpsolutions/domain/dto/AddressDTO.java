package com.kmvpsolutions.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String address1;
    private String address2;
    private String city;
    private String postcode;

    @Size(min = 2, max = 2)
    private String country;
}
