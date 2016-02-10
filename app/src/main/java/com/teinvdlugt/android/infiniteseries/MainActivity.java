package com.teinvdlugt.android.infiniteseries;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final int UPDATE_DELAY_MIN_VALUE = 749; // Shouldn't be zero

    private FloatingActionButton fab;
    private TextView resultTV, nTextView, updateDelayTV;
    private PiTask piTask;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        updateDelayTV = (TextView) findViewById(R.id.update_delay_textView);
        resultTV = (TextView) findViewById(R.id.result_textView);
        nTextView = (TextView) findViewById(R.id.n_textView);
        fab = (FloatingActionButton) findViewById(R.id.start_fab);
        updateDelayTV.setText(getString(R.string.update_delay_format, (seekBar.getProgress() + UPDATE_DELAY_MIN_VALUE) * 2));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFab();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newUpdateDelay = (progress + UPDATE_DELAY_MIN_VALUE) * 2;
                if (piTask != null)
                    piTask.updateDelay = newUpdateDelay;
                updateDelayTV.setText(getString(R.string.update_delay_format, newUpdateDelay));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void onClickFab() {
        if (piTask != null && piTask.getStatus() == AsyncTask.Status.RUNNING) {
            piTask.cancel(true);
            fab.setImageDrawable(getDrawableCompat(android.R.drawable.ic_media_play));
        } else {
            piTask = new PiTask();
            piTask.execute();
            fab.setImageDrawable(getDrawableCompat(android.R.drawable.ic_media_pause));
        }
    }

    private class PiTask extends AsyncTask<Void, Double, Void> {
        private int updateDelay = UPDATE_DELAY_MIN_VALUE;

        @Override
        protected void onPreExecute() {
            updateDelay = (seekBar.getProgress() + UPDATE_DELAY_MIN_VALUE) * 2;
        }

        @Override
        protected Void doInBackground(Void... params) {
            int n = 1;
            double pi = 0;

            while (!isCancelled()) {
                if ((n + 1) % 4 == 0) {
                    pi -= 4. / n;
                } else {
                    pi += 4. / n;
                }

                if ((n + 1) % updateDelay == 0) {
                    publishProgress(pi, (double) n);
                }

                n += 2;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            if (!isCancelled()) {
                resultTV.setText(Double.toString(values[0]));
                nTextView.setText(getString(R.string.n_textView_format, values[1].intValue()));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (piTask != null) piTask.cancel(true);
        fab.setImageDrawable(getDrawableCompat(android.R.drawable.ic_media_play));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_learnMore) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Leibniz_formula_for_%CF%80"));
            startActivity(intent);
            return true;
        }
        return false;
    }

    private Drawable getDrawableCompat(@DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= 21) {
            return getDrawable(drawableId);
        } else {
            return getResources().getDrawable(drawableId);
        }
    }
}
