package ua.improveit.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btn;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_gg).setOnClickListener(v -> {
                    Toast.makeText(this, "asdfdsf ", Toast.LENGTH_SHORT).show();
                }
        );
        findViewById(R.id.button).setOnClickListener(v -> {
            ((ua.improveit.topnotificator.NotificationView) findViewById(R.id.notificator)).showMessage("asdf asdfkjnn jsdf" + count);
            count++;
        });
    }
}