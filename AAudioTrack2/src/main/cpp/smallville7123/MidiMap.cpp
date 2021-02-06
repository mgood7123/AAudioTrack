//
// Created by Matthew Good on 4/2/21.
//

#include "MidiMap.h"

const char * MidiMap::KEY_B10 = "B10";
const char * MidiMap::KEY_ASharp10 = "A#10";
const char * MidiMap::KEY_A10 = "A10";
const char * MidiMap::KEY_GSharp10 = "G#8";
const char * MidiMap::KEY_G10 = "G10";
const char * MidiMap::KEY_FSharp10 = "F#10";
const char * MidiMap::KEY_F10 = "F10";
const char * MidiMap::KEY_E10 = "E10";
const char * MidiMap::KEY_DSharp10 = "D#10";
const char * MidiMap::KEY_D10 = "D10";
const char * MidiMap::KEY_CSharp10 = "C#10";
const char * MidiMap::KEY_C10 = "C10";

const char * MidiMap::KEY_B9 = "B9";
const char * MidiMap::KEY_ASharp9 = "A#9";
const char * MidiMap::KEY_A9 = "A9";
const char * MidiMap::KEY_GSharp9 = "G#8";
const char * MidiMap::KEY_G9 = "G9";
const char * MidiMap::KEY_FSharp9 = "F#9";
const char * MidiMap::KEY_F9 = "F9";
const char * MidiMap::KEY_E9 = "E9";
const char * MidiMap::KEY_DSharp9 = "D#9";
const char * MidiMap::KEY_D9 = "D9";
const char * MidiMap::KEY_CSharp9 = "C#9";
const char * MidiMap::KEY_C9 = "C9";

const char * MidiMap::KEY_B8 = "B8";
const char * MidiMap::KEY_ASharp8 = "A#8";
const char * MidiMap::KEY_A8 = "A8";
const char * MidiMap::KEY_GSharp8 = "G#8";
const char * MidiMap::KEY_G8 = "G8";
const char * MidiMap::KEY_FSharp8 = "F#8";
const char * MidiMap::KEY_F8 = "F8";
const char * MidiMap::KEY_E8 = "E8";
const char * MidiMap::KEY_DSharp8 = "D#8";
const char * MidiMap::KEY_D8 = "D8";
const char * MidiMap::KEY_CSharp8 = "C#8";
const char * MidiMap::KEY_C8 = "C8";

const char * MidiMap::KEY_B7 = "B7";
const char * MidiMap::KEY_ASharp7 = "A#7";
const char * MidiMap::KEY_A7 = "A7";
const char * MidiMap::KEY_GSharp7 = "G#7";
const char * MidiMap::KEY_G7 = "G7";
const char * MidiMap::KEY_FSharp7 = "F#7";
const char * MidiMap::KEY_F7 = "F7";
const char * MidiMap::KEY_E7 = "E7";
const char * MidiMap::KEY_DSharp7 = "D#7";
const char * MidiMap::KEY_D7 = "D7";
const char * MidiMap::KEY_CSharp7 = "C#7";
const char * MidiMap::KEY_C7 = "C7";

const char * MidiMap::KEY_B6 = "B6";
const char * MidiMap::KEY_ASharp6 = "A#6";
const char * MidiMap::KEY_A6 = "A6";
const char * MidiMap::KEY_GSharp6 = "G#6";
const char * MidiMap::KEY_G6 = "G6";
const char * MidiMap::KEY_FSharp6 = "F#6";
const char * MidiMap::KEY_F6 = "F6";
const char * MidiMap::KEY_E6 = "E6";
const char * MidiMap::KEY_DSharp6 = "D#6";
const char * MidiMap::KEY_D6 = "D6";
const char * MidiMap::KEY_CSharp6 = "C#6";
const char * MidiMap::KEY_C6 = "C6";

const char * MidiMap::KEY_B5 = "B5";
const char * MidiMap::KEY_ASharp5 = "A#5";
const char * MidiMap::KEY_A5 = "A5";
const char * MidiMap::KEY_GSharp5 = "G#5";
const char * MidiMap::KEY_G5 = "G5";
const char * MidiMap::KEY_FSharp5 = "F#5";
const char * MidiMap::KEY_F5 = "F5";
const char * MidiMap::KEY_E5 = "E5";
const char * MidiMap::KEY_DSharp5 = "D#5";
const char * MidiMap::KEY_D5 = "D5";
const char * MidiMap::KEY_CSharp5 = "C#5";
const char * MidiMap::KEY_C5 = "C5";

const char * MidiMap::KEY_B4 = "B4";
const char * MidiMap::KEY_ASharp4 = "A#4";
const char * MidiMap::KEY_A4 = "A4";
const char * MidiMap::KEY_GSharp4 = "G#4";
const char * MidiMap::KEY_G4 = "G4";
const char * MidiMap::KEY_FSharp4 = "F#4";
const char * MidiMap::KEY_F4 = "F4";
const char * MidiMap::KEY_E4 = "E4";
const char * MidiMap::KEY_DSharp4 = "D#4";
const char * MidiMap::KEY_D4 = "D4";
const char * MidiMap::KEY_CSharp4 = "C#4";
const char * MidiMap::KEY_C4 = "C4";

const char * MidiMap::KEY_B3 = "B3";
const char * MidiMap::KEY_ASharp3 = "A#3";
const char * MidiMap::KEY_A3 = "A3";
const char * MidiMap::KEY_GSharp3 = "G#3";
const char * MidiMap::KEY_G3 = "G3";
const char * MidiMap::KEY_FSharp3 = "F#3";
const char * MidiMap::KEY_F3 = "F3";
const char * MidiMap::KEY_E3 = "E3";
const char * MidiMap::KEY_DSharp3 = "D#3";
const char * MidiMap::KEY_D3 = "D3";
const char * MidiMap::KEY_CSharp3 = "C#3";
const char * MidiMap::KEY_C3 = "C3";

