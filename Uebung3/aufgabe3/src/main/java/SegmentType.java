/**
 * Enum für die verschiedenen Typen von Segmenten.
 */
public enum SegmentType {
    START_GOAL("start-goal"),
    NORMAL("normal"),
    CAESAR("caesar"),
    BOTTLENECK("bottleneck");

    public final String label;

    SegmentType(String label) {
        this.label = label;
    }

    public static SegmentType from(String label) {
        for (SegmentType type : values()) {
            if (type.label.equals(label)) return type;
        }
        throw new IllegalArgumentException("Unknown segment type: " + label);
    }
}
