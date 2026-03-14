package com.cedarco.service;

import com.cedarco.dto.UserDto;
import com.cedarco.entity.Address;
import com.cedarco.entity.User;
import com.cedarco.repository.AddressRepository;
import com.cedarco.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.FullResponse getMe(User user) {
        return mapToFullResponse(user);
    }

    @Transactional
    public UserDto.Response updateProfile(User user, UserDto.ProfileUpdateRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void changePassword(User user, UserDto.PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserDto.AddressResponse addAddress(User user, UserDto.AddressRequest request) {
        if (request.isDefault()) {
            clearDefaultAddresses(user);
        }
        
        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .region(request.getState())
                .postalCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();
        
        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public UserDto.AddressResponse updateAddress(User user, Long addressId, UserDto.AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (request.isDefault()) {
            clearDefaultAddresses(user);
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setRegion(request.getState());
        address.setPostalCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());
        
        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        addressRepository.delete(address);
    }

    private void clearDefaultAddresses(User user) {
        List<Address> addresses = addressRepository.findByUser(user);
        for (Address a : addresses) {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        }
    }

    private UserDto.FullResponse mapToFullResponse(User user) {
        UserDto.FullResponse resp = new UserDto.FullResponse();
        resp.setId(user.getId());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setRole(user.getRole().name());
        resp.setActive(user.isActive());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setAddresses(user.getAddresses().stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList()));
        return resp;
    }

    private UserDto.Response mapToResponse(User user) {
        UserDto.Response resp = new UserDto.Response();
        resp.setId(user.getId());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setRole(user.getRole().name());
        resp.setActive(user.isActive());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }

    private UserDto.AddressResponse mapToAddressResponse(Address address) {
        UserDto.AddressResponse resp = new UserDto.AddressResponse();
        resp.setId(address.getId());
        resp.setStreet(address.getStreet());
        resp.setCity(address.getCity());
        resp.setState(address.getRegion());
        resp.setZipCode(address.getPostalCode());
        resp.setCountry(address.getCountry());
        resp.setDefault(address.isDefault());
        return resp;
    }
}
