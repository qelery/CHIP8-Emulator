package com.qelery.chip8.sound.piano;

import com.qelery.chip8.sound.Sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class PianoSound implements Sound {

    private Synthesizer synthesizer;
    private MidiChannel midiChannel;
    private final int noteNumber;

    public PianoSound(PianoNote pianoNote) {
        this.noteNumber = pianoNote.getMidiValue();
        try {
            this.synthesizer = MidiSystem.getSynthesizer();
            this.synthesizer.open();
            this.midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            System.out.println("Error created sound.");
        }
    }

    @Override
    public void play() {
        if (midiChannel != null) {
            int velocity = 80; // a reasonable volume
            midiChannel.noteOn(noteNumber, velocity);
        } else {
            System.out.println("Beep!");
        }
    }

    @Override
    public void stop() {
        if (midiChannel != null) {
            midiChannel.noteOff(noteNumber);
        }
    }

    @Override
    public void closeLine() {
        synthesizer.close();
    }
}
