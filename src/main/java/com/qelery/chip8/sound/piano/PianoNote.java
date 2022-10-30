package com.qelery.chip8.sound.piano;

import com.qelery.chip8.sound.Sound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class PianoNote implements Sound {

    private static final Logger logger = LogManager.getLogger(PianoNote.class);
    private Synthesizer synthesizer;
    private MidiChannel midiChannel;
    private final int noteNumber;

    public PianoNote(MusicalNote musicalNote) {
        this.noteNumber = musicalNote.getMidiValue();
        try {
            this.synthesizer = MidiSystem.getSynthesizer();
            this.synthesizer.open();
            this.midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            logger.info("Error created sound. Midi unavailable.");
        }
    }

    @Override
    public void play() {
        if (midiChannel != null) {
            int velocity = 80; // a reasonable volume
            midiChannel.noteOn(noteNumber, velocity);
        } else {
            logger.info("Beep! (No sound available)");
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
