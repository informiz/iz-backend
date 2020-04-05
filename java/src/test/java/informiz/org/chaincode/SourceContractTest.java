package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.ContractType;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Source;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourceContractTest {

    @Mocked
    Context ctx;

    @Mocked
    ChaincodeStub stub;

    @Tested
    SourceContract contract = new SourceContract();

    static Source src = Source.createSource("www.nasa.com", 0.98f, 0.99f);

    static String srcJson;

    @BeforeAll
    private static void prepareTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        srcJson = mapper.writeValueAsString(src);
        // mock the creation of UUIDs to always return the test-source's id
        new MockUp<Utils>() {
            @Mock
            public String createUuid(ContractType contractType) {
                return src.getSid();
            }
        };
    }

    @Nested
    class InvokeCreateSourceTransaction {

        /**
         * Currently not doing any verification on the chaincode side.
         * Calling applications should handle duplicate entities
         */
        @Test
        public void whenSourceExists() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            Source created = contract.createSource(ctx, src.getName(), src.getScore().getReliability(), src.getScore().getConfidence());
            assertTrue(src.equals(created));
        }

        @Test
        public void whenSourceDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            Source created = contract.createSource(ctx, src.getName(), src.getScore().getReliability(), src.getScore().getConfidence());
            assertTrue(src.equals(created)); // Source's 'equals' method only considers source id
            assertTrue(src.getScore().equals(created.getScore()));
            assertEquals(src.getName(), created.getName());
            assertEquals(src.getReviews(), src.getReviews());
        }
    }

    @Nested
    class InvokeQuerySourceTransaction {

        @Test
        public void whenSourceExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid());
                result = srcJson; }};

            Source found = contract.querySource(ctx, src.getSid());
            assertTrue(src.equals(found));
        }

        @Test
        public void whenSourceDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () -> contract.querySource(ctx, src.getSid()));
        }
    }

    @Mocked QueryResultsIteratorWithMetadata<KeyValue> states;
    @Mocked ChaincodeShim.QueryResponseMetadata metadata;

    @Nested
    class invokeQueryAllSourcesTransaction {

        @Test
        void whenSourcesExist() {
            List<KeyValue> resList = Arrays.asList(
                    new TestUtils.MockKeyValue("source-1", "{ \"sid\": \"source-1\", \"name\": \"source no.1\", \"score\": { \"reliability\": 0.85, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("source-2", "{ \"sid\": \"source-2\", \"name\": \"source no.2\", \"score\": { \"reliability\": 0.7, \"confidence\": 0.85 }}"),
                    new TestUtils.MockKeyValue("source-3", "{ \"sid\": \"source-3\", \"name\": \"source no.3\", \"score\": { \"reliability\": 0.9, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("source-4", "{ \"sid\": \"source-4\", \"name\": \"source no.4\", \"score\": { \"reliability\": 0.94, \"confidence\": 0.73 }}")
            );
            runTest(resList, "blah", 4);
        }

        @Test
        void whenNoSourcesExist() {
            List<KeyValue> resList = new ArrayList<>();
            runTest(resList, "", 0);
        }

        private void runTest(List<KeyValue> resList, String bookmark, int numSources) {
            new Expectations() {
                { ctx.getStub(); result = stub; }
                { stub.getStateByRangeWithPagination("", "", 100, ""); result = states; }
                { metadata.getFetchedRecordsCount(); result = numSources; }
                { metadata.getBookmark(); result = bookmark;}
                { states.getMetadata(); result = metadata; }
                { states.iterator(); result = resList.iterator(); }
            };

            PaginatedResults result = contract.queryAllSources(ctx, 100, "");
            assertEquals(bookmark, result.getBookmark());
            assertEquals(numSources, result.getResults().size());
        }
    }

    @Nested
    class InvokeUpdateNameTransaction {

        @Test
        public void whenSourceExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid());
                result = srcJson; }};

            Source updated = contract.updateSourceName(ctx, src.getSid(), "www.nasa.gov");
            assertEquals("www.nasa.gov", updated.getName());
        }

        @Test
        public void whenSourceDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateSourceName(ctx, src.getSid(), "www.nasa.gov"));
        }
    }

    @Nested
    class InvokeUpdateScoreTransaction {

        @Test
        public void whenSourceExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid());
                result = srcJson; }};

            Source updated = contract.updateSourceScore(ctx, src.getSid(), 0.95f, 0.97f);
            assertEquals(0.95f, updated.getScore().getReliability().floatValue());
            assertEquals(0.97f, updated.getScore().getConfidence().floatValue());
        }

        @Test
        public void whenSourceDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateSourceScore(ctx, src.getSid(), 0.95f, 0.97f));
        }
    }

    @Nested
    class InvokeAddRemoveReviewTransaction {

        @Test
        public void whenSourceExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid());
                result = srcJson; }};

            Source updated = contract.addReview(ctx, src.getSid(), "factChecker1", 0.96f);
            assertEquals(1, updated.getReviews().size());
            assertEquals(0.96f, updated.getReviews().get("factChecker1").floatValue());

            updated = contract.removeReview(ctx, src.getSid(), "factChecker1");
            assertEquals(0, updated.getReviews().size());
        }

        @Test
        public void whenSourceDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(src.getSid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.addReview(ctx, src.getSid(), "factChecker1", 0.96f));
        }
    }
}
