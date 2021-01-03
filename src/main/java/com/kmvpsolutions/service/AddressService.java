package com.kmvpsolutions.service;

import com.kmvpsolutions.domain.Address;
import com.kmvpsolutions.domain.dto.AddressDTO;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@Slf4j
@Transactional
@ApplicationScoped
public class AddressService {

    public static Address createFromDTO(AddressDTO addressDTO) {
        return new Address(
                addressDTO.getAddress1(),
                addressDTO.getAddress2(),
                addressDTO.getCity(),
                addressDTO.getPostcode(),
                addressDTO.getCountry()
        );
    }

    public static AddressDTO mapToDTO(Address address) {
        return new AddressDTO(
                address.getAddress1(),
                address.getAddress2(),
                address.getCity(),
                address.getPostcode(),
                address.getCountry()
        );
    }
}
