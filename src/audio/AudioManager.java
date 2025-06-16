package audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private final Map<String, Clip> sounds;

    //-- 1. Variabel baru untuk menyimpan status mute
    private boolean isMuted = false;

    private AudioManager() {
        sounds = new HashMap<>();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    //-- 2. Metode untuk mengontrol dan memeriksa status mute
    public boolean isMuted() {
        return isMuted;
    }

    //-- KETERANGAN: Logika di dalam metode ini diperbaiki --
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (this.isMuted) {
            // Langsung hentikan musik menu secara spesifik jika di-mute
            stopSound("menu_music");
        }
    }

    public void loadSound(String name, String path) {
        try (InputStream audioSrc = getClass().getResourceAsStream(path);
             BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc)) {
            if (audioSrc == null) {
                throw new RuntimeException("Sound file not found: " + path);
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            sounds.put(name, clip);
        } catch (Exception e) {
            System.err.println("Error loading sound: " + name);
            e.printStackTrace();
        }
    }

    public void playSound(String name) {
        // Suara efek singkat tetap bisa diputar meskipun musik di-mute,
        // jika Anda ingin mematikan semua suara, tambahkan if(isMuted) return; di sini.
        Clip clip = sounds.get(name);
        if (clip != null) {
//            if (clip.isRunning()) {
//                clip.stop();
//            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void loopSound(String name) {
        //-- 3. Cek status mute sebelum memutar musik
        if (isMuted) {
            return; // Jangan putar musik jika sedang di-mute
        }
        Clip clip = sounds.get(name);
        if (clip != null && !clip.isRunning()) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopSound(String name) {
        Clip clip = sounds.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}