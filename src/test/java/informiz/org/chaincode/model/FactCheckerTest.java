package informiz.org.chaincode.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/*
 * SPDX-License-Identifier: Apache-2.0
 */
public final class FactCheckerTest {

    @Nested
    class Equality {

        @Tested
        FactChecker factChecker = FactChecker.createFactChecker("Jane Doe", null);

        private void mockCreateId() {
            new MockUp<Utils>() {
                @Mock
                public String createUuid(ContractType contractType) {
                    return factChecker.getFcid();
                }
            };
        }


        @Test
        public void isReflexive() {
            assertTrue(factChecker.equals(factChecker));
        }

        @Test
        public void isSymmetric() {
            mockCreateId();
            FactChecker otherFactChecker = FactChecker.createFactChecker("Jane Doe", null);

            assertTrue(factChecker.equals(otherFactChecker));
            assertTrue(otherFactChecker.equals(factChecker));
        }

        @Test
        public void isTransitive() {
            mockCreateId();
            FactChecker otherFactChecker = FactChecker.createFactChecker("Jane Doe", null);
            FactChecker anotherFactChecker = FactChecker.createFactChecker("Jane Doe", null);

            assertTrue(factChecker.equals(otherFactChecker));
            assertTrue(otherFactChecker.equals(anotherFactChecker));
            assertTrue(factChecker.equals(anotherFactChecker));
        }

        @Test
        public void handlesInequality() {
            FactChecker factCheckerB = FactChecker.createFactChecker("John Doe", null);

            assertFalse(factChecker.equals(factCheckerB));
        }

        @Test
        public void handlesOtherObjects() {
            String factCheckerB = "not a fact-checker";

            assertFalse(factChecker.equals(factCheckerB));
        }

        @Test
        public void handlesNull() {
            assertFalse(factChecker.equals(null));
        }

        @Test
        public void ifSameIdThenEqual() {
            mockCreateId();
            FactChecker factCheckerB =
                    FactChecker.createFactChecker("John Doe", new Score(0.9f, 0.8f));

            assertTrue(factChecker.equals(factCheckerB));
        }

        @Test
        public void ifDifferentIdThenNotEqual() {
            FactChecker factCheckerB =
                    FactChecker.createFactChecker(factChecker.getName(), factChecker.getScore());

            assertFalse(factChecker.equals(factCheckerB));
        }
    }

    @Tested
    FactChecker factChecker = FactChecker.createFactChecker("Jane Doe", null);

    @Test
    public void toStringFormat() {
        assertEquals("{ \"name\": \"Jane Doe\", \"score\": { \"reliability\": 0.50, \"confidence\": 0.00 } }", factChecker.toString());
    }

    /**
     * Test the ser/de configuration of the class, e.g handle private setters
     * @throws IOException if ser/de fails
     */
    @Test
    public void jsonConversion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // TODO: add/remove reviews
        String asJson = mapper.writeValueAsString(factChecker);
        FactChecker restored = mapper.readValue(asJson, FactChecker.class);

        assertEquals(factChecker.getFcid(), restored.getFcid());
        assertEquals(factChecker.getScore(), restored.getScore());
        assertEquals(factChecker.getName(), restored.getName());
    }


}
