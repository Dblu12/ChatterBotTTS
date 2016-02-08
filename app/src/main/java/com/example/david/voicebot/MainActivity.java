package com.example.david.voicebot;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.voicebot.chatterbot.ChatterBot;
import com.example.david.voicebot.chatterbot.ChatterBotFactory;
import com.example.david.voicebot.chatterbot.ChatterBotSession;
import com.example.david.voicebot.chatterbot.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    private int CTE=1;
    private TextView tv;
    private boolean ok;
    private String listen;
    private ChatterBot bot1;
    private ChatterBotSession bot1session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv= (TextView)findViewById(R.id.textView);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE);
    }

    public void voiceBotRead(View view){

            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla");
            i.putExtra(RecognizerIntent.
                            EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    3000);
            Log.v("Prueba", "antes");
            startActivityForResult(i, 2);
            Log.v("Prueba", "entra");

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            //se puede reproducir
            ok= true;
        } else {
            //no se puede reproducir
            ok=false;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
                tts.setLanguage(Locale.getDefault());
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }else if(requestCode==2){
            Log.v("Prueba", "aqui");
            ArrayList<String> textos = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            tv.append("Tu: " + textos.get(0) + "\n"); // las siguientes son diferentes interpretaciones.
            listen= textos.get(0);
            Tarea t= new Tarea();
            t.execute(listen);
        }
    }

    class Tarea extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            ChatterBotFactory factory = new ChatterBotFactory();

            try {
                bot1 = factory.create(ChatterBotType.CLEVERBOT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bot1session = bot1.createSession();

            try {
                return bot1session.think(listen);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            tv.append("VoiceBot: " + s + "\n");
            if(ok) {
                tts.setLanguage(Locale.getDefault());
                tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
            }
            else
                Toast.makeText(MainActivity.this, "No se puede leer", Toast.LENGTH_LONG).show();
        }
    }
}
