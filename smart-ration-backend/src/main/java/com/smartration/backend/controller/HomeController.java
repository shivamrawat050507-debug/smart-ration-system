package com.smartration.backend.controller;

import com.smartration.backend.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"", "/"})
    public ResponseEntity<MessageResponse> home() {
        return ResponseEntity.ok(new MessageResponse("Smart Ration Backend is running"));
    }
}
