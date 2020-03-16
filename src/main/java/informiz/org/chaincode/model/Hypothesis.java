package informiz.org.chaincode.model;

/* TODO: need this?
 * SPDX-License-Identifier: Apache-2.0
 */


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A data-type managed by the hypothesis contract. A hypothesis consists of:
 * - a factual claim
 * - a locale
 * - the current reliability/confidence score
 * - supporting references
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@DataType()
public final class Hypothesis {

    @Property()
    private String hid;

    @Property()
    private String claim;

    @Property()
    private Locale locale;

    @Property()
    private Score score;

    @Property()
    private Map<String, Float> reviews = new HashMap<>(); // TODO: keep review dates as well?

    @Property()
    private Map<String, String> references = new HashMap<>(); // TODO: do spark auto-generated encoders support Set<>?

    private Hypothesis() {
        setHid(Utils.createUuid(ContractType.HYPOTHESIS));
        setScore(new Score());
    }

    public static Hypothesis createHypothesis(String claim, Locale locale) {
        Hypothesis hypothesis = new Hypothesis();
        hypothesis.setClaim(claim);
        hypothesis.setLocale(locale);
        return hypothesis;
    }

        public String getHid() {
        return hid;
    }

    private void setHid(String hid) {
        this.hid = hid;
    }

    public String getClaim() {
        return claim;
    }

    // TODO: should it be allowed to change the claim?
    private void setClaim(String claim) {
        this.claim = claim;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Add a review by a fact-checker to this hypothesis
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this hypothesis before
     * @see Map#put(Object, Object)
     */
    public Float addReview(String fcid, float reliability) {
        return reviews.put(fcid, reliability);
    }

    /**
     * Remove a review by a fact-checker from this hypothesis
     * @param fcid the fact-checker's id
     * @return the score given by this fact-checker, if one was found
     * @see Map#remove(Object)
     */
    public Float removeReview(String fcid) {
        return reviews.remove(fcid);
    }

    public Map<String, Float> getReviews() {
        return reviews;
    }

    /**
     * Add a reference to this hypothesis
     * @param tid the reference-text's id on the ledger
     * @return the reference-text's id, if it was already assigned to the hypothesis
     * @see Map#put(Object, Object)
     */
    public String addReference(String tid) {
        return references.put(tid, tid);
    }

    /**
     * Remove a reference from this hypothesis
     * @param tid the reference-text's id
     * @return the reference-text's id, if it was assigned to the hypothesis
     * @see Map#remove(Object)
     */
    public Float removeReference(String tid) {
        return reviews.remove(tid);
    }

    public Map<String, String> getReferences() {
        return references;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Hypothesis other = (Hypothesis) obj;

        return this.hid.equals(other.hid);
    }

    @Override
    public int hashCode() {
        return this.hid.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"claim\": \"%s\", \"score\": %s }", claim, score.toString());
    }
}
