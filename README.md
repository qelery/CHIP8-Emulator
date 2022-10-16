# CHIP8 Emulator
My CHIP-8 emulator built in Java.

[CHIP-8](https://en.wikipedia.org/wiki/CHIP-8) was an interpreted programming language, developed in the mid-1970s to allow video games to be more easily programmed for the computers of its time. CHIP-8 programs are run on a CHIP-8 virtual machine.

## Requirements

* Java 17+


## Usage

* Clone and download this repository
* In your terminal, navigate to the root of the repository
* Run `./mvnw clean javafx:run` for macOS/Linux or `mvnw.cmd clean javafx:run` for Windows
* Instructions on selecting a game will appear in the terminal
* Troubleshooting instructions are below


## Images
<img src="/images/1.png"  width="614" height="307" alt="">

ROM selection

<img src="/images/2.png"  width="614" height="307" alt="">

Space Invaders Game ROM

<img src="/images/3.png"  width="614" height="307" alt="">

Hidden Game ROM

<img src="/images/4.png"  width="614" height="307" alt="">

Instructions for a ROM


## Troubleshooting

* If getting a mvnw permissions error in Linux terminal, try running `chmod +x mvnw` then running the program
* If getting a mvnw permissions error in Window Powershell, try running `.\mvnw.cmd clean javafx:run`