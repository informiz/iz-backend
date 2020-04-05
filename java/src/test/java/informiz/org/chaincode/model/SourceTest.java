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
public final class SourceTest {

    @Nested
    class Equality {

        @Tested
        Source source = Source.createSource("Some source", null);

        private void mockCreateId() {
            new MockUp<Utils>() {
                @Mock
                public String createUuid(ContractType contractType) {
                    return source.getSid();
                }
            };
        }


        @Test
        public void isReflexive() {
            assertTrue(source.equals(source));
        }

        @Test
        public void isSymmetric() {
            mockCreateId();
            Source otherSource = Source.createSource("Some source", null);

            assertTrue(source.equals(otherSource));
            assertTrue(otherSource.equals(source));
        }

        @Test
        public void isTransitive() {
            mockCreateId();
            Source otherSource = Source.createSource("Some source", null);
            Source anotherSource = Source.createSource("Some source", null);

            assertTrue(source.equals(otherSource));
            assertTrue(otherSource.equals(anotherSource));
            assertTrue(source.equals(anotherSource));
        }

        @Test
        public void handlesInequality() {
            Source sourceB = Source.createSource("Completely different source", null);

            assertFalse(source.equals(sourceB));
        }

        @Test
        public void handlesOtherObjects() {
            String sourceB = "not a source";

            assertFalse(source.equals(sourceB));
        }

        @Test
        public void handlesNull() {
            assertFalse(source.equals(null));
        }

        @Test
        public void ifSameIdThenEqual() {
            mockCreateId();
            Source sourceB =
                    Source.createSource("Completely different source-name", new Score(0.9f, 0.8f));

            assertTrue(source.equals(sourceB));
        }

        @Test
        public void ifDifferentIdThenNotEqual() {
            Source sourceB =
                    Source.createSource(source.getName(), source.getScore());

            assertFalse(source.equals(sourceB));
        }
    }

    @Tested
    Source source = Source.createSource("Some source...", null);

    @Test
    public void toStringFormat() {
        assertEquals("{ \"name\": \"Some source...\", \"score\": { \"reliability\": 0.50, \"confidence\": 0.00 } }", source.toString());
    }

    /**
     * Test the ser/de configuration of the class, e.g handle private setters
     * @throws IOException if ser/de fails
     */
    @Test
    public void jsonConversion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // TODO: add/remove reviews
        String asJson = mapper.writeValueAsString(source);
        Source restored = mapper.readValue(asJson, Source.class);

        assertEquals(source.getSid(), restored.getSid());
        assertEquals(source.getScore(), restored.getScore());
        assertEquals(source.getName(), restored.getName());
        assertEquals(source.getReviews(), restored.getReviews());
    }


}
