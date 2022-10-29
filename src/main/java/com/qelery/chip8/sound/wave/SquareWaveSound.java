package com.qelery.chip8.sound.wave;

public class SquareWaveSound extends AbstractWaveSound {

    /**
     * Creates a square wave tone.
     *
     * @param frequency in hertz
     */
    public SquareWaveSound(double frequency) {
        super(frequency);
    }

    @Override
    byte[] createToneBuffer() {
        int waveLength = 1000;
        int samples = (waveLength * SAMPLE_RATE) / 1000;
        byte[] toneBuffer = new byte[samples];
        double period = SAMPLE_RATE / frequency;
        double volume = 0.05; // volume > 0.1 is loud!
        for (int i = 0; i < toneBuffer.length; i++) {
            double angle = 2.0 * Math.PI * i / period;
            float byteAmplitude = 127f;
            double sinPoint = Math.sin(angle) * byteAmplitude * volume;
            toneBuffer[i] = (byte) Math.signum(sinPoint);
        }
        return toneBuffer;
    }
}
