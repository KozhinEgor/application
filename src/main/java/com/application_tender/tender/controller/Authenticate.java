package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.User;
import com.application_tender.tender.service.JWTUtil;
import com.application_tender.tender.service.myUserDetailsService;
import com.application_tender.tender.subsidiaryModels.AuthenctionRequest;
import com.application_tender.tender.subsidiaryModels.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Authenticate {
    @Autowired
    private TableMapper tableMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private myUserDetailsService userDetailsService;
    @Autowired
    private JWTUtil jwtUtil;
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity<?> createAuth(@RequestBody AuthenctionRequest authenctionRequest) throws Exception {

        if (tableMapper.findUserByUserName(authenctionRequest.getUsername()) != null) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenctionRequest.getUsername(), authenctionRequest.getPassword()));
            } catch (BadCredentialsException e) {

                return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No users");

            }
            final User userDetails = userDetailsService.loadUserByUsername(authenctionRequest.getUsername());

            final String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse(jwt, userDetails.getUsername(), userDetails.getRole()));
        } else {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No users");
        }
    }

}
