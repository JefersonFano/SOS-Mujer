package com.example.sos_mujer.fragmentos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sos_mujer.R;
import com.example.sos_mujer.adaptadores.ReporteAdapter;
import com.example.sos_mujer.clases.Reporte;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComunidadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComunidadFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public final static String urlMostrarReportes = "http://sos-mujer.atwebpages.com/ws/mostrarReportes.php";
    private RecyclerView recReportes;
    private ReporteAdapter adapter;
    public static ArrayList<Reporte> lista;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ComunidadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ComunidadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ComunidadFragment newInstance(String param1, String param2) {
        ComunidadFragment fragment = new ComunidadFragment();
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
    public void onResume() {
        super.onResume();
        //mostrar reportes

        AsyncHttpClient ahcMostrarReportes = new AsyncHttpClient();

        ahcMostrarReportes.get(urlMostrarReportes, null, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if (statusCode == 200){
                    try {
                        JSONArray jsonArray = new JSONArray(rawJsonResponse);
                        lista.clear();
                        for (int i = 0; i < jsonArray.length(); i++){
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            Date fecha = format.parse(jsonArray.getJSONObject(i).getString("fecha"));
                            lista.add(new Reporte(jsonArray.getJSONObject(i).getInt("id"),
                                    jsonArray.getJSONObject(i).getString("foto"),
                                    jsonArray.getJSONObject(i).getString("tipo"),
                                    fecha,jsonArray.getJSONObject(i).getString("lugar"),
                                    jsonArray.getJSONObject(i).getString("descripcion")));
                            adapter.notifyDataSetChanged();
                        }
                    }catch (JSONException | ParseException e){
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {

            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_comunidad, container, false);
        recReportes = vista.findViewById(R.id.frgRepRecReportes);
        lista = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recReportes.setLayoutManager(manager);
        adapter = new ReporteAdapter(lista);
        recReportes.setAdapter(adapter);

        return vista;
    }
}