package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.account.ConnectionStatusDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.security.MasterAccountIdResolver;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TimeService {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
