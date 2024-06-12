package com.example.gasolinerafirebase;

import static android.media.MediaFormat.KEY_PRIORITY;
import static androidx.browser.customtabs.CustomTabsIntent.KEY_DESCRIPTION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateNoteActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDescription, editTextDescription2, editTextDescription3, editTextComentario;
    private NumberPicker numberPickerPriority;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private DocumentReference noteRef = db.document("Notebook/Hola");
    private String docidsaved;
    private MediaPlayer confirm1;
    private String correo;
    private boolean isadmin = false;

    FirebaseAuth auth;
    FirebaseUser user;
    ConnectionClass connectionClass;
    Connection con;
    String name, str, idUsuario, idGasolinera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        connectionClass = new ConnectionClass();
        connect();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            correo = user.getEmail();
            if (correo.equals("admingt@gmail.com")) {
                isadmin = true;
            }
            else {
                //System.out.println(correo);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() ->{
                    try {
                        con = connectionClass.CONN();
                        String query = "SELECT IDUsuario FROM usuarios WHERE Correo LIKE '"+correo+"'";
                        System.out.println("Query " + query);
                        PreparedStatement stmt = con.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();
                        StringBuilder nombreStrB = new StringBuilder("");
                        while (rs.next())
                        {
                            nombreStrB.append(rs.getString("IDUsuario"));
                        }
                        idUsuario = nombreStrB.toString();

                        System.out.println("IDUsuario: "+idUsuario+"\nCorreo: "+correo);

                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                    }

                    /*runOnUiThread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });*/
                });



            }


        }

        Intent intent = getIntent();
        docidsaved = intent.getStringExtra("docid");
        System.out.println("Hola"+docidsaved);
        noteRef = db.document("Notebook/"+docidsaved);

        // Inicializa el MediaPlayer con la música de fondo
        confirm1 = MediaPlayer.create(this, R.raw.confirm1);
        confirm1.setVolume(0.5f, 0.5f); // Volumen (0.0f - 1.0f)


        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Modificar datos");

        Toast.makeText(this, "Cambiando para: " + docidsaved, Toast.LENGTH_SHORT).show();

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDescription2 = findViewById(R.id.edit_text_description2);
        editTextDescription3 = findViewById(R.id.edit_text_description3);
        editTextComentario = findViewById(R.id.edit_text_comentario);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

        editTextDescription.setHint("Obteniendo precio...");
        editTextDescription2.setHint("Obteniendo precio...");
        editTextDescription3.setHint("Obteniendo precio...");


        noteRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // El documento existe, puedes obtener el o los datos
                            String titulo = documentSnapshot.getString("title");
                            editTextTitle.setEnabled(false);

                            idGasolinera = documentSnapshot.getId();

                            String precioDocumento = documentSnapshot.getString("description");

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

                            // Ahora usamos los datos para introducirlos a los views
                            editTextTitle.setText(titulo);
                            editTextDescription.setText(precioMagna);
                            editTextDescription2.setText(precioPremium);
                            editTextDescription3.setText(precioDiesel);
                        } else {
                            // El documento no existe
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejar el error en caso de fallo al obtener los datos del documento
                    }
                });

        //editTextTitle.setText();
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
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String description2 = editTextDescription2.getText().toString();
        String description3 = editTextDescription3.getText().toString();
        description = "MAGNA - " + description + " / PREMIUM - " + description2 + " / DIESEL - " + description3;
        int priority = numberPickerPriority.getValue();

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(this, "Por favor inserta el nombre y los precios", Toast.LENGTH_SHORT).show();
            return;
        }

        //SQL
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionClass.CONN();
                String query = "INSERT INTO comentarios (IDGasolinera, Comentario, Rating, IDUsuario) VALUES ('"+idGasolinera+"','"+editTextComentario.getText()+"','"+priority+"', '"+idUsuario+"');";
                PreparedStatement stmt = con.prepareStatement(query);
                int queryUpdates = stmt.executeUpdate();

            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        });

        /*CollectionReference notebookRef = FirebaseFirestore.getInstance()
                .collection("Notebook");
        notebookRef.add(new Note(title, description, priority));*/
        Map<String, Object> note = new HashMap<>();
        //note.put("title", description);
        note.put("description", description);
        note.put("priority", priority);
        //noteRef.set(note, SetOptions.merge());
        noteRef.update(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La actualización fue exitosa
                        confirm1.start();
                        Toast.makeText(UpdateNoteActivity.this, "Datos modificados exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejar error en caso de fallo en la actualización
                        Toast.makeText(UpdateNoteActivity.this, "Hubo un problema! No se pudieron modificar los datos :c", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

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