#!/usr/bin/env python3
import sys
import json
import random

def generate_shared_track(num_players: int, length_of_track: int):
    """
    Generiert eine Strecke mit genau einem Track, der von allen Spielern gemeinsam genutzt wird.
    Segmenttypen: normal, caesar, bottleneck.
    """
    segments = []

    # Start- und Zielsegment
    start_segment_id = "start-and-goal"
    first_seg_id = "segment-1"
    segments.append({
        "segmentId": start_segment_id,
        "type": "start-goal",
        "nextSegments": [first_seg_id]
    })

    # Zwischen-Segmente
    for i in range(1, length_of_track):
        seg_id = f"segment-{i}"

        if i == length_of_track - 1:
            next_segments = [start_segment_id]
        else:
            next1 = f"segment-{i+1}"
            next_segments = [next1]
            # 20% Chance auf eine zusätzliche Verzweigung
            if random.random() < 0.2 and i < length_of_track - 2:
                next2 = f"segment-{i+2}"
                next_segments.append(next2)

        # 1 von 3 Segmenten wird zu Caesar oder Bottleneck
        seg_type = "normal"
        r = random.random()
        if r < 0.1:
            seg_type = "caesar"
        elif r < 0.2:
            seg_type = "bottleneck"

        segments.append({
            "segmentId": seg_id,
            "type": seg_type,
            "nextSegments": next_segments
        })

    return {
        "tracks": [
            {
                "trackId": "shared",
                "segments": segments
            }
        ],
        "players": num_players
    }

def main():
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <num_players> <length_of_track> <output_file>")
        sys.exit(1)

    num_players = int(sys.argv[1])
    length_of_track = int(sys.argv[2])
    output_file = sys.argv[3]

    track_data = generate_shared_track(num_players, length_of_track)

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(track_data, f, indent=2)
        f.write('\n')

    print(f"Generated shared track with {length_of_track} segments for {num_players} players → '{output_file}'")

if __name__ == "__main__":
    main()
