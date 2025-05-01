package com.example.sos_mujer.fragmentos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sos_mujer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfiguracionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfiguracionFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    Spinner cboIdiomas;
    CheckBox chkNotificacion;
    TextView lblSonido;
    SeekBar barSonido;
    Button btnAplicar, btnRestaurar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConfiguracionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConfiguracionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfiguracionFragment newInstance(String param1, String param2) {
        ConfiguracionFragment fragment = new ConfiguracionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_configuracion, container, false);
        cboIdiomas = vista.findViewById(R.id.confCboIdioma);
        chkNotificacion = vista.findViewById(R.id.frgCfgChkNotificaciones);
        lblSonido = vista.findViewById(R.id.frgCfgLblSonido);
        barSonido = vista.findViewById(R.id.frgCfgBarSonido);
        btnAplicar = vista.findViewById(R.id.frgCfgBtnAplicar);
        btnRestaurar = vista.findViewById(R.id.frgCfgBtnRestaurar);

        btnAplicar.setOnClickListener(this);
        btnRestaurar.setOnClickListener(this);
        barSonido.setOnSeekBarChangeListener(this);
        cargarPreferencias();

        return vista;
    }

    private void cargarPreferencias() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        int idioma = preferences.getInt("idioma",0);
        boolean notificaciones = preferences.getBoolean("notificaciones",false);
        int sonido = preferences.getInt("sonido",100);

        cboIdiomas.setSelection(idioma);
        chkNotificacion.setChecked(notificaciones);
        barSonido.setProgress(sonido);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.frgCfgBtnAplicar)
            aplicar();
        else if (v.getId() == R.id.frgCfgBtnRestaurar)
            restaurar();
    }

    private void aplicar() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();   //objeto para guardar datos
        editor.putInt("idioma",cboIdiomas.getSelectedItemPosition()); //capturar idioma
        editor.putBoolean("notificaciones",chkNotificacion.isChecked());//capturar check
        editor.putInt("sonido",barSonido.getProgress());
        editor.apply(); //escribir en el archivo
        Toast.makeText(getContext(),"Preferencias guardadas",Toast.LENGTH_SHORT).show(); //cargar datos
    }

    private void restaurar() {
        cboIdiomas.setSelection(0);
        chkNotificacion.setChecked(true);
        barSonido.setProgress(100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar == barSonido){
            String volumen = "Volumen: ";
            switch (progress){
                case 0: volumen += "mínimo";break;
                case 100: volumen += "máximo";break;
                default: volumen += String.valueOf(progress);break;
            }
            lblSonido.setText(volumen);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}