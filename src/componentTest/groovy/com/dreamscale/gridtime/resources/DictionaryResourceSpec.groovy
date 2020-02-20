package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.TagsInputDto
import com.dreamscale.gridtime.api.dictionary.BookDto
import com.dreamscale.gridtime.api.dictionary.BookReferenceDto
import com.dreamscale.gridtime.api.dictionary.RefactorBookInputDto
import com.dreamscale.gridtime.api.dictionary.WordDefinitionDto
import com.dreamscale.gridtime.api.dictionary.WordDefinitionInputDto
import com.dreamscale.gridtime.api.dictionary.WordDefinitionWithDetailsDto
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
        WordDefinitionDto newDefinition = dictionaryClient.createOrRefactorWord("test", new WordDefinitionInputDto("test", "def"))

        then:
        assert newDefinition != null
        assert newDefinition.wordName == "test"
        assert newDefinition.definition == "def"

    }

    def 'should refactor an existing dictionary word and create a tombstone'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        WordDefinitionDto initialDef = dictionaryClient.createOrRefactorWord("test", new WordDefinitionInputDto("test", "def"))

        when:

        WordDefinitionDto newWord = dictionaryClient.createOrRefactorWord("test", new WordDefinitionInputDto("test2", "def2"))

        WordDefinitionWithDetailsDto newWordetails = dictionaryClient.getWord("test2")

        then:
        assert newWord != null
        assert newWord.wordName == "test2"
        assert newWord.definition == "def2"

        assert newWordetails != null
        assert newWordetails.wordName == "test2"
        assert newWordetails.definition == "def2"

        assert newWordetails.tombstones.size() == 1
        assert newWordetails.tombstones.get(0).deadWordName == "test"
        assert newWordetails.tombstones.get(0).deadDefinition == "def"

    }

    def 'should revive an existing tombstone attached to a new term'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        //refactor definition

        WordDefinitionDto initialWord = dictionaryClient.createOrRefactorWord("old", new WordDefinitionInputDto("old", "olddef"))
        WordDefinitionDto renamedWord = dictionaryClient.createOrRefactorWord("old", new WordDefinitionInputDto("new", "newdef"))

        when:

        WordDefinitionDto revivedDeadWord = dictionaryClient.createOrRefactorWord("old", new WordDefinitionInputDto("old", "revive"))


        WordDefinitionWithDetailsDto revivedWithDetails = dictionaryClient.getWord("old")
        WordDefinitionWithDetailsDto renamedWithDetails = dictionaryClient.getWord("new")


        then:
        assert revivedWithDetails != null
        assert renamedWithDetails != null

        assert revivedWithDetails != null
        assert revivedWithDetails.wordName == "old"
        assert revivedWithDetails.definition == "revive"

        assert renamedWithDetails != null
        assert renamedWithDetails.wordName == "new"
        assert renamedWithDetails.definition == "newdef"


        assert renamedWithDetails.tombstones.size() == 1
        assert renamedWithDetails.tombstones.get(0).deadWordName == "old"
        assert renamedWithDetails.tombstones.get(0).deadDefinition == "olddef"
        assert renamedWithDetails.tombstones.get(0).reviveDate != null

    }

    def 'should create new blank definition words when tagging objects and tags dont exist'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto("tag1", "tag2"))

        List<WordDefinitionDto> undefinedWords = dictionaryClient.getUndefinedTeamWords();

        then:
        assert undefinedWords != null
        assert undefinedWords.size() == 2
    }

    def 'should create new blank definition words only when tags dont exist'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:

        LearningCircuitDto circuit = circuitClient.startWTF()

        dictionaryClient.createOrRefactorWord("tag1", new WordDefinitionInputDto("tag1", "def"))

        circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto("tag1", "tag2"))

        List<WordDefinitionDto> undefinedWords = dictionaryClient.getUndefinedTeamWords();

        then:
        assert undefinedWords != null
        assert undefinedWords.size() == 1
        assert undefinedWords.get(0).wordName == "tag2"
    }

    def 'should pull words into a book'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        when:
        dictionaryClient.createOrRefactorWord("tag1", new WordDefinitionInputDto("tag1", "def"))
        dictionaryClient.createOrRefactorWord("tag2", new WordDefinitionInputDto("tag2", "def2"))


        BookReferenceDto bookRef = dictionaryClient.createTeamBook("mybook")

        WordDefinitionDto wordInBook = dictionaryClient.pullWordIntoTeamBook("mybook", "tag1")

        BookDto bookDto = dictionaryClient.getTeamBook("mybook")

        then:
        assert bookRef != null
        assert wordInBook != null
        assert bookDto != null
        assert bookDto.getDefinitions().size() == 1
    }

    def 'should refactor words inside a book without affecting global word scope'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        dictionaryClient.createOrRefactorWord("tag1", new WordDefinitionInputDto("tag1", "def"))
        BookReferenceDto bookRef = dictionaryClient.createTeamBook("mybook")

        WordDefinitionDto wordInBook = dictionaryClient.pullWordIntoTeamBook("mybook", "tag1")

        when:
        dictionaryClient.refactorWordInsideTeamBook("mybook", "tag1", new WordDefinitionInputDto("tag1", "newdef"))

        WordDefinitionWithDetailsDto globalDefinition = dictionaryClient.getWord("tag1")

        BookDto bookDto = dictionaryClient.getTeamBook("mybook")

        then:
        assert bookRef != null
        assert wordInBook != null
        assert globalDefinition != null
        assert bookDto != null
        assert bookDto.getDefinitions().size() == 1

        assert globalDefinition.definition == "def"
        assert bookDto.getDefinitions().get(0).definition == "newdef"

    }

    def 'should get a list of available team books'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:

        BookReferenceDto book1Ref = dictionaryClient.createTeamBook("book1")
        BookReferenceDto book2Ref = dictionaryClient.createTeamBook("book2")

        List<BookReferenceDto> bookRefs = dictionaryClient.getTeamBooks()

        then:
        assert bookRefs != null
        assert bookRefs.size() == 2
    }

    def 'should get the detailed tombstone history for the refactorings of a book word'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        dictionaryClient.createOrRefactorWord("tag", new WordDefinitionInputDto("tag", "def"))

        BookReferenceDto bookRef = dictionaryClient.createTeamBook("mybook")
        WordDefinitionDto wordInBook = dictionaryClient.pullWordIntoTeamBook("mybook", "tag")

        when:

        dictionaryClient.refactorWordInsideTeamBook("mybook", "tag", new WordDefinitionInputDto("tag1", "newdef1"))
        dictionaryClient.refactorWordInsideTeamBook("mybook", "tag1", new WordDefinitionInputDto("tag2", "newdef2"))
        dictionaryClient.refactorWordInsideTeamBook("mybook", "tag2", new WordDefinitionInputDto("tag3", "newdef3"))


        WordDefinitionWithDetailsDto wordWithDetails = dictionaryClient.getTeamBookWord("mybook", "tag3")

        then:
        assert wordWithDetails != null
        assert wordWithDetails.wordName == "tag3"
        assert wordWithDetails.definition == "newdef3"
        assert wordWithDetails.tombstones.size() == 3
    }

    def 'should rename an existing book'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        BookReferenceDto bookRef = dictionaryClient.createTeamBook("book1")

        when:

        dictionaryClient.updateTeamBook("book1", new RefactorBookInputDto("renamedBook"))

        List<BookReferenceDto> bookRefs = dictionaryClient.getTeamBooks()

        BookDto renamedBook = dictionaryClient.getTeamBook("renamedBook")

        then:
        assert bookRefs != null
        assert bookRefs.size() == 1
        assert bookRefs.get(0).bookName == "renamedBook"

        assert renamedBook != null

    }

    def 'should archive an existing book on delete'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        BookReferenceDto book1Ref = dictionaryClient.createTeamBook("book1")
        BookReferenceDto book2Ref = dictionaryClient.createTeamBook("book2")

        when:

        dictionaryClient.archiveTeamBook("book1")

        List<BookReferenceDto> bookRefs = dictionaryClient.getTeamBooks()

        BookDto archivedBook = dictionaryClient.getTeamBook("book1")

        then:
        assert bookRefs != null
        assert bookRefs.size() == 1
        assert bookRefs.get(0).bookName == "book2"

        assert archivedBook != null
        assert archivedBook.getBookStatus() == "ARCHIVED"

    }

    //TODO forget about community dictionary for now, lets just get the team one working
    //implement actual deletion of words in books
    //actual deletion of books
    //words archive?

    //TODO next is promoting words into community dictionary


    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }

}
