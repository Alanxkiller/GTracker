package com.example.gasolinerafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth auth;
    Button button, buttonPrecios;
    TextView textView;
    FirebaseUser user;
    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap mMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private String nombre, correo;
    private Button precios, logout;
    private boolean isadmin = false;
    private String coloradmin1hex = "#947703";
    ConnectionClass connectionClass;
    Connection con;
    String name, str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionClass = new ConnectionClass();
        connect();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        button = findViewById(R.id.logout);
        buttonPrecios = findViewById(R.id.precios);
        textView = findViewById(R.id.user_details);


        Button precios = findViewById(R.id.precios);
        Button logout = findViewById(R.id.logout);

        // Convertir el color hexadecimal a un entero
        int coloradmin1 = Color.parseColor(coloradmin1hex);


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            correo = user.getEmail();
            if (correo.equals("admingt@gmail.com")) {
                textView.setText("ADMINISTRADOR: " + correo);
                isadmin = true;
                precios.setBackgroundColor(coloradmin1);
                precios.setText("Agregar/eliminar gasolinera");
                logout.setBackgroundColor(coloradmin1);

            }
            else {
                //System.out.println(correo);

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() ->{
                    try {
                        con = connectionClass.CONN();
                        String query = "SELECT Nombre FROM usuarios WHERE Correo LIKE '"+correo+"'";
                        System.out.println("Query" + query);
                        PreparedStatement stmt = con.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();
                        StringBuilder nombreStrB = new StringBuilder("");
                        while (rs.next())
                        {
                            nombreStrB.append(rs.getString("Nombre"));
                        }
                        nombre = nombreStrB.toString();

                        System.out.println("Nombre: "+nombre);

                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                    }

                    runOnUiThread(() -> {
                        try {
                            Thread.sleep(500);
                            textView.setText("Cuenta: " + nombre);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                });



            }


        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonPrecios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FirestoreActivity.class);
                startActivity(intent);
                //finish();
            }
        });

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 17.5f;
            LatLng posIni = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            
            //googleMap.addMarker(new MarkerOptions().position(posIni).title("Mi ubicación"));
            //googleMap.addMarker(new MarkerOptions().position(gasolinera2).title(""));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posIni, zoomLevel));

            // Establecer la ubicación actual en el LocationManager
            LocationManager.getInstance().setCurrentLocation(posIni);

            obtenerYAgregarMarcadoresDesdeFirestore();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else {
                Toast.makeText(this, "El permiso para la ubicacion fue denegado, por favor da el acceso", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerYAgregarMarcadoresDesdeFirestore() {
        // Realizar una consulta a Firestore para obtener datos
        db.collection("Notebook")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Iterar sobre los documentos y agregar marcadores
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tituloDocumento = document.getString("title");
                            String precioDocumento = document.getString("description");

                            // Dividir el texto original en partes usando "/"
                            String[] partes = precioDocumento.split("/");

                            // Variables para almacenar los precios como cadenas
                            String precioMagna = "";
                            String precioPremium = "";
                            String precioDiesel = "";

                            for (String parte : partes) {
                                // Dividir cada parte en palabras usando "-"
                                String[] palabras = parte.trim().split("-");

                                if (palabras.length == 2) {
                                    // Obtener la primera letra de la primera palabra
                                    char letra = palabras[0].trim().charAt(0);

                                    // Obtener el precio como cadena
                                    String precio = palabras[1].trim();

                                    // Asignar el precio a la variable correspondiente
                                    if (letra == 'M') {
                                        precioMagna = precio;
                                    } else if (letra == 'P') {
                                        precioPremium = precio;
                                    } else if (letra == 'D') {
                                        precioDiesel = precio;
                                    }
                                }
                            }

                            // Obtener las coordenadas del documento
                            Double latitud = document.getDouble("lat");
                            Double longitud = document.getDouble("lng");

                            // Verificar si las coordenadas son válidas
                            if (latitud != null && longitud != null) {
                                // Crear un objeto LatLng
                                LatLng ubicacion = new LatLng(latitud, longitud);

                                // Agregar un marcador al mapa
                                mMap.addMarker(new MarkerOptions().position(ubicacion).title("M-" + precioMagna + " || P-" + precioPremium + " || D-" + precioDiesel));

                                // Opcional: Mover la cámara hacia el primer marcador
                                //mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));
                            }
                        }
                    } else {
                        // Manejar el fallo de la consulta
                    }
                });
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public void connect(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->{
            try {
                con = connectionClass.CONN();
                if (con == null){
                    str = "Error in connection with MySQL server";
                } else {
                    str = "Connected with MySQL server";
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }
}