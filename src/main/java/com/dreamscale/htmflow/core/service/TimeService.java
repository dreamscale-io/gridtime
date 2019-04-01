package com.dreamscale.htmflow.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TimeService {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
