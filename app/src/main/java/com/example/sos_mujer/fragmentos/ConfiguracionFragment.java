package com.example.sos_mujer.fragmentos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sos_mujer.R;
import com.example.sos_mujer.utils.LocaleHelper;

public class ConfiguracionFragment extends Fragment
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Spinner cboIdiomas;
    private CheckBox chkNotificacion;
    private TextView lblSonido, lblTamañoTexto, lblPreviewTexto;
    private SeekBar barSonido, barTamañoTexto;
    private Switch swTemaOscuro;
    private Button btnAplicar, btnRestaurar;

    private static final String PREFS = "preferencias";

    public ConfiguracionFragment() { }

    public static ConfiguracionFragment newInstance(String param1, String param2) {
        ConfiguracionFragment fragment = new ConfiguracionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_configuracion, container, false);

        cboIdiomas = vista.findViewById(R.id.confCboIdioma);
        chkNotificacion = vista.findViewById(R.id.frgCfgChkNotificaciones);
        lblSonido = vista.findViewById(R.id.frgCfgLblSonido);
        barSonido = vista.findViewById(R.id.frgCfgBarSonido);
        swTemaOscuro = vista.findViewById(R.id.frgCfgSwitchTema);
        lblTamañoTexto = vista.findViewById(R.id.frgCfgLblTamañoTexto);
        barTamañoTexto = vista.findViewById(R.id.frgCfgBarTamañoTexto);
        lblPreviewTexto = vista.findViewById(R.id.frgCfgLblPreviewTexto);
        btnAplicar = vista.findViewById(R.id.frgCfgBtnAplicar);
        btnRestaurar = vista.findViewById(R.id.frgCfgBtnRestaurar);

        btnAplicar.setOnClickListener(this);
        btnRestaurar.setOnClickListener(this);
        barSonido.setOnSeekBarChangeListener(this);
        barTamañoTexto.setOnSeekBarChangeListener(this);

        cargarPreferencias();

        return vista;
    }

    private void cargarPreferencias() {
        SharedPreferences preferences =
                requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        int idiomaIndex = preferences.getInt("idiomaIndex", 0);     // 0=es,1=qu,2=en
        boolean notificaciones = preferences.getBoolean("notificaciones", true);
        int sonido = preferences.getInt("sonido", 70);
        boolean temaOscuro = preferences.getBoolean("temaOscuro", false);
        int tamIndex = preferences.getInt("tamanoTextoIndex", 1);   // 0=pequeño,1=normal,2=grande,3=muy grande

        cboIdiomas.setSelection(idiomaIndex);
        chkNotificacion.setChecked(notificaciones);
        barSonido.setProgress(sonido);
        actualizarLabelSonido(sonido);

        swTemaOscuro.setChecked(temaOscuro);
        barTamañoTexto.setProgress(tamIndex);
        actualizarLabelTamTexto(tamIndex);
        aplicarPreviewTamTexto(tamIndex);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.frgCfgBtnAplicar) {
            aplicar();
        } else if (id == R.id.frgCfgBtnRestaurar) {
            restaurar();
        }
    }

    private void aplicar() {
        SharedPreferences preferences =
                requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int idiomaIndex = cboIdiomas.getSelectedItemPosition();
        String langCode = getLangCodeFromIndex(idiomaIndex);

        boolean notificaciones = chkNotificacion.isChecked();
        int sonido = barSonido.getProgress();
        boolean temaOscuro = swTemaOscuro.isChecked();
        int tamIndex = barTamañoTexto.getProgress();

        editor.putInt("idiomaIndex", idiomaIndex);
        editor.putString("idioma_codigo", langCode);
        editor.putBoolean("notificaciones", notificaciones);
        editor.putInt("sonido", sonido);
        editor.putBoolean("temaOscuro", temaOscuro);
        editor.putInt("tamanoTextoIndex", tamIndex);

        editor.apply();

        // Aplicar idioma
        LocaleHelper.setLanguage(requireContext(), langCode);

        // Aplicar modo oscuro
        if (temaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Tamaño de texto: lo guardamos; la app lo aplicará en actividades usando un helper
        // (te doy abajo FontScaleHelper para eso)

        Toast.makeText(getContext(), getString(R.string.lblConfiguracion) + " guardada", Toast.LENGTH_SHORT).show();

        // Recrear la Activity para que apliquen idioma/tema
        requireActivity().recreate();
    }

    private void restaurar() {
        // Valores por defecto
        cboIdiomas.setSelection(0);         // Español
        chkNotificacion.setChecked(true);
        barSonido.setProgress(70);
        swTemaOscuro.setChecked(false);
        barTamañoTexto.setProgress(1);      // Normal

        actualizarLabelSonido(70);
        actualizarLabelTamTexto(1);
        aplicarPreviewTamTexto(1);

        aplicar();  // guarda y aplica
    }

    private String getLangCodeFromIndex(int index) {
        switch (index) {
            case 1: return "qu"; // Quechua
            case 2: return "en"; // Inglés
            default: return "es"; // Español
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == barSonido) {
            actualizarLabelSonido(progress);
        } else if (seekBar == barTamañoTexto) {
            actualizarLabelTamTexto(progress);
            aplicarPreviewTamTexto(progress);
        }
    }

    private void actualizarLabelSonido(int progress) {
        String texto = getString(R.string.lblSonido).split(":")[0] + ": " + progress + "%";
        lblSonido.setText(texto);
    }

    private void actualizarLabelTamTexto(int index) {
        String tamaño;
        switch (index) {
            case 0: tamaño = "Pequeño"; break;
            case 2: tamaño = "Grande"; break;
            case 3: tamaño = "Muy grande"; break;
            default: tamaño = "Normal"; break;
        }
        lblTamañoTexto.setText(getString(R.string.lblTamañoTexto) + " (" + tamaño + ")");
    }

    private void aplicarPreviewTamTexto(int index) {
        float sizeSp;
        switch (index) {
            case 0: sizeSp = 12f; break;
            case 2: sizeSp = 20f; break;
            case 3: sizeSp = 24f; break;
            default: sizeSp = 16f; break;
        }
        lblPreviewTexto.setTextSize(sizeSp);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }
}
