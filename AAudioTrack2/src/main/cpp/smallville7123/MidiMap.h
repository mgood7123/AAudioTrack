//
// Created by Matthew Good on 3/2/21.
//

#ifndef AAUDIOTRACK_MIDIMAP_H
#define AAUDIOTRACK_MIDIMAP_H

//g++  7.4.0

#include <math.h>
#include <vector>
#include <tuple>
#include <string>
#include <iostream>
#include <iomanip>

class MidiMap {
public:

    static const char * KEY_B10;
    static const char * KEY_ASharp10;
    static const char * KEY_A10;
    static const char * KEY_GSharp10;
    static const char * KEY_G10;
    static const char * KEY_FSharp10;
    static const char * KEY_F10;
    static const char * KEY_E10;
    static const char * KEY_DSharp10;
    static const char * KEY_D10;
    static const char * KEY_CSharp10;
    static const char * KEY_C10;

    static const char * KEY_B9;
    static const char * KEY_ASharp9;
    static const char * KEY_A9;
    static const char * KEY_GSharp9;
    static const char * KEY_G9;
    static const char * KEY_FSharp9;
    static const char * KEY_F9;
    static const char * KEY_E9;
    static const char * KEY_DSharp9;
    static const char * KEY_D9;
    static const char * KEY_CSharp9;
    static const char * KEY_C9;

    static const char * KEY_B8;
    static const char * KEY_ASharp8;
    static const char * KEY_A8;
    static const char * KEY_GSharp8;
    static const char * KEY_G8;
    static const char * KEY_FSharp8;
    static const char * KEY_F8;
    static const char * KEY_E8;
    static const char * KEY_DSharp8;
    static const char * KEY_D8;
    static const char * KEY_CSharp8;
    static const char * KEY_C8;

    static const char * KEY_B7;
    static const char * KEY_ASharp7;
    static const char * KEY_A7;
    static const char * KEY_GSharp7;
    static const char * KEY_G7;
    static const char * KEY_FSharp7;
    static const char * KEY_F7;
    static const char * KEY_E7;
    static const char * KEY_DSharp7;
    static const char * KEY_D7;
    static const char * KEY_CSharp7;
    static const char * KEY_C7;

    static const char * KEY_B6;
    static const char * KEY_ASharp6;
    static const char * KEY_A6;
    static const char * KEY_GSharp6;
    static const char * KEY_G6;
    static const char * KEY_FSharp6;
    static const char * KEY_F6;
    static const char * KEY_E6;
    static const char * KEY_DSharp6;
    static const char * KEY_D6;
    static const char * KEY_CSharp6;
    static const char * KEY_C6;

    static const char * KEY_B5;
    static const char * KEY_ASharp5;
    static const char * KEY_A5;
    static const char * KEY_GSharp5;
    static const char * KEY_G5;
    static const char * KEY_FSharp5;
    static const char * KEY_F5;
    static const char * KEY_E5;
    static const char * KEY_DSharp5;
    static const char * KEY_D5;
    static const char * KEY_CSharp5;
    static const char * KEY_C5;

    static const char * KEY_B4;
    static const char * KEY_ASharp4;
    static const char * KEY_A4;
    static const char * KEY_GSharp4;
    static const char * KEY_G4;
    static const char * KEY_FSharp4;
    static const char * KEY_F4;
    static const char * KEY_E4;
    static const char * KEY_DSharp4;
    static const char * KEY_D4;
    static const char * KEY_CSharp4;
    static const char * KEY_C4;

    static const char * KEY_B3;
    static const char * KEY_ASharp3;
    static const char * KEY_A3;
    static const char * KEY_GSharp3;
    static const char * KEY_G3;
    static const char * KEY_FSharp3;
    static const char * KEY_F3;
    static const char * KEY_E3;
    static const char * KEY_DSharp3;
    static const char * KEY_D3;
    static const char * KEY_CSharp3;
    static const char * KEY_C3;

    static const char * KEY_B2;
    static const char * KEY_ASharp2;
    static const char * KEY_A2;
    static const char * KEY_GSharp2;
    static const char * KEY_G2;
    static const char * KEY_FSharp2;
    static const char * KEY_F2;
    static const char * KEY_E2;
    static const char * KEY_DSharp2;
    static const char * KEY_D2;
    static const char * KEY_CSharp2;
    static const char * KEY_C2;

    static const char * KEY_B1;
    static const char * KEY_ASharp1;
    static const char * KEY_A1;
    static const char * KEY_GSharp1;
    static const char * KEY_G1;
    static const char * KEY_FSharp1;
    static const char * KEY_F1;
    static const char * KEY_E1;
    static const char * KEY_DSharp1;
    static const char * KEY_D1;
    static const char * KEY_CSharp1;
    static const char * KEY_C1;

