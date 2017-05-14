package me.soundthing.soundthing;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

  Thread t;
  int sr = 44100;
  double freq = 440.f;
  boolean isRunning = true;

  long millis = System.currentTimeMillis();

  private SensorManager mSensorManager;
  private Sensor mSensor;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        isRunning = !isRunning;
        makeSomeSound();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void makeSomeSound() {
    System.out.println("FUCK YOU?");
    // start a new thread to synthesise audio
    t = new Thread() {
      public void run() {
        // set process priority
        setPriority(Thread.MAX_PRIORITY);
        // set the buffer size
        int buffsize = AudioTrack.getMinBufferSize(sr,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // create an audiotrack object
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize,
                AudioTrack.MODE_STREAM);

        short samples[] = new short[buffsize];
        int amp = 10000;
        double twopi = 8. * Math.atan(1.);
        double ph = 0.0;

        // start audio
        audioTrack.play();

        // synthesis loop
        while (isRunning) {
          for (int i = 0; i < buffsize; i++) {
            samples[i] = (short) (amp * Math.sin(ph));
            ph += twopi * freq / sr;
          }
          audioTrack.write(samples, 0, buffsize);
        }
        audioTrack.stop();
        audioTrack.release();
      }
    };
    t.start();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    float x = Math.abs(event.values[0]);
    freq = 800 + x * 100;
    System.out.println(System.currentTimeMillis() - millis);
    millis = System.currentTimeMillis();
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }
}
