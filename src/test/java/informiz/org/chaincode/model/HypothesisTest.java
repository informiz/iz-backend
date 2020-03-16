package informiz.org.chaincode.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/*
 * SPDX-License-Identifier: Apache-2.0
 */
public final class HypothesisTest {

    @Nested
    class Equality {

        @Tested
        Hypothesis hypothesis = Hypothesis.createHypothesis("Some hypothesis...", Locale.GERMANY);

        private void mockCreateId() {
            new MockUp<Utils>() {
                @Mock
                public String createUuid(ContractType contractType) {
                    return hypothesis.getHid();
                }
            };
        }


        @Test
        public void isReflexive() {
            assertTrue(hypothesis.equals(hypothesis));
        }

        @Test
        public void isSymmetric() {
            mockCreateId();
            Hypothesis otherText = Hypothesis.createHypothesis("Some hypothesis...", Locale.GERMANY);

            assertTrue(hypothesis.equals(otherText));
            assertTrue(otherText.equals(hypothesis));
        }

        @Test
        public void isTransitive() {
            mockCreateId();
            Hypothesis otherText = Hypothesis.createHypothesis("Some hypothesis...",  Locale.GERMANY);
            Hypothesis anotherText = Hypothesis.createHypothesis("Some hypothesis...",  Locale.GERMANY);

            assertTrue(hypothesis.equals(otherText));
            assertTrue(otherText.equals(anotherText));
            assertTrue(hypothesis.equals(anotherText));
        }

        @Test
        public void handlesInequality() {
            Hypothesis hypothesisB = Hypothesis.createHypothesis("Some other hypothesis...", Locale.FRANCE);

            assertFalse(hypothesis.equals(hypothesisB));
        }

        @Test
        public void handlesOtherObjects() {
            String hypothesisB = "not a hypothesis";

            assertFalse(hypothesis.equals(hypothesisB));
        }

        @Test
        public void handlesNull() {
            assertFalse(hypothesis.equals(null));
        }

        @Test
        public void ifSameIdThenEqual() {
            mockCreateId();
            Hypothesis hypothesisB =
                    Hypothesis.createHypothesis("Completely different hypothesis", Locale.FRANCE);

            assertTrue(hypothesis.equals(hypothesisB));
        }

        @Test
        public void ifDifferentIdThenNotEqual() {
            Hypothesis hypothesisB =
                    Hypothesis.createHypothesis(hypothesis.getClaim(), hypothesis.getLocale());

            assertFalse(hypothesis.equals(hypothesisB));
        }
    }

    @Tested
    Hypothesis hypothesis = Hypothesis.createHypothesis("Some hypothesis...", Locale.GERMANY);

    @Test
    public void toStringFormat() {
        assertEquals("{ \"claim\": \"Some hypothesis...\", \"score\": { \"reliability\": 0.50, \"confidence\": 0.00 } }", hypothesis.toString());
    }

    /**
     * Test the ser/de configuration of the class, e.g handle private setters
     * @throws IOException if ser/de fails
     */
    @Test
    public void jsonConversion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // TODO: add/remove reviews
        String asJson = mapper.writeValueAsString(hypothesis);
        Hypothesis restored = mapper.readValue(asJson, Hypothesis.class);

        assertEquals(hypothesis.getHid(), restored.getHid());
        assertEquals(hypothesis.getClaim(), restored.getClaim());
        assertEquals(hypothesis.getScore(), restored.getScore());
        assertEquals(hypothesis.getLocale(), restored.getLocale());
        assertEquals(hypothesis.getReviews(), restored.getReviews());
        assertEquals(hypothesis.getReferences(), restored.getReferences());
    }


}
