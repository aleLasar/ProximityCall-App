package com.example.project;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.airbnb.lottie.LottieAnimationView;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicWindow extends AppCompatActivity implements View.OnClickListener{

    LottieAnimationView recordBtn;
    AtomicBoolean recording = new AtomicBoolean(false);
    Socket socket = SocketHandler.getSocket();;
    private Recorder rec;
    Thread t;
    LottieAnimationView recorder_animation;

    private class CheckSocket extends AsyncTask<Socket, Void, Void> {

        @Override
        protected Void doInBackground(Socket... sockets) {
            Socket s = sockets[0];
            while (true){
                if(s.isClosed() == true)
                    MicWindow.this.finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MicWindow.CheckSocket ck = (CheckSocket) new CheckSocket().execute(socket);

        setContentView(R.layout.activity_mic_window);
        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(this);
        startService(new Intent(getApplicationContext(), Player.class));
        recorder_animation = findViewById(R.id.recorder_animation);
        recorder_animation.playAnimation();
        recorder_animation.setMaxFrame(98);
        recorder_animation.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if( animation.getAnimatedFraction() == 1)
                    recorder_animation.pauseAnimation();
            }
        });
    }




    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.record_btn) {
            if (recording.get() == false) {
                // stream audio
                recordBtn.playAnimation();
                recorder_animation.setMinAndMaxFrame(105,340);
                recorder_animation.removeAllUpdateListeners();
                recorder_animation.playAnimation();
                recording.set(true);
                rec = new Recorder();
                t = new Thread(rec);
                if (rec != null) {
                    Recorder.keepRecording = true;
                }
                t.start();
            } else {
                recording.set(false);
                recorder_animation.setMinAndMaxFrame(100,101);
                recorder_animation.playAnimation();
                recordBtn.pauseAnimation();
                if (rec != null) {
                    Recorder.keepRecording = false;
                }
            }
        }
        /*try{
            OutputStream s1out = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(s1out));
            bw.write("Ciao Bello");
            Toast.makeText(getApplicationContext(), "Ho scritto 'Ciao Bello'", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        }*/
    }

    @Override
    protected void onDestroy() {
        if(socket.isClosed() == false) {
            try {
                socket.close();
            } catch (Exception e) {}
        }

        if(rec != null) {
            Recorder.keepRecording = false;
        }

    }


}