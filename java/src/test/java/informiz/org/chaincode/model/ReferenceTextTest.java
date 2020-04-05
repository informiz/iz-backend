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
public final class ReferenceTextTest {

    @Nested
    class Equality {

        @Tested
        ReferenceText text = ReferenceText.createRefText("Some text...", "src_111", "link_111", Locale.GERMANY);

        private void mockCreateId() {
            new MockUp<Utils>() {
                @Mock
                public String createUuid(ContractType contractType) {
                    return text.getTid();
                }
            };
        }


        @Test
        public void isReflexive() {
            assertTrue(text.equals(text));
        }

        @Test
        public void isSymmetric() {
            mockCreateId();
            ReferenceText otherText = ReferenceText.createRefText("Some text...", "src_111", "link_111", Locale.GERMANY);

            assertTrue(text.equals(otherText));
            assertTrue(otherText.equals(text));
        }

        @Test
        public void isTransitive() {
            mockCreateId();
            ReferenceText otherText = ReferenceText.createRefText("Some text...", "src_111", "link_111", Locale.GERMANY);
            ReferenceText anotherText = ReferenceText.createRefText("Some text...", "src_111", "link_111", Locale.GERMANY);

            assertTrue(text.equals(otherText));
            assertTrue(otherText.equals(anotherText));
            assertTrue(text.equals(anotherText));
        }

        @Test
        public void handlesInequality() {
            ReferenceText textB = ReferenceText.createRefText("Some other text...", "src_222", "link_222", Locale.FRANCE);

            assertFalse(text.equals(textB));
        }

        @Test
        public void handlesOtherObjects() {
            String textB = "not a text";

            assertFalse(text.equals(textB));
        }

        @Test
        public void handlesNull() {
            assertFalse(text.equals(null));
        }

        @Test
        public void ifSameIdThenEqual() {
            mockCreateId();
            ReferenceText textB =
                    ReferenceText.createRefText("Completely different text", "different_src", "different_link", Locale.FRANCE);

            assertTrue(text.equals(textB));
        }

        @Test
        public void ifDifferentIdThenNotEqual() {
            ReferenceText textB =
                    ReferenceText.createRefText(text.getText(), text.getSid(), text.getLink(), text.getLocale());

            assertFalse(text.equals(textB));
        }
    }

    @Tested
    ReferenceText text = ReferenceText.createRefText("Some text...", "src_111", "link_111", Locale.GERMANY);

    @Test
    public void toStringFormat() {
        assertEquals("{ \"text\": \"Some text...\", \"score\": { \"reliability\": 0.50, \"confidence\": 0.00 } }", text.toString());
    }

    /**
     * Test the ser/de configuration of the class, e.g handle private setters
     * @throws IOException if ser/de fails
     */
    @Test
    public void jsonConversion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // TODO: add/remove reviews
        String asJson = mapper.writeValueAsString(text);
        ReferenceText restored = mapper.readValue(asJson, ReferenceText.class);

        assertEquals(text.getTid(), restored.getTid());
        assertEquals(text.getSid(), restored.getSid());
        assertEquals(text.getScore(), restored.getScore());
        assertEquals(text.getText(), restored.getText());
        assertEquals(text.getLink(), restored.getLink());
        assertEquals(text.getLocale(), restored.getLocale());
        assertEquals(text.getReviews(), restored.getReviews());
    }


}
