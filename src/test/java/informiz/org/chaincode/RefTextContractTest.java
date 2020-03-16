package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.ContractType;
import informiz.org.chaincode.model.ReferenceText;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Utils;
import mockit.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class RefTextContractTest {

    @Mocked
    Context ctx;

    @Mocked
    ChaincodeStub stub;

    @Tested
    ReferenceTextContract contract = new ReferenceTextContract();

    static ReferenceText text = ReferenceText.createRefText(
            "Today, Canada includes ten provinces and three territories",
            "https://www.canada.ca/en/intergovernmental-affairs/services/provinces-territories.html",
            "source-1", Locale.CANADA);

    static String textJson;

    @BeforeAll
    private static void prepareTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        textJson = mapper.writeValueAsString(text);
        // mock the creation of UUIDs to always return the test-texte's id
        new MockUp<Utils>() {
            @Mock
            public String createUuid(ContractType contractType) {
                return text.getTid();
            }
        };
    }

    @Nested
    class InvokeCreateReferenceTextTransaction {

        /**
         * Currently not doing any verification on the chaincode side.
         * Calling applications should handle duplicate entities
         */
        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            ReferenceText created = contract.createReferenceText(ctx, text.getText(), text.getSid(),
                    text.getLink(), text.getLocale());
            assertTrue(text.equals(created));
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            ReferenceText created = contract.createReferenceText(ctx, text.getText(), text.getSid(),
                    text.getLink(), text.getLocale());
            assertTrue(text.equals(created)); // ReferenceText's 'equals' method only considers text id
            assertEquals(text.getText(), created.getText());
            assertEquals(text.getSid(), created.getSid());
            assertEquals(text.getLink(), created.getLink());
            assertTrue(text.getScore().equals(created.getScore()));
            assertEquals(text.getLocale(), created.getLocale());
            assertEquals(text.getReviews(), text.getReviews());
        }
    }

    @Nested
    class InvokeQueryReferenceTextTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText found = contract.queryReferenceText(ctx, text.getTid());
            assertTrue(text.equals(found));
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () -> contract.queryReferenceText(ctx, text.getTid()));
        }
    }

    @Mocked QueryResultsIteratorWithMetadata<KeyValue> states;
    @Mocked ChaincodeShim.QueryResponseMetadata metadata;

    @Nested
    class invokeQueryAllReferenceTextTransaction {

        @Test
        void whenReferenceTextExist() {
            List<KeyValue> resList = Arrays.asList(
                    new TestUtils.MockKeyValue("texte-1", "{ \"tid\": \"text-1\", \"claim\": \"claim-1\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-1\", \"link\": \"link-1\", \"score\": { \"reliability\": 0.85, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("texte-2", "{ \"tid\": \"text-2\", \"claim\": \"claim-2\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-2\", \"link\": \"link-2\", \"score\": { \"reliability\": 0.7, \"confidence\": 0.85 }}"),
                    new TestUtils.MockKeyValue("texte-3", "{ \"tid\": \"text-3\", \"claim\": \"claim-3\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-3\", \"link\": \"link-2\", \"score\": { \"reliability\": 0.9, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("texte-4", "{ \"tid\": \"text-4\", \"claim\": \"claim-4\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-4\", \"link\": \"link-2\", \"score\": { \"reliability\": 0.94, \"confidence\": 0.73 }}")
            );
            runTest(resList, "blah", 4);
        }

        @Test
        void whenNoReferenceTextExist() {
            List<KeyValue> resList = new ArrayList<>();
            runTest(resList, "", 0);
        }

        private void runTest(List<KeyValue> resList, String bookmark, int numReferenceText) {
            new Expectations() {
                { ctx.getStub(); result = stub; }
                { stub.getStateByRangeWithPagination("", "", 100, ""); result = states; }
                { metadata.getFetchedRecordsCount(); result = numReferenceText; }
                { metadata.getBookmark(); result = bookmark;}
                { states.getMetadata(); result = metadata; }
                { states.iterator(); result = resList.iterator(); }
            };

            PaginatedResults result = contract.queryAllReferenceTexts(ctx, 100, "");
            assertEquals(bookmark, result.getBookmark());
            assertEquals(numReferenceText, result.getResults().size());
        }
    }

    @Nested
    class InvokeUpdateScoreTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText updated = contract.updateReferenceTextScore(ctx, text.getTid(), 0.95f, 0.97f);
            assertEquals(0.95f, updated.getScore().getReliability().floatValue());
            assertEquals(0.97f, updated.getScore().getConfidence().floatValue());
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateReferenceTextScore(ctx, text.getTid(), 0.95f, 0.97f));
        }
    }

    @Nested
    class InvokeUpdateSourceTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText updated = contract.updateReferenceTextSource(ctx, text.getTid(), "another-src-id");
            assertEquals("another-src-id", updated.getSid());
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateReferenceTextSource(ctx, text.getTid(), "another-src-id"));
        }
    }

    @Nested
    class InvokeUpdateLinkTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText updated = contract.updateReferenceTextLink(ctx, text.getTid(), "www.server.com");
            assertEquals("www.server.com", updated.getLink());
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateReferenceTextLink(ctx, text.getTid(), "www.server.com"));
        }
    }

    @Nested
    class InvokeUpdateLocaleTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText updated = contract.updateReferenceTextLocale(ctx, text.getTid(), Locale.CANADA_FRENCH);
            assertEquals(Locale.CANADA_FRENCH, updated.getLocale());
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateReferenceTextScore(ctx, text.getTid(), 0.95f, 0.97f));
        }
    }

    @Nested
    class InvokeAddRemoveReviewTransaction {

        @Test
        public void whenReferenceTextExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid());
                result = textJson; }};

            ReferenceText updated = contract.addOrUpdateReview(ctx, text.getTid(), "factChecker1", 0.96f);
            assertEquals(1, updated.getReviews().size());
            assertEquals(0.96f, updated.getReviews().get("factChecker1").floatValue());

            updated = contract.removeReview(ctx, text.getTid(), "factChecker1");
            assertEquals(0, updated.getReviews().size());
        }

        @Test
        public void whenReferenceTextDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(text.getTid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.addOrUpdateReview(ctx, text.getTid(), "factChecker1", 0.96f));
        }
    }
}
