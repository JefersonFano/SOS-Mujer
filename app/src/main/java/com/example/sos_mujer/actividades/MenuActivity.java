package com.example.sos_mujer.actividades;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Menu;
import com.example.sos_mujer.fragmentos.ComunidadFragment;
import com.example.sos_mujer.fragmentos.ConfiguracionFragment;
import com.example.sos_mujer.fragmentos.MapaFragment;
import com.example.sos_mujer.fragmentos.MenuFragment;
import com.example.sos_mujer.utils.LanguageHelper;

public class MenuActivity extends AppCompatActivity implements Menu {

    Fragment[] fragments;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fragments = new Fragment[4];
        fragments[0] = new MenuFragment();
        fragments[1] = new MapaFragment();
        fragments[2] = new ComunidadFragment();
        fragments[3] = new ConfiguracionFragment();

        int id = getIntent().getIntExtra("id", -1);
        onClickMenu(id);
    }

    @Override
    public void onClickMenu(int id) {
        FragmentManager fm =getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menRelContenedor, fragments[id]);
        ft.commit();
    }
}