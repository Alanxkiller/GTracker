package com.example.gasolinerafirebase;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class FirestoreActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private NoteAdapter adapter;
    private boolean borrar = false;
    private boolean isadmin = false;
    private String correo;
    FirebaseAuth auth;
    FirebaseUser user;
    private String coloradmin1hex = "#947703";
    private MediaPlayer confirm1;
    private MediaPlayer confirm2;
    private MediaPlayer swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore);
        setTitle("Precios de gasolina");

        // Convertir el color hexadecimal a un entero
        int coloradmin1 = Color.parseColor(coloradmin1hex);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        correo = user.getEmail();
        if (correo.equals("admingt@gmail.com")) {
            isadmin = true;
        }
        else {

        }

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);

            buttonAddNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FirestoreActivity.this, NewNoteActivity.class));
                }
            });


        if (!isadmin){
        buttonAddNote.setVisibility(GONE);
        }

        setUpRecyclerView();

    }

    private void setUpRecyclerView(){
        Query query = notebookRef.orderBy("priority", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().clear();

        // Inicializa el MediaPlayer con la música de fondo
        confirm1 = MediaPlayer.create(this, R.raw.confirm1);
        confirm1.setVolume(0.5f, 0.5f); // Volumen (0.0f - 1.0f)

        // Inicializa el MediaPlayer con la música de fondo
        confirm2 = MediaPlayer.create(this, R.raw.confirm2);
        confirm2.setVolume(0.5f, 0.5f); // Volumen (0.0f - 1.0f)

        // Inicializa el MediaPlayer con la música de fondo
        swipe = MediaPlayer.create(this, R.raw.swipe);
        swipe.setVolume(0.5f, 0.5f); // Volumen (0.0f - 1.0f)

        if (isadmin){
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    swipe.start();
                    AlertDialog.Builder builder = new AlertDialog.Builder(FirestoreActivity.this);

                    builder.setMessage("¿Estás seguro de que deseas borrar la gasolinera?")
                            .setTitle("Confirmar acción");

                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            borrar = true;
                            if (borrar){
                                adapter.deleteItem(viewHolder.getAdapterPosition());
                                borrar = false;
                                confirm1.start();
                                Toast.makeText(FirestoreActivity.this, "Elemento borrado exitosamente.", Toast.LENGTH_SHORT).show();
                            }
                            else {

                            }
                            dialog.dismiss();  // Cierra el diálogo
                        }
                    });

                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            borrar = false;
                            adapter.notifyDataSetChanged();
                            recyclerView.getRecycledViewPool().clear();
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();  // Cierra el diálogo
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }).attachToRecyclerView(recyclerView);
        }

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                Toast.makeText(FirestoreActivity.this, "Posición: " + position + " ID: " + id, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), UpdateNoteActivity.class);
                intent.putExtra("docid", id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("¿Estás seguro de que deseas borrar la gasolinera?")
                .setTitle("Confirmar acción");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                borrar = true;
                dialog.dismiss();  // Cierra el diálogo
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                borrar = false;
                dialog.dismiss();  // Cierra el diálogo
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
