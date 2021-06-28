package com.application_tender.tender.service;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class myUserDetailsService implements UserDetailsService {
    @Autowired
    private TableMapper tableMapper;
    @Override
    public User loadUserByUsername(String s) throws UsernameNotFoundException {
        return tableMapper.findUserByUserName(s);
    }
}
