package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.ContractType;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Hypothesis;
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

public class HypothesisContractTest {

    @Mocked
    Context ctx;

    @Mocked
    ChaincodeStub stub;

    @Tested
    HypothesisContract contract = new HypothesisContract();

    static Hypothesis hypothesis = Hypothesis.createHypothesis("Canada has ten provinces", Locale.CANADA);

    static String hypothesisJson;

    @BeforeAll
    private static void prepareTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        hypothesisJson = mapper.writeValueAsString(hypothesis);
        // mock the creation of UUIDs to always return the test-hypothesise's id
        new MockUp<Utils>() {
            @Mock
            public String createUuid(ContractType contractType) {
                return hypothesis.getHid();
            }
        };
    }

    @Nested
    class InvokeCreateHypothesisTransaction {

        /**
         * Currently not doing any verification on the chaincode side.
         * Calling applications should handle duplicate entities
         */
        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            Hypothesis created = contract.createHypothesis(ctx, hypothesis.getClaim(), hypothesis.getLocale().toString());
            assertTrue(hypothesis.equals(created));
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            Hypothesis created = contract.createHypothesis(ctx, hypothesis.getClaim(), hypothesis.getLocale().toString());
            assertTrue(hypothesis.equals(created)); // Hypothesis's 'equals' method only considers hypothesise id
            assertTrue(hypothesis.getScore().equals(created.getScore()));
            assertEquals(hypothesis.getClaim(), created.getClaim());
            assertEquals(hypothesis.getReviews(), hypothesis.getReviews());
        }
    }

    @Nested
    class InvokeQueryHypothesisTransaction {

        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid());
                result = hypothesisJson; }};

            Hypothesis found = contract.queryHypothesis(ctx, hypothesis.getHid());
            assertTrue(hypothesis.equals(found));
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () -> contract.queryHypothesis(ctx, hypothesis.getHid()));
        }
    }

    @Mocked QueryResultsIteratorWithMetadata<KeyValue> states;
    @Mocked ChaincodeShim.QueryResponseMetadata metadata;

    @Nested
    class invokeQueryAllHypothesisTransaction {

        @Test
        void whenHypothesisExist() {
            List<KeyValue> resList = Arrays.asList(
                    new TestUtils.MockKeyValue("hypothesise-1", "{ \"hid\": \"hypothesis-1\", \"claim\": \"claim-1\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-1\", \"score\": { \"reliability\": 0.85, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("hypothesise-2", "{ \"hid\": \"hypothesis-2\", \"claim\": \"claim-2\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-2\", \"score\": { \"reliability\": 0.7, \"confidence\": 0.85 }}"),
                    new TestUtils.MockKeyValue("hypothesise-3", "{ \"hid\": \"hypothesis-3\", \"claim\": \"claim-3\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-3\", \"score\": { \"reliability\": 0.9, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("hypothesise-4", "{ \"hid\": \"hypothesis-4\", \"claim\": \"claim-4\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-4\", \"score\": { \"reliability\": 0.94, \"confidence\": 0.73 }}")
            );
            runTest(resList, "blah", 4);
        }

        @Test
        void whenNoHypothesisExist() {
            List<KeyValue> resList = new ArrayList<>();
            runTest(resList, "", 0);
        }

        private void runTest(List<KeyValue> resList, String bookmark, int numHypothesis) {
            new Expectations() {
                { ctx.getStub(); result = stub; }
                { stub.getStateByRangeWithPagination("", "", 100, ""); result = states; }
                { metadata.getFetchedRecordsCount(); result = numHypothesis; }
                { metadata.getBookmark(); result = bookmark;}
                { states.getMetadata(); result = metadata; }
                { states.iterator(); result = resList.iterator(); minTimes = 0;}
            };

            PaginatedResults result = contract.queryAllHypothesis(ctx, "100", "");
            assertEquals(bookmark, result.getBookmark());
            assertEquals(numHypothesis, result.getResults().size());
        }
    }

    @Nested
    class InvokeUpdateScoreTransaction {

        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid());
                result = hypothesisJson; }};

            Hypothesis updated = contract.updateHypothesisScore(ctx, hypothesis.getHid(), "0.95", "0.97");
            assertEquals(0.95f, updated.getScore().getReliability().floatValue());
            assertEquals(0.97f, updated.getScore().getConfidence().floatValue());
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateHypothesisScore(ctx, hypothesis.getHid(), "0.95", "0.97"));
        }
    }

    @Nested
    class InvokeUpdateLocaleTransaction {

        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid());
                result = hypothesisJson; }};

            Hypothesis updated = contract.updateHypothesisLocale(ctx, hypothesis.getHid(), Locale.CANADA_FRENCH.toString());
            assertEquals(Locale.CANADA_FRENCH, updated.getLocale());
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateHypothesisScore(ctx, hypothesis.getHid(), "0.95", "0.97"));
        }
    }

    @Nested
    class InvokeAddRemoveReviewTransaction {

        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid());
                result = hypothesisJson; }};

            Hypothesis updated = contract.addOrUpdateReview(ctx, hypothesis.getHid(), "factChecker1", "0.96");
            assertEquals(1, updated.getReviews().size());
            assertEquals(0.96f, updated.getReviews().get("factChecker1").floatValue());

            updated = contract.removeReview(ctx, hypothesis.getHid(), "factChecker1");
            assertEquals(0, updated.getReviews().size());
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.addOrUpdateReview(ctx, hypothesis.getHid(), "factChecker1", "0.96"));
        }
    }

    @Nested
    class InvokeAddRemoveReferenceTransaction {

        @Test
        public void whenHypothesisExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid());
                result = hypothesisJson; }};

            Hypothesis updated = contract.addReference(ctx, hypothesis.getHid(), "ref-id1");
            assertTrue(updated.getReferences().containsKey("ref-id1"));
            updated = contract.removeReference(ctx, hypothesis.getHid(), "ref-id1");
            assertFalse(updated.getReferences().containsKey("ref-id1"));
        }

        @Test
        public void whenHypothesisDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(hypothesis.getHid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.addReference(ctx, hypothesis.getHid(), "ref-id1"));
        }
    }

}
