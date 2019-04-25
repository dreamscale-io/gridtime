package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.api.status.ConnectionResultDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity;
import com.dreamscale.htmflow.core.domain.member.OrganizationRepository;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.hooks.hypercore.HypercoreConnection;
import com.dreamscale.htmflow.core.hooks.hypercore.HypercoreConnectionFactory;
import com.dreamscale.htmflow.core.hooks.hypercore.HypercoreDto;
import com.dreamscale.htmflow.core.hooks.hypercore.HypercoreKeysDto;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class HypercoreService {

    @Autowired
    HypercoreConnectionFactory hypercoreConnectionFactory;


    public HypercoreKeysDto createNewFeed() {
        HypercoreConnection connection = hypercoreConnectionFactory.connect();
        HypercoreDto core = connection.create();

        return core.getResponse();
    }

}
