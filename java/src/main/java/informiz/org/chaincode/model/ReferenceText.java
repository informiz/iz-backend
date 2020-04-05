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
 * A data-type managed by the reference-text contract. A reference text consists of:
 * - the text
 * - a locale
 * - the id of the source for the reference (e.g the NASA website)
 * - a link to the source of the reference (e.g the specific page on the NASA website)
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@DataType()
public final class ReferenceText {

    @Property()
    private String tid;

    @Property()
    private String text;

    @Property()
    private Locale locale;

    @Property()
    private String sid;

    @Property()
    private String link;

    @Property()
    private Score score;

    @Property()
    private HashMap<String, Float> reviews = new HashMap<>();

    private ReferenceText() {
        setTid(Utils.createUuid(ContractType.REF_TEXT));
        setScore(new Score());
    }

    public static ReferenceText createRefText(String text, String sid, String link, Locale locale) {
        ReferenceText refText = new ReferenceText();
        refText.setText(text);
        refText.setLocale(locale);
        refText.setSid(sid);
        refText.setLink(link);
        return refText;
    }

        public String getTid() {
        return tid;
    }

    private void setTid(String tid) {
        this.tid = tid;
    }

    public String getText() {
        return text;
    }

    // TODO: should it be allowed to change the text?
    private void setText(String text) {
        this.text = text;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Add a review by a fact-checker to this reference-text
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this text before
     * @see Map#put(Object, Object)
     */
    public Float addReview(String fcid, float reliability) {
        return reviews.put(fcid, reliability);
    }

    /**
     * Remove a review by a fact-checker from this reference-text
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

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        ReferenceText other = (ReferenceText) obj;

        return this.tid.equals(other.tid);
    }

    @Override
    public int hashCode() {
        return this.tid.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"text\": \"%s\", \"score\": %s }", text, score.toString());
    }
}
