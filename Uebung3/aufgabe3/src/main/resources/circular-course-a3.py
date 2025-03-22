#!/usr/bin/env python3
import sys
import json
import random

def generate_tracks(num_tracks: int, length_of_track: int):
    """
    Erzeugt Rundkurse mit zusätzlichen Segmenttypen (caesar, bottleneck) und zufälligen Verzweigungen.
    """
    all_tracks = []

    for t in range(1, num_tracks + 1):
        track_id = str(t)
        segments = []

        # Start-Segment
        start_segment_id = f"start-and-goal-{t}"
        first_seg_id = f"segment-{t}-1"
        start_segment = {
            "segmentId": start_segment_id,
            "type": "start-goal",
            "nextSegments": [first_seg_id]
        }
        segments.append(start_segment)

        last_segment_id = ""
        for c in range(1, length_of_track):
            seg_id = f"segment-{t}-{c}"

            # Letztes Segment zeigt auf Start
            if c == length_of_track - 1:
                next_segs = [start_segment_id]
            else:
                # 20% Chance auf Verzweigung
                next1 = f"segment-{t}-{c+1}"
                next_segs = [next1]
                if random.random() < 0.2 and c < length_of_track - 2:
                    next2 = f"segment-{t}-{c+2}"
                    next_segs.append(next2)

            # 1 von 3 Segmenten wird zu Caesar oder Bottleneck
            seg_type = "normal"
            rand = random.random()
            if rand < 0.1:
                seg_type = "caesar"
            elif rand < 0.2:
                seg_type = "bottleneck"

            segment = {
                "segmentId": seg_id,
                "type": seg_type,
                "nextSegments": next_segs
            }
            segments.append(segment)

        track_definition = {
            "trackId": track_id,
            "segments": segments
        }
        all_tracks.append(track_definition)

    return {"tracks": all_tracks}


def main():
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <num_tracks> <length_of_track> <output_file>")
        sys.exit(1)

    num_tracks = int(sys.argv[1])
    length_of_track = int(sys.argv[2])
    output_file = sys.argv[3]

    tracks_data = generate_tracks(num_tracks, length_of_track)

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(tracks_data, f, indent=2)
        f.write('\n')
    print(f"Generated {num_tracks} track(s) of length {length_of_track} into '{output_file}'")

if __name__ == "__main__":
    main()
