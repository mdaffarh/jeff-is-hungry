package audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Kelas AudioManager (Singleton) untuk mengelola semua pemutaran suara dan musik dalam game.
 * Menggunakan satu instance untuk diakses dari seluruh bagian aplikasi.
 */
public class AudioManager {
    // Satu-satunya instance dari AudioManager (Singleton Pattern)
    private static AudioManager instance;
    // Penyimpanan untuk semua klip audio yang sudah dimuat, diakses dengan nama
    private final Map<String, Clip> sounds;
    // Flag untuk menyimpan status mute global
    private boolean isMuted = false;

    /**
     * Constructor privat untuk mencegah pembuatan instance dari luar kelas (Singleton).
     */
    private AudioManager() {
        sounds = new HashMap<>();
    }

    /**
     * Menyediakan akses global ke satu-satunya instance AudioManager.
     * @return Instance tunggal dari AudioManager.
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Memeriksa apakah audio sedang dalam mode mute.
     * @return true jika di-mute, false jika tidak.
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Mengatur status mute dan menghentikan musik latar jika di-mute.
     * @param muted Status mute yang baru.
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (this.isMuted) {
            // Langsung hentikan musik menu secara spesifik jika di-mute
            stopSound("menu_music");
        }
    }

    /**
     * Memuat file audio dari path dan menyimpannya ke dalam map untuk digunakan nanti.
     * @param name Nama kunci untuk mengakses suara (contoh: "button_click").
     * @param path Path ke file audio di dalam folder resources (contoh: "/audio/sound.wav").
     */
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

    /**
     * Memutar efek suara (SFX) satu kali dari awal.
     * @param name Nama kunci dari suara yang akan diputar.
     */
    public void playSound(String name) {
        // Efek suara singkat bisa tetap diputar meskipun musik di-mute.
        Clip clip = sounds.get(name);
        if (clip != null) {
            clip.setFramePosition(0); // Putar dari awal
            clip.start();
        }
    }

    /**
     * Memutar musik secara terus-menerus (looping).
     * Tidak akan memutar apapun jika status sedang mute.
     * @param name Nama kunci dari musik yang akan diputar.
     */
    public void loopSound(String name) {
        // Cek status mute sebelum memutar musik
        if (isMuted) {
            return;
        }
        Clip clip = sounds.get(name);
        if (clip != null && !clip.isRunning()) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Menghentikan klip audio yang sedang berjalan.
     * @param name Nama kunci dari suara yang akan dihentikan.
     */
    public void stopSound(String name) {
        Clip clip = sounds.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}