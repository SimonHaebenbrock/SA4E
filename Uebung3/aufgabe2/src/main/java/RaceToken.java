/**
 * Ein Token, das die Informationen zu einem Rennen enthält.
 */
public class RaceToken {
    public String tokenId;
    public long startTime;
    public int lapCount;
    public int maxLaps;
    public String vehicleId;

    public RaceToken() {}

    public RaceToken(long startTime, int lapCount, int maxLaps, String vehicleId) {
        this.startTime = startTime;
        this.lapCount = lapCount;
        this.maxLaps = maxLaps;
        this.vehicleId = vehicleId;
    }
}
