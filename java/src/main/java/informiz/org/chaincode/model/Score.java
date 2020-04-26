package informiz.org.chaincode.model;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class Score {
    @Property(schema = {"minimum", "0", "maximum", "1"})
    private Float reliability;
    @Property(schema = {"minimum", "0", "maximum", "1"})
    private Float confidence;

    public Score() {
        setReliability(0.5f);
        setConfidence(0.0f);
    }

    public Score(float reliability, float confidence) {
        // TODO: test score out-of-bound values
        assert reliability > 0.0 && reliability < 1.0;
        assert confidence > 0.0 && confidence < 1.0;
        setReliability(reliability);
        setConfidence(confidence);
    }

    public Float getReliability() {
        return reliability;
    }

    public void setReliability(Float reliability) {
        this.reliability = reliability;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Score other = (Score) obj;

        return this.reliability.equals(other.reliability) && this.confidence.equals(other.confidence);
    }

    // TODO: make comparable?

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"reliability\": %.2f, \"confidence\": %.2f }", reliability, confidence);
    }

}
