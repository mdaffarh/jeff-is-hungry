# Jeff is Hungry
![title](https://github.com/user-attachments/assets/c277f516-a2c7-442f-9da0-d3d3a280531e)

**Jeff is Hungry** adalah sebuah game kasual 2D di mana pemain mengendalikan karakter bernama Jeff untuk menangkap dan memakan makanan yang muncul di layar. Game ini dibuat dengan Java, menggunakan model arsitektur MVVM (Model-View-ViewModel).

## Fitur Utama
![image](https://github.com/user-attachments/assets/8a44174f-11d4-454a-a5e0-bf1b1307b09b)
![image](https://github.com/user-attachments/assets/30fad16d-dc12-4afd-8e94-4a54f068807b)

- **Gameplay Sederhana & Menyenangkan:** Kontrol Jeff untuk mengumpulkan poin dengan menangkap makanan sebanyak-banyaknya.
- **Animasi & Aset Grafis:** Menggunakan gambar dan animasi custom untuk karakter, makanan, dan latar belakang.
- **SFX & Musik:** Dukungan efek suara, musik latar, dan kontrol mute.
- **Papan Skor (Leaderboard):** Skor pemain disimpan ke database (MySQL).
- **Kontrol:** WASD atau Arrow Keys untuk bergerak.

## Cara Instalasi & Menjalankan

### Prasyarat
- Java JDK 8 atau lebih baru
- Maven (opsional, jika ingin build otomatis)
- MySQL (untuk leaderboard)

### 1. Clone Repo
```sh
git clone https://github.com/mdaffarh/jeff-is-hungry.git
cd jeff-is-hungry
```

### 2. Setup Database
- Buat database baru: `shark_game_db`
- Buat tabel `thasil`:
```sql
CREATE TABLE thasil (
  username VARCHAR(64) PRIMARY KEY,
  skor INT NOT NULL,
  count INT NOT NULL
);
```
- Ubah konfigurasi koneksi database di `src/model/DatabaseConnection.java` jika diperlukan.

### 3. Build & Jalankan
```sh
javac -d out $(find src -name "*.java")
java -cp out Main
```
Atau gunakan IDE (IntelliJ, NetBeans, dsb), import sebagai project Java, lalu run file `Main.java`.

## Struktur Direktori

```
src/
  audio/         # Manajemen suara/musik (AudioManager)
  model/         # Model data, koneksi DB, objek game (Player, Result, dll)
  view/          # Tampilan, panel utama game (GamePanel)
  viewmodel/     # Logika game dan state management
resources/
  images/        # Semua aset gambar
  audio/         # Semua aset suara
  font/          # Font custom
```

## Kontribusi

1. Fork repo ini
2. Buat branch fitur/bugfix
3. Lakukan perubahan, commit, dan push
4. Buat Pull Request

## Kredit Aset

- Background: [Cruisenho - itch.io](https://crusenho.itch.io/beriesadventureseaside)
- Food sprites:
  [GhostPixxells - itch.io](https://ghostpixxells.itch.io/pixelfood)
  [AlexKovacArt - itch.io](https://alexkovacsart.itch.io/free-pixel-art-foods)
- Fonts: [Pixelify Sans - Google Fonts](https://fonts.google.com/specimen/Pixelify+Sans)
- SFX:
  [Stormyman - itch.io](https://stormyman.itch.io/goofy-sounds-for-scary-monsters)
  [Mayragandra - itch.io](https://mayragandra.itch.io/free-footsteps-sound-effects)
- Musik: [Tallbeard - itch.io](https://tallbeard.itch.io/music-loop-bundle)
- Cursor: [Aspecsgaming - itch.io](https://aspecsgaming.itch.io/pixel-art-cursors)

## Lisensi

Proyek ini menggunakan lisensi MIT. Silakan gunakan, modifikasi, dan distribusikan dengan bebas.

---

Selamat bermain dan berkontribusi!