const char * MidiMap::KEY_B2 = "B2";
const char * MidiMap::KEY_ASharp2 = "A#2";
const char * MidiMap::KEY_A2 = "A2";
const char * MidiMap::KEY_GSharp2 = "G#2";
const char * MidiMap::KEY_G2 = "G2";
const char * MidiMap::KEY_FSharp2 = "F#2";
const char * MidiMap::KEY_F2 = "F2";
const char * MidiMap::KEY_E2 = "E2";
const char * MidiMap::KEY_DSharp2 = "D#2";
const char * MidiMap::KEY_D2 = "D2";
const char * MidiMap::KEY_CSharp2 = "C#2";
const char * MidiMap::KEY_C2 = "C2";

const char * MidiMap::KEY_B1 = "B1";
const char * MidiMap::KEY_ASharp1 = "A#1";
const char * MidiMap::KEY_A1 = "A1";
const char * MidiMap::KEY_GSharp1 = "G#1";
const char * MidiMap::KEY_G1 = "G1";
const char * MidiMap::KEY_FSharp1 = "F#1";
const char * MidiMap::KEY_F1 = "F1";
const char * MidiMap::KEY_E1 = "E1";
const char * MidiMap::KEY_DSharp1 = "D#1";
const char * MidiMap::KEY_D1 = "D1";
const char * MidiMap::KEY_CSharp1 = "C#1";
const char * MidiMap::KEY_C1 = "C1";

const char * MidiMap::KEY_B0 = "B-1";
const char * MidiMap::KEY_ASharp0 = "A#0";
const char * MidiMap::KEY_A0 = "A0";
const char * MidiMap::KEY_GSharp0 = "G#0";
const char * MidiMap::KEY_G0 = "G0";
const char * MidiMap::KEY_FSharp0 = "F#0";
const char * MidiMap::KEY_F0 = "F0";
const char * MidiMap::KEY_E0 = "E0";
const char * MidiMap::KEY_DSharp0 = "D#0";
const char * MidiMap::KEY_D0 = "D0";
const char * MidiMap::KEY_CSharp0 = "C#0";
const char * MidiMap::KEY_C0 = "C0";

std::string MidiMap::notes[12] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
int MidiMap::min = 0;
int MidiMap::max = 10;
double MidiMap::baseFrequency = 440.0;
MidiMap::value_type MidiMap::baseData = MidiMap::genBaseData();

const std::string MidiMap::getNoteNameFromMidiNumber(value_type & data, const int midiNumber) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<1>(data[i]) == midiNumber) return std::get<0>(data[i]);
    }
    return "";
}

const std::string MidiMap::getNoteNameFromMidiNumber(const int midiNumber) {
    return getNoteNameFromMidiNumber(baseData, midiNumber);
}

const std::string MidiMap::getNoteNameFromNoteFrequency(value_type & data, const double noteFrequency) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<2>(data[i]) == noteFrequency) return std::get<0>(data[i]);
    }
    return "";
}

const std::string MidiMap::getNoteNameFromNoteFrequency(const double noteFrequency) {
    return getNoteNameFromNoteFrequency(baseData, noteFrequency);
}

const int MidiMap::getMidiNumberFromNoteName(value_type & data, std::string noteName) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<0>(data[i]) == noteName) return std::get<1>(data[i]);
    }
    return -1;
}

const int MidiMap::getMidiNumberFromNoteName(std::string noteName) {
    return getMidiNumberFromNoteName(baseData, noteName);
}

const int MidiMap::getMidiNumberFromNoteFrequency(value_type & data, const double noteFrequency) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<2>(data[i]) == noteFrequency) return std::get<1>(data[i]);
    }
    return -1;
}

const int MidiMap::getMidiNumberFromNoteFrequency(const double noteFrequency) {
    return getMidiNumberFromNoteFrequency(baseData, noteFrequency);
}

const double MidiMap::getNoteFrequencyFromNoteName(value_type & data, std::string noteName) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<0>(data[i]) == noteName) return std::get<2>(data[i]);
    }
    return -1;
}

const double MidiMap::getNoteFrequencyFromNoteName(std::string noteName) {
    return getNoteFrequencyFromNoteName(baseData, noteName);
}

const double MidiMap::getNoteFrequencyFromMidiNumber(value_type & data, const int midiNumber) {
    for (size_t i = 0; i < data.size(); i++) {
        if (std::get<1>(data[i]) == midiNumber) return std::get<2>(data[i]);
    }
    return -1;
}

const double MidiMap::getNoteFrequencyFromMidiNumber(const int midiNumber) {
    return getNoteFrequencyFromMidiNumber(baseData, midiNumber);
}

const std::string MidiMap::getRootNoteName(value_type & data) {
    return getNoteNameFromNoteFrequency(data, baseFrequency);
}

const std::string MidiMap::getRootNoteName() {
    return getNoteNameFromNoteFrequency(baseData, baseFrequency);
}

const int MidiMap::getRootMidiNumber(value_type & data) {
    return getMidiNumberFromNoteFrequency(data, baseFrequency);
}

const int MidiMap::getRootMidiNumber() {
    return getMidiNumberFromNoteFrequency(baseData, baseFrequency);
}

const double MidiMap::getRootFrequency() {
    return baseFrequency;
}
