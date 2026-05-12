package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            configurarImmersiveMode();
        }
    }

    private void configurarImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}