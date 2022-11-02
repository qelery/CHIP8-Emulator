package com.qelery.chip8.components.sound.piano;

public enum MusicalNote {

    C(72),
    D(74),
    E(76),
    F(77),
    G(79),
    A(81),
    B(83);

    private final int midiValue;

    MusicalNote(int midiValue) {
        this.midiValue = midiValue;
    }

    public int getMidiValue() {
        return midiValue;
    }
}