    static const char * KEY_B0;
    static const char * KEY_ASharp0;
    static const char * KEY_A0;
    static const char * KEY_GSharp0;
    static const char * KEY_G0;
    static const char * KEY_FSharp0;
    static const char * KEY_F0;
    static const char * KEY_E0;
    static const char * KEY_DSharp0;
    static const char * KEY_D0;
    static const char * KEY_CSharp0;
    static const char * KEY_C0;

    typedef std::tuple<std::string, int, double> sub_value_type;
    typedef std::vector<sub_value_type> value_type;

    static int min;
    static int max;
    static std::string notes[];
    static value_type baseData;

    static double noteToFreq(double root_note, double note) {
        return root_note * pow(2.0, (note - 69.0) / 12.0);
    }

    static double baseFrequency;

    static value_type genBaseData() {
        value_type data;
        int noteNumber = 0;
        for (int i = min; i <= max+1; i++) {
            for (int _i = 0; _i < 12; _i++) {
                std::string note = notes[_i] + std::to_string(i);
                float x = noteToFreq(baseFrequency, noteNumber);
                data.push_back(sub_value_type(note, noteNumber, x));
                noteNumber++;
            }
        }
        return data;
    }

    static value_type genData(std::string rootNote) {
        value_type data;
        double frequency = baseFrequency;

        int root = 0;
        int offset = 0;

        for (size_t i = 0; i < baseData.size(); i++) {
            if (std::get<2>(baseData[i]) == frequency) root = i;
            if (std::get<0>(baseData[i]) == rootNote) offset = i;
        }

        int index = root - offset;
        frequency = get<2>(baseData[root + index]);

        int noteNumber = 0;
        for (int i = min; i <= max; i++) {
            for (int _i = 0; _i < 12; _i++) {
                std::string note = notes[_i] + std::to_string(i);
                float x = noteToFreq(frequency, noteNumber);
                data.push_back(sub_value_type(note, noteNumber, x));
                noteNumber++;
            }
        }
        return data;
    }

    static value_type genData() {
        value_type data;
        double frequency = baseFrequency;
        int noteNumber = 0;
        for (int i = min; i <= max; i++) {
            for (int _i = 0; _i < 12; _i++) {
                std::string note = notes[_i] + std::to_string(i);
                float x = noteToFreq(frequency, noteNumber);
                data.push_back(sub_value_type(note, noteNumber, x));
                noteNumber++;
            }
        }
        return data;
    }

    static void printVector(value_type & data) {
        for (sub_value_type item : data) {
            std::cout << std::fixed;
            double x = std::get<2>(item);
            if (x < 10.0) std::cout << std::setprecision(6);
            else if (x < 100.0) std::cout << std::setprecision(5);
            else if (x < 1000.0) std::cout << std::setprecision(4);
            else if (x < 10000.0) std::cout << std::setprecision(3);
            else if (x < 100000.0) std::cout << std::setprecision(2);
            else if (x < 1000000.0) std::cout << std::setprecision(1);
            std::cout << std::get<0>(item) << "\t=\t" << x << std::endl;
        }
    }

    static void print(value_type & data, std::string rootNote) {
        for (sub_value_type item : data) {
            std::string n = std::get<0>(item);
            if (n == rootNote) {
                std::cout << std::fixed;
                double x = std::get<2>(item);
                if (x < 10.0) std::cout << std::setprecision(6);
                else if (x < 100.0) std::cout << std::setprecision(5);
                else if (x < 1000.0) std::cout << std::setprecision(4);
                else if (x < 10000.0) std::cout << std::setprecision(3);
                else if (x < 100000.0) std::cout << std::setprecision(2);
                else if (x < 1000000.0) std::cout << std::setprecision(1);
                std::cout << n << "\t=\t" << x << std::endl;
            }
        }
    }

    static void print(std::string rootNote) {
        value_type vector = genData(rootNote);
        print(vector, rootNote);
    }

    static const std::string getNoteNameFromMidiNumber(value_type & data, const int midiNumber);
    static const std::string getNoteNameFromMidiNumber(const int midiNumber);

    static const std::string getNoteNameFromNoteFrequency(value_type & data, const double noteFrequency);
    static const std::string getNoteNameFromNoteFrequency(const double noteFrequency);

    static const int getMidiNumberFromNoteName(value_type & data, std::string noteName);
    static const int getMidiNumberFromNoteName(std::string noteName);

    static const int getMidiNumberFromNoteFrequency(value_type & data, const double noteFrequency);
    static const int getMidiNumberFromNoteFrequency(const double noteFrequency);

    static const double getNoteFrequencyFromNoteName(value_type & data, std::string noteName);
    static const double getNoteFrequencyFromNoteName(std::string noteName);

    static const double getNoteFrequencyFromMidiNumber(value_type & data, const int midiNumber);
    static const double getNoteFrequencyFromMidiNumber(const int midiNumber);

    static const std::string getRootNoteName(value_type & data);
    static const std::string getRootNoteName();

    static const int getRootMidiNumber(value_type & data);
    static const int getRootMidiNumber();

    static const double getRootFrequency();
};

#endif //AAUDIOTRACK_MIDIMAP_H
