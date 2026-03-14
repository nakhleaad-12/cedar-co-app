package com.cedarco.controller;

import com.cedarco.dto.UserDto;
import com.cedarco.entity.User;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto.FullResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.getMe(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto.Response> updateProfile(@AuthenticationPrincipal UserDetails userDetails, 
                                                         @RequestBody UserDto.ProfileUpdateRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails, 
                                               @RequestBody UserDto.PasswordChangeRequest request) {
        User user = getUser(userDetails);
        userService.changePassword(user, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<UserDto.AddressResponse> addAddress(@AuthenticationPrincipal UserDetails userDetails, 
                                                              @RequestBody UserDto.AddressRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.addAddress(user, request));
    }

    @PutMapping("/me/addresses/{id}")
    public ResponseEntity<UserDto.AddressResponse> updateAddress(@AuthenticationPrincipal UserDetails userDetails, 
                                                                 @PathVariable Long id, 
                                                                 @RequestBody UserDto.AddressRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(userService.updateAddress(user, id, request));
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserDetails userDetails, 
                                              @PathVariable Long id) {
        User user = getUser(userDetails);
        userService.deleteAddress(user, id);
        return ResponseEntity.ok().build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
