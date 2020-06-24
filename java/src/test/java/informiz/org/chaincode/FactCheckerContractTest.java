package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.ContractType;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.FactChecker;
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

public class FactCheckerContractTest {

    @Mocked
    Context ctx;

    @Mocked
    ChaincodeStub stub;

    @Tested
    FactCheckerContract contract = new FactCheckerContract();

    static FactChecker factChecker = FactChecker.createFactChecker("Chuck Fact", 0.98f, 0.99f);

    static String factCheckerJson;

    @BeforeAll
    private static void prepareTest() throws JsonProcessingException {
        factChecker.setEmail("chuck@informiz.org");
        factChecker.setLink("https://www.linkedin.com/in/chuckfact");
        ObjectMapper mapper = new ObjectMapper();
        factCheckerJson = mapper.writeValueAsString(factChecker);
        // mock the creation of UUIDs to always return the test-fact-checker's id
        new MockUp<Utils>() {
            @Mock
            public String createUuid(ContractType contractType) {
                return factChecker.getFcid();
            }
        };
    }

    @Nested
    class InvokeCreateFactCheckerTransaction {

        /**
         * Currently not doing any verification on the chaincode side.
         * Calling applications should handle duplicate entities
         */
        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            FactChecker created = contract.createFactChecker(ctx, factChecker.getName(),
                    String.valueOf(factChecker.getScore().getReliability()),
                    String.valueOf(factChecker.getScore().getConfidence()),
                    factChecker.getEmail(),
                    factChecker.getLink());
            assertTrue(factChecker.equals(created));
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } };
            FactChecker created = contract.createFactChecker(ctx, factChecker.getName(),
                    String.valueOf(factChecker.getScore().getReliability()),
                    String.valueOf(factChecker.getScore().getConfidence()),
                    factChecker.getEmail(),
                    factChecker.getLink());
            assertTrue(factChecker.equals(created)); // FactChecker's 'equals' method only considers fact-checker id
            assertTrue(factChecker.getScore().equals(created.getScore()));
            assertEquals(factChecker.getName(), created.getName());
        }
    }

    @Nested
    class InvokeQueryFactCheckerTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker found = contract.queryFactChecker(ctx, factChecker.getFcid());
            assertTrue(factChecker.equals(found));
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () -> contract.queryFactChecker(ctx, factChecker.getFcid()));
        }
    }

    @Mocked QueryResultsIteratorWithMetadata<KeyValue> states;
    @Mocked ChaincodeShim.QueryResponseMetadata metadata;

    @Nested
    class invokeQueryAllFactCheckersTransaction {

        @Test
        void whenFactCheckersExist() {
            List<KeyValue> resList = Arrays.asList(
                    new TestUtils.MockKeyValue("fact-checker-1", "{ \"sid\": \"fact-checker-1\", \"name\": \"fact-checker no.1\", \"score\": { \"reliability\": 0.85, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("fact-checker-2", "{ \"sid\": \"fact-checker-2\", \"name\": \"fact-checker no.2\", \"score\": { \"reliability\": 0.7, \"confidence\": 0.85 }}"),
                    new TestUtils.MockKeyValue("fact-checker-3", "{ \"sid\": \"fact-checker-3\", \"name\": \"fact-checker no.3\", \"score\": { \"reliability\": 0.9, \"confidence\": 0.7 }}"),
                    new TestUtils.MockKeyValue("fact-checker-4", "{ \"sid\": \"fact-checker-4\", \"name\": \"fact-checker no.4\", \"score\": { \"reliability\": 0.94, \"confidence\": 0.73 }}")
            );
            runTest(resList, "blah", 4);
        }

        @Test
        void whenNoFactCheckersExist() {
            List<KeyValue> resList = new ArrayList<>();
            runTest(resList, "", 0);
        }

        private void runTest(List<KeyValue> resList, String bookmark, int numFactCheckers) {
            new Expectations() {
                { ctx.getStub(); result = stub; }
                { stub.getStateByRangeWithPagination("", "", 100, ""); result = states; }
                { metadata.getFetchedRecordsCount(); result = numFactCheckers; }
                { metadata.getBookmark(); result = bookmark;}
                { states.getMetadata(); result = metadata; }
                { states.iterator(); result = resList.iterator(); minTimes=0;}
            };

            PaginatedResults result = contract.queryAllFactCheckers(ctx, "100", "");
            assertEquals(bookmark, result.getBookmark());
            assertEquals(numFactCheckers, result.getResults().size());
        }
    }

    @Nested
    class InvokeUpdateNameTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker updated = contract.updateFactCheckerName(ctx, factChecker.getFcid(), "Chuck Fact Jr.");
            assertEquals("Chuck Fact Jr.", updated.getName());
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateFactCheckerName(ctx, factChecker.getFcid(), "Chuck Fact Jr."));
        }
    }

    @Nested
    class InvokeUpdateScoreTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker updated = contract.updateFactCheckerScore(ctx, factChecker.getFcid(), "0.95", "0.97");
            assertEquals(0.95f, updated.getScore().getReliability().floatValue());
            assertEquals(0.97f, updated.getScore().getConfidence().floatValue());
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateFactCheckerScore(ctx, factChecker.getFcid(), "0.95", "0.97"));
        }
    }

    @Nested
    class InvokeUpdateEmailTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker updated = contract.updateFactCheckerEmail(ctx, factChecker.getFcid(), "chuck.fact@informiz.org");
            assertEquals("chuck.fact@informiz.org", updated.getEmail());
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateFactCheckerEmail(ctx, factChecker.getFcid(), "chuck.fact@informiz.org"));
        }
    }

    @Nested
    class InvokeUpdateLinkTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker updated = contract.updateFactCheckerLink(ctx, factChecker.getFcid(), "https://www.facebook.com/chuckfact");
            assertEquals("https://www.facebook.com/chuckfact", updated.getLink());
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.updateFactCheckerLink(ctx, factChecker.getFcid(), "https://www.facebook.com/chuckfact"));
        }
    }

    @Nested
    class InvokeDeleteFactCheckerTransaction {

        @Test
        public void whenFactCheckerExists() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid());
                result = factCheckerJson; }};

            FactChecker updated = contract.deleteFactChecker(ctx, factChecker.getFcid());
            assertEquals(false, updated.getActive());
        }

        @Test
        public void whenFactCheckerDoesNotExist() {
            new Expectations() { { ctx.getStub(); result = stub; } { stub.getStringState(factChecker.getFcid()); result = ""; } };
            Assertions.assertThrows(ChaincodeException.class, () ->
                    contract.deleteFactChecker(ctx, factChecker.getFcid()));
        }
    }

}
