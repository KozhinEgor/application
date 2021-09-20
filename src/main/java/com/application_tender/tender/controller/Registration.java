package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.User;
import com.application_tender.tender.service.MailSender;
import com.application_tender.tender.subsidiaryModels.setPassword;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.TabableView;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class Registration {
    @Autowired
    private MailSender mailSender;
    @Autowired
    private TableMapper tableMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${site.url}")
    private String url;
    @PostMapping("/registration")
    @ResponseBody
    ResponseEntity registration(@RequestBody User user) {
//        System.out.println(user);
        if(tableMapper.findUserByUserName(user.getUsername()) != null){
            return ResponseEntity.status(500).build();
        }
        else {
            tableMapper.insertUser(user.getUsername(),user.getRole().toString(), UUID.randomUUID().toString());
            User u = tableMapper.findUserByUserName(user.getUsername());
            if(u != null){
                String message = String.format(
                        "Здравствуйте, %s! \n" +
                                "Вам доступна регистрация в приложении  \"Application Tender\", на роль%s. Перейдите по ссылке: "+url+", \n" +
                                "и введите данный код %s",
                        u.getUsername(),u.getRole().toString(),u.getActivationCode()
                );
                mailSender.send(user.getUsername(), "Письмо для регистрации в приложении \"Application Tender\"",message);
                return ResponseEntity.ok().build();
            }
            else{
                return ResponseEntity.status(500).build();
            }
        }

    }
    @PostMapping("/setPassword")
    @ResponseBody
    ResponseEntity setPassword(@RequestBody setPassword setPassword) {

        User u = tableMapper.findUserByUserName(setPassword.getUsername());
        if(u != null){
            if(setPassword.getActivationCode().equals(u.getActivationCode())){
                tableMapper.updateNickName(setPassword.getNickname(),u.getId());
                tableMapper.updatePassword(passwordEncoder.encode(setPassword.getPassword()),u.getId());
                return ResponseEntity.ok().build();
            }
            else{
                ResponseEntity.status(500).build();
            }

        }

        return ResponseEntity.status(500).build();
    }
    @PostMapping("/checkNickname")
    @ResponseBody
    Map<String,Boolean> checkNickname(@RequestBody String nickname) {
        Map<String, Boolean> a = new HashMap<>();
        if(tableMapper.findUserByNickname(nickname) == null){
            a.put("name",true);
          return  a;
        }
        else{
            a.put("name",false);
            return a;
        }
    }

}
