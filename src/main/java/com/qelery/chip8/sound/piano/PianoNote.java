package com.qelery.chip8.sound.piano;

public enum PianoNote {

    C(72),
    D(74),
    E(76),
    F(77),
    G(79),
    A(81),
    B(83);

    private final int midiValue;

    PianoNote(int midiValue) {
        this.midiValue = midiValue;
    }

    public int getMidiValue() {
        return midiValue;
    }
}
