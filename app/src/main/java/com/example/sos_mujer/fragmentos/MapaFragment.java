package com.example.sos_mujer.fragmentos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sos_mujer.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends Fragment {

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }else {
                googleMap.setMyLocationEnabled(true);
            }

            LatLng upnSJL = new LatLng(-11.9849634,-77.0075525);
            Marker marcadorUPNSJL = googleMap.addMarker(new MarkerOptions().position(upnSJL).title("UPN SJL: 530 reportes"));
            marcadorUPNSJL.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.advertenciasosmujer));
            LatLng estSCarlos = new LatLng(-11.985109,-77.0117623);
            Marker marcadorSanCarlos = googleMap.addMarker(new MarkerOptions().position(estSCarlos).title("Estación San Carlos: 350 reportes"));
            marcadorSanCarlos.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.advertenciasosmujer));
            LatLng utpSJL = new LatLng(-11.983581,-77.011833);
            Marker marcadorUTPSJL = googleMap.addMarker(new MarkerOptions().position(utpSJL).title("Ultimo acontecimiento"));
            marcadorUTPSJL.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.alertasosmujer));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(upnSJL,18));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}