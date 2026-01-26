package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.service.interfaces.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
@Slf4j
public class TouristController {
    private final TouristService service;

}
