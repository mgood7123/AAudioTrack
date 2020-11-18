package smallville7123.aaudiotrack;

import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class Instrument {

    public Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons;
    public String instrumentName;
    public AAudioTrack sound;

    public Instrument(Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons, String instrumentName) {
        this.buttons = buttons;
        this.instrumentName = instrumentName;
    }

    public Instrument(Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> buttons, String instrumentName, AAudioTrack sound) {
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

    public void setSound(AAudioTrack sound) {
        this.sound = sound;
    }

    public String getInstrumentName() {
        return this.instrumentName;
    }

    public Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> getButtons() {
        return this.buttons;
    }

    public AAudioTrack getSound(){
        return this.sound;
    }

    public Pair<ToggleButton, ToggleButton> getButtonAt(int i) {
        return new Pair<>(this.buttons.first.second.get(i), this.buttons.second.second.get(i));
    }

}
