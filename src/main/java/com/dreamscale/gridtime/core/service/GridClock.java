package com.dreamscale.gridtime.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GridClock {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public Long nanoTime() {
        return System.nanoTime();
    }
}
