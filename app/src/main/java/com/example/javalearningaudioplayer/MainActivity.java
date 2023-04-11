package com.example.javalearningaudioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
    // создание полей
    private final String DATA_STREAM = "http://ep128.hostingradio.ru:8030/ep128"; // ссылка на аудио поток
    //private final String DATA_SD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/music.mp3"; // патч на аудио-файл с SD-карты
    private final String DATA_SD = ""; // патч на аудио-файл с SD-карты
    private String nameAudio = ""; // название контента

    private MediaPlayer mediaPlayer; // создание поля медиа-плеера
    private AudioManager audioManager; // создание поля аудио-менеджера
    private Toast toast; // создание поля тоста

    private TextView textOut; // поле вывода информации об аудио-файле
    private Switch switchLoop; // поле переключателя повтора воспроизведения

    private final String LOG_TAG_ERROR = "Ошибка медиа плеера:";
    private final String LOG_TAG = "Медиа плеер:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присваеваем полям соответствующие ID из activity_main
        textOut = findViewById(R.id.textOut);
        switchLoop = findViewById(R.id.switchLoop);

        // получение доступа к аудио-менеджеру
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // создание слушателя переключателя повтора
        switchLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mediaPlayer != null)
                    mediaPlayer.setLooping(isChecked);
                setTextOut();
            }
        });

    }

    // onCompletion() - метод слушателя OnCompletionListener (вызывается, когда достигнут конец проигрываемого содержимого)
    // метод закрытия дополнительного потока
    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(this, R.string.text_toast_distroy_mediaplayer, Toast.LENGTH_SHORT); // инициализация тоста и показ

    }

    // onPrepared - метод слушателя OnPreparedListener (вызывается, когда плеер готов к проигрыванию)
    // метод подготовки дополнительного потока
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start(); // старт медиа-плейера
        Log.i(LOG_TAG, "старт медиа-плейера");

        Toast.makeText(this, R.string.text_start_mediaplayer, Toast.LENGTH_SHORT).show(); // инициализация тоста и показ
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer(); // метод освобождения используемых проигрывателем ресурсов
    }

    // метод освобождения используемых проигрывателем ресурсов
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Событие на нажатие кнопок управления
    public void onClickButton(View view) {

        if (mediaPlayer == null) return;

        switch (view.getId()) {
            case R.id.btnResume:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start(); // метод возобновления проигрывания
                }
                break;
            case R.id.btnPause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // метод паузы
                }
                break;
            case R.id.btnStop:
                mediaPlayer.stop(); // метод остановки
                break;
            case R.id.btnForward:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000); // переход к определённой позиции трека
                // mediaPlayer.getCurrentPosition() - метод получения текущей позиции
                break;
            case R.id.btnBack:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000); // переход к определённой позиции трека
                break;
        }

        setTextOut();
    }

    //Вывод текста информации
    public void setTextOut(){
        // информативный вывод информации
        textOut.setText(nameAudio + "\n(проигрывание " + mediaPlayer.isPlaying() + ", время " + mediaPlayer.getCurrentPosition()
                + ",\nповтор " + mediaPlayer.isLooping() + ", громкость " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + ")");

    }

    //Событие на нажатие кнопок ресурсов
    public void onClickSource(View view) {
        releaseMediaPlayer(); // метод освобождения используемых проигрывателем ресурсов

        mediaPlayer = new MediaPlayer(); // создание объекта медиа-плеера

        // обработка нажатия кнопок
        try {

            switch (view.getId()) {
                case R.id.btnStream:
                    // код выполнения кнопки btnStream
                    // размещаем тост (контекст, сообщение, длительность сообщения)
                    Toast.makeText(getApplicationContext(), "Запущен поток аудио", Toast.LENGTH_SHORT).show(); // инициализация и плказ тоста

//                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // задает аудио-поток, который будет использован для проигрывания
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );

                    mediaPlayer.setDataSource(DATA_STREAM); // указание источника аудио

                    mediaPlayer.setOnPreparedListener(this); // ассинхронная подготовка плеера к проигрыванию
                    mediaPlayer.prepareAsync(); // ассинхронная подготовка плейера к проигрыванию
                    nameAudio = "РАДИО"; // инициализация названия контента
                    break;
                case R.id.btnRAW:
                    // код выполнения кнопки btnRAW
                    Toast.makeText(getApplicationContext(), "Запущен аудио-файл с памяти телефона", Toast.LENGTH_SHORT).show(); // инициализация и плказ тоста

                    mediaPlayer = MediaPlayer.create(this, R.raw.flight_of_the_bumblebee); // создание дорожки с имеющимся аудио-файлом

                    mediaPlayer.start(); // старт данной дорожки
                    nameAudio = "Н.А.Римский-Корсаков - Полёт шмеля"; // инициализация названия контента
                    break;
                case R.id.btnSD:
                    // код выполнения кнопки btnSD
                    Toast.makeText(getApplicationContext(), "Запущен аудио-файл с SD-карты", Toast.LENGTH_SHORT).show(); // инициализация и плказ тоста

                    mediaPlayer.setDataSource(DATA_SD); // указание источника аудио

                    //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // подключение аудио-менеджера
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );

                    mediaPlayer.prepare(); // ассинхронная подготовка плеера к проигрыванию
                    mediaPlayer.start(); // ассинхронная подготовка плеера к проигрыванию
                    break;
            }
            setTextOut();

        } catch (IOException e) { // исключение ввода / вывода
            e.printStackTrace();
            Log.i(LOG_TAG_ERROR, e.getMessage());

            Toast.makeText(this, "Источник информации не найден", Toast.LENGTH_SHORT).show(); // инициализация и плказ тоста
        }

        if (mediaPlayer == null) return;

        mediaPlayer.setLooping(switchLoop.isChecked()); // включение / выключение повтора
        mediaPlayer.setOnCompletionListener(this); // слушатель окончания проигрывания

    }

    //Регулировка громкости
    public void onClickVolume(View view) {
        switch (view.getId()) {
            case R.id.btnUp:
                // ADJUST_RAISE = Raise the volume, FLAG_PLAY_SOUND = make a sound when clicked
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                break;
            case R.id.btnDown:
                // ADJUST_LOWER = Lowers the volume, FLAG_PLAY_SOUND = make a sound when clicked
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                break;
        }

        setTextOut();
    }
}