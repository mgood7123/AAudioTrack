package smallville7123.aaudiotrack.application;

import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.AAudioTrack2;

public class Instrument {

    public Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons;
    public String instrumentName;
    public AAudioTrack2 sound;

    public Instrument(Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons, String instrumentName) {
        this.buttons = buttons;
        this.instrumentName = instrumentName;
    }

    public Instrument(Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons, String instrumentName, AAudioTrack2 sound) {
        this.buttons = buttons;
        this.instrumentName = instrumentName;
        this.sound = sound;
    }


    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setButtons(Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons) {
        this.buttons = buttons;
    }

    public void setSound(AAudioTrack2 sound) {
        this.sound = sound;
    }

    public String getInstrumentName() {
        return this.instrumentName;
    }

    public Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> getButtons() {
        return this.buttons;
    }

    public AAudioTrack2 getSound(){
        return this.sound;
    }

    public Pair<ToggleButton, ToggleButton> getButtonAt(int i) {
        return new Pair<>(this.buttons.first.second.get(i), this.buttons.second.second.get(i));
    }

}
