package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.TagsInputDto
import com.dreamscale.gridtime.api.dictionary.BookDto
import com.dreamscale.gridtime.api.dictionary.BookReferenceDto
import com.dreamscale.gridtime.api.dictionary.TagDefinitionDto
import com.dreamscale.gridtime.api.dictionary.TagDefinitionInputDto
import com.dreamscale.gridtime.api.dictionary.TagDefinitionWithDetailsDto
import com.dreamscale.gridtime.client.DictionaryClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class DictionaryResourceSpec extends Specification {

    @Autowired
    DictionaryClient dictionaryClient

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    TimeService mockTimeService
    OrganizationEntity org

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        org = aRandom.organizationEntity().save()
    }


    def 'should create a new dictionary word'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        when:
        TagDefinitionDto newDefinition = dictionaryClient.createOrRefactorDefinition("test", new TagDefinitionInputDto("test", "def"))

        then:
        assert newDefinition != null
        assert newDefinition.tagName == "test"
        assert newDefinition.definition == "def"

    }

    def 'should refactor an existing dictionary word and create a tombstone'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        TagDefinitionDto initialDef = dictionaryClient.createOrRefactorDefinition("test", new TagDefinitionInputDto("test", "def"))

        when:

        TagDefinitionDto newDefinition = dictionaryClient.createOrRefactorDefinition("test", new TagDefinitionInputDto("test2", "def2"))

        TagDefinitionWithDetailsDto newDefinitionDetails = dictionaryClient.getDefinition("test2")

        then:
        assert newDefinition != null
        assert newDefinition.tagName == "test2"
        assert newDefinition.definition == "def2"

        assert newDefinitionDetails != null
        assert newDefinitionDetails.tagName == "test2"
        assert newDefinitionDetails.definition == "def2"

        assert newDefinitionDetails.tombstones.size() == 1
        assert newDefinitionDetails.tombstones.get(0).deadTagName == "test"
        assert newDefinitionDetails.tombstones.get(0).deadDefinition == "def"

    }

    def 'should revive an existing tombstone attached to a new term'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        //refactor definition

        TagDefinitionDto initialDef = dictionaryClient.createOrRefactorDefinition("old", new TagDefinitionInputDto("old", "olddef"))
        TagDefinitionDto newDefinition = dictionaryClient.createOrRefactorDefinition("old", new TagDefinitionInputDto("new", "newdef"))

        when:

        TagDefinitionDto reviveDef = dictionaryClient.createOrRefactorDefinition("old", new TagDefinitionInputDto("old", "revive"))


        TagDefinitionWithDetailsDto revivedDef = dictionaryClient.getDefinition("old")
        TagDefinitionWithDetailsDto renamedDef = dictionaryClient.getDefinition("new")


        then:
        assert revivedDef != null
        assert renamedDef != null

        assert revivedDef != null
        assert revivedDef.tagName == "old"
        assert revivedDef.definition == "revive"

        assert renamedDef != null
        assert renamedDef.tagName == "new"
        assert renamedDef.definition == "newdef"


        assert renamedDef.tombstones.size() == 1
        assert renamedDef.tombstones.get(0).deadTagName == "old"
        assert renamedDef.tombstones.get(0).deadDefinition == "olddef"
        assert renamedDef.tombstones.get(0).reviveDate != null

    }

    def 'should create new blank tags when tagging objects and tags dont exist'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto("tag1", "tag2"))

        List<TagDefinitionDto> undefinedTerms = dictionaryClient.getUndefinedTeamDictionaryTerms();

        then:
        assert undefinedTerms != null
        assert undefinedTerms.size() == 2
    }

    def 'should create new blank tags only when tags dont exist'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:

        LearningCircuitDto circuit = circuitClient.startWTF()

        dictionaryClient.createOrRefactorDefinition("tag1", new TagDefinitionInputDto("tag1", "def"))

        circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto("tag1", "tag2"))

        List<TagDefinitionDto> undefinedTerms = dictionaryClient.getUndefinedTeamDictionaryTerms();

        then:
        assert undefinedTerms != null
        assert undefinedTerms.size() == 1
        assert undefinedTerms.get(0).tagName == "tag2"
    }

    def 'should pull words into a book'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        when:
        dictionaryClient.createOrRefactorDefinition("tag1", new TagDefinitionInputDto("tag1", "def"))
        dictionaryClient.createOrRefactorDefinition("tag2", new TagDefinitionInputDto("tag2", "def2"))


        BookReferenceDto bookRef = dictionaryClient.createTeamBook("mybook")

        TagDefinitionDto tag1 = dictionaryClient.pullDefinitionIntoTeamBook("mybook", "tag1")

        BookDto bookDto = dictionaryClient.getTeamBook("mybook")

        then:
        assert bookRef != null
        assert tag1 != null
        assert bookDto != null
        assert bookDto.getDefinitions().size() == 1
    }


    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }

}
