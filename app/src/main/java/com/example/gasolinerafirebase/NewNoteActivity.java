package com.example.gasolinerafirebase;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewNoteActivity extends AppCompatActivity {

    /*
    private static final String[] GAS_STATION_NAMES = {
            "Pemex", "BP", "Shell", "Repsol", "Gulf", "Total", "ExxonMobil", "Chevron", "Texaco", "Oxxo Gas"
    };

    private static final double MIN_LAT = 21.8951000;
    private static final double MAX_LAT = 22.0000000;
    private static final double MIN_LNG = -102.3990000;
    private static final double MAX_LNG = -102.2900000;

    private static final double MIN_MAGNA_PRICE = 19.00;
    private static final double MAX_MAGNA_PRICE = 21.00;
    private static final double MIN_PREMIUM_PRICE = 20.50;
    private static final double MAX_PREMIUM_PRICE = 23.00;
    private static final double MIN_DIESEL_PRICE = 19.50;
    private static final double MAX_DIESEL_PRICE = 22.00;

     */

    private EditText editTextTitle;
    private EditText editTextDescription, editTextDescription2, editTextDescription3, editTextComentario;
    private NumberPicker numberPickerPriority;
    private MediaPlayer confirm2;
    public boolean crear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notebookRef = db.collection("Notebook");
        Random random = new Random();

        /*for (int i = 0; i < 20; i++) {
            String title = GAS_STATION_NAMES[random.nextInt(GAS_STATION_NAMES.length)];

            double magnaPrice = MIN_MAGNA_PRICE + (MAX_MAGNA_PRICE - MIN_MAGNA_PRICE) * random.nextDouble();
            double premiumPrice = MIN_PREMIUM_PRICE + (MAX_PREMIUM_PRICE - MIN_PREMIUM_PRICE) * random.nextDouble();
            double dieselPrice = MIN_DIESEL_PRICE + (MAX_DIESEL_PRICE - MIN_DIESEL_PRICE) * random.nextDouble();

            String description = String.format("MAGNA - $%.2f / PREMIUM - $%.2f / DIESEL - $%.2f", magnaPrice, premiumPrice, dieselPrice);

            double lat = MIN_LAT + (MAX_LAT - MIN_LAT) * random.nextDouble();
            double lng = MIN_LNG + (MAX_LNG - MIN_LNG) * random.nextDouble();

            Map<String, Object> note = new HashMap<>();
            note.put("title", title);
            note.put("description", description);
            note.put("priority", random.nextInt(10) + 1); // Assuming priority is between 1 and 5
            note.put("lat", lat);
            note.put("lng", lng);

            notebookRef.add(note)
                    .addOnSuccessListener(documentReference -> {
                        System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error adding document: " + e.getMessage());
                    });
        }

     */

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Añadir gasolinera");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDescription2 = findViewById(R.id.edit_text_description2);
        editTextDescription3 = findViewById(R.id.edit_text_description3);
        editTextComentario = findViewById(R.id.edit_text_comentario);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

        editTextDescription.setText("$");
        editTextDescription2.setText("$");
        editTextDescription3.setText("$");

        editTextComentario.setVisibility(GONE);


        // Inicializa el MediaPlayer con la música de fondo
        confirm2 = MediaPlayer.create(this, R.raw.confirm2);
        confirm2.setVolume(0.5f, 0.5f); // Volumen (0.0f - 1.0f)

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_note) {
                saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Guardarás tu ubicación actual para el punto de la gasolinera. \n ¿Estás seguro de dar de alta este punto?")
                .setTitle("Confirmar acción");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                crear = true;
                if (crear) {
                    String title = editTextTitle.getText().toString();
                    String description = editTextDescription.getText().toString();
                    String description2 = editTextDescription2.getText().toString();
                    String description3 = editTextDescription3.getText().toString();
                    description = "MAGNA - $" + description + " / PREMIUM - $" + description2 + " / DIESEL - $" + description3;
                    int priority = numberPickerPriority.getValue();

                    if (title.trim().isEmpty() || description.trim().isEmpty()) {
                        Toast.makeText(NewNoteActivity.this, "Por favor inserta el nombre y los precios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = LocationManager.getInstance().getCurrentLatitude();
                    double lng = LocationManager.getInstance().getCurrentLongitude();

                    CollectionReference notebookRef = FirebaseFirestore.getInstance()
                            .collection("Notebook");

                    Map<String, Object> note = new HashMap<>();
                    note.put("title", title);
                    note.put("description", description);
                    note.put("priority", priority);
                    note.put("lat", lat);
                    note.put("lng", lng);

                    notebookRef.add(note)
                            .addOnSuccessListener(documentReference -> {
                                // El documento se agregó exitosamente y documentReference contiene el ID del nuevo documento
                                Toast.makeText(NewNoteActivity.this, "Datos modificados exitosamente", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Maneja el caso de fallo al agregar el documento
                                Toast.makeText(NewNoteActivity.this, "Hubo un problema! No se pudo dar de alta! :c", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    confirm2.start();
                    crear = false;
                    Toast.makeText(NewNoteActivity.this, "Dato agregado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    finish();
                }
                dialog.dismiss();  // Cierra el diálogo
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                crear = false;
                finish();
                dialog.dismiss();  // Cierra el diálogo
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

}