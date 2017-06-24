package dfp.pgnconverter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DFP";
    final int REQUEST_WRITE_ACCESS_CUSTOM = 1;
    final int REQUEST_WRITE_ACCESS_DROIDFISH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button droidfish = findViewById(R.id.droidfish);
        droidfish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePGN(getString(R.string.save_location_droidfish), REQUEST_WRITE_ACCESS_DROIDFISH);
            }
        });

        Button custom = findViewById(R.id.custom);
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                sharedPreferences.getString(getString(R.string.default_save_location_key), "");
                savePGN(getString(R.string.save_location_documents), REQUEST_WRITE_ACCESS_CUSTOM);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.share:
                //sharePGN();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createPGN(String path, String fileName, String pgnText) {
        FileOutputStream stream;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File extDir = Environment.getExternalStoragePublicDirectory(path);
            if (!extDir.mkdirs()) {
                Log.e(TAG, "Failed to make directory");
            }

            File file = new File(
                    extDir,
                    fileName.contains(".pgn") ? fileName : fileName + ".pgn");

            try {
                if (!file.exists() && !file.createNewFile()) {
                    Log.e(TAG, "Failed to create file");
                }
                stream = new FileOutputStream(file);
                stream.write(pgnText.getBytes());
                stream.close();
                Toast.makeText(this, String.format("%s sent to %s", file.getName(), path),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
            }
            ((EditText) findViewById(R.id.editText)).setText("");
        } else {
            Toast.makeText(this, "External media is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void savePGN(final String path, int permissionRequest) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    permissionRequest);
        } else {
            final EditText directoryText = new EditText(MainActivity.this);
            directoryText.setHint("File name");

            new AlertDialog.Builder(MainActivity.this)
                    .setView(directoryText)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createPGN(path, directoryText.getText().toString(),
                                    ((EditText) findViewById(R.id.editText)).getText().toString());
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //TODO more tightly couple the request codes with the save paths
        switch (requestCode) {
            case REQUEST_WRITE_ACCESS_CUSTOM:
            case REQUEST_WRITE_ACCESS_DROIDFISH:
                if (grantResults.length == 0 || grantResults[0]
                        == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Write permission required to save PGN files",
                            Toast.LENGTH_LONG).show();
                } else if (requestCode == REQUEST_WRITE_ACCESS_CUSTOM) {
                    savePGN("Documents/PGNs", REQUEST_WRITE_ACCESS_CUSTOM);
                } else {
                    savePGN("DroidFish/pgn", REQUEST_WRITE_ACCESS_DROIDFISH);
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}