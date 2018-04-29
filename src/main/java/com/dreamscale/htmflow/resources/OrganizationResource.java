package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.ORGANIZATION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class OrganizationResource {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<OrganizationInputDto, OrganizationEntity> organizationMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        organizationMapper = mapperFactory.createDtoEntityMapper(OrganizationInputDto.class, OrganizationEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }



    /**
     * Creates a new organization with the specified name, and Jira connection information
     * returns status of Jira connectivity, and a sharable invite link for inviting members to the Org
     * @param orgInputDto
     * @return OrganizationDto
     */
    @PostMapping
    public OrganizationDto createOrganization(@RequestBody OrganizationInputDto orgInputDto) {

        return organizationService.createOrganization(orgInputDto);
    }



    /**
     * Use an invitation token to decode the organization associated with the invite,
     * if the token is valid, the organization object will be returned, otherwise 404
     * @param inviteToken
     * @return
     */
    @GetMapping(ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH)
    public OrganizationDto decodeInvitation(@RequestParam("token") String inviteToken) {

        return organizationService.decodeInvitation(inviteToken);

    }

    /**
     * Creates a new member within an organization associated with the specified email
     * A new account will be created using the email, and a validation email sent
     * If the email already exists, a validation will be sent to the email to , will need to validate the email address one more time.
     * The invitation token will need to be passed in once again
     */
    @PostMapping("/{id}" + ResourcePaths.MEMBER_PATH)
    public MembershipDto registerMember(@PathVariable("id") String organizationId, @RequestBody MembershipInputDto membershipInputDto) {
        //TODO check invite token again, before allowing member to join

        MembershipDto membership = new MembershipDto();
        membership.setMemberId(UUID.randomUUID());
        membership.setOrgEmail(membershipInputDto.getOrgEmail());

        membership.setBindingStatus(Status.VALID);

        //TODO get external from Jira list
        ExternalMemberDto externalMemberDto = new ExternalMemberDto();
        externalMemberDto.setFullName("Janelle Klein");
        externalMemberDto.setExternalId("44");
        externalMemberDto.setOrgEmail("janelle@dreamscale.io");

        membership.setBoundExternalAccount(externalMemberDto);

        //TODO get orgDto from service

        OrganizationDto org = new OrganizationDto();
        org.setId(UUID.fromString(organizationId));
        org.setName("DreamScale");
        //org.setInviteLink(constructInvitationLink(org.getId()));

        membership.setOrganization(org);

        //TODO get master account match, for now, create a new master account

        MasterAccountDto masterAccountDto = new MasterAccountDto();
        masterAccountDto.setMasterAccountId(UUID.randomUUID());
        masterAccountDto.setFullName(externalMemberDto.getFullName());

        membership.setMasterAccountDto(masterAccountDto);

        return membership;
    }

    private List<ExternalMemberDto> findUnboundMembers(String orgId, String searchByEmail ) {

        //TODO if search is provided, then filter results to matches, otherwise, return everything

        //TODO first decode token into an organization
        OrganizationDto org = new OrganizationDto();
        org.setId(UUID.randomUUID());
        org.setName("Org name");

        //TODO then use org to lookup unbound Jira accounts

        ExternalMemberDto externalMember = new ExternalMemberDto();
        externalMember.setExternalId("123");
        externalMember.setOrgEmail("janelle@dreamscale.io");
        externalMember.setFullName("Janelle Klein");

        List<ExternalMemberDto> externalMembers = new ArrayList<>();
        externalMembers.add(externalMember);

        return externalMembers;
    }



}
