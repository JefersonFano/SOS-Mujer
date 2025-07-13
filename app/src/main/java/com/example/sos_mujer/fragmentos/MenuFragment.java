package com.example.sos_mujer.fragmentos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Menu;
import com.example.sos_mujer.actividades.BienvenidaActivity;

public class MenuFragment extends Fragment {

    private final static int BOTONES[] = {R.id.btnPrincipal, R.id.btnMapa, R.id.btnComunidad, R.id.btnConfiguracion};

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MenuFragment() {
        // Required empty public constructor
    }

    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
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
        View vista =  inflater.inflate(R.layout.fragment_menu, container, false);
        ImageButton imgBoton;

        for (int i = 0; i < BOTONES.length; i++) {
            imgBoton = vista.findViewById(BOTONES[i]);
            final int id = i;
            imgBoton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    if (id == 0) {
                        // Si es el primer botón (botón principal), iniciar la actividad de bienvenida
                        Intent intent = new Intent(getActivity(), BienvenidaActivity.class);
                        startActivity(intent);
                    } else {
                        // De lo contrario, manejar el cambio de fragmento
                        ((Menu)activity).onClickMenu(id);
                    }
                }
            });
        }
        return vista;
    }
}
