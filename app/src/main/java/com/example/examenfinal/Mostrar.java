package com.example.examenfinal;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Mostrar extends AppCompatActivity {

    Button btnEscucharAudio, btnBorrar, btnEditar;
    private RecyclerView recyclerView;
    private EntrevistaAdapter entrevistaAdapter;
    private List<Entrevista> entrevistaList;
    private int selectedPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar);

        btnEditar = findViewById(R.id.btnEditar);
        btnEscucharAudio = findViewById(R.id.btnEscucharAudio);
        btnBorrar = findViewById(R.id.btnBorrar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        entrevistaList = new ArrayList<>();
        entrevistaAdapter = new EntrevistaAdapter(this, entrevistaList);
        recyclerView.setAdapter(entrevistaAdapter);

        entrevistaAdapter.setOnItemClickListener(new EntrevistaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (selectedPosition == position) {
                    selectedPosition = RecyclerView.NO_POSITION; // Deselecciona si ya estaba seleccionado
                } else {
                    selectedPosition = position; // Selecciona el nuevo elemento
                }
                entrevistaAdapter.setSelectedPosition(selectedPosition);
            }
        });

        obtenerDatosDeFirebase();

        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarEntrevistaSeleccionada();
            }
        });

        btnEscucharAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    Entrevista entrevistaSeleccionada = entrevistaList.get(selectedPosition);
                    String audioUrl = entrevistaSeleccionada.getAudio(); // Obtiene la URL del audio de la entrevista seleccionada
                    Intent intent = new Intent(Mostrar.this, ReproducirAudioActivity.class);
                    intent.putExtra("audioUrl", audioUrl); // Agrega la URL del audio como extra
                    startActivity(intent);
                } else {
                    Toast.makeText(Mostrar.this, "Selecciona una entrevista para escuchar el audio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarEntrevistaSeleccionada();
            }
        });
    }

    private void obtenerDatosDeFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entrevistas");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entrevistaList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Entrevista entrevista = snapshot.getValue(Entrevista.class);
                    entrevistaList.add(entrevista);
                }
                entrevistaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void borrarEntrevistaSeleccionada() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            Entrevista entrevistaSeleccionada = entrevistaList.get(selectedPosition);
            String idEntrevista = entrevistaSeleccionada.getIdEntrevista();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entrevistas")
                    .child(idEntrevista);

            databaseReference.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            entrevistaList.remove(selectedPosition);
                            entrevistaAdapter.notifyItemRemoved(selectedPosition);
                            selectedPosition = RecyclerView.NO_POSITION;
                            Toast.makeText(Mostrar.this, "Entrevista borrada correctamente", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Mostrar.this, "Error al borrar la entrevista", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(Mostrar.this, "Selecciona una entrevista para borrar", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarEntrevistaSeleccionada() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            Entrevista entrevistaSeleccionada = entrevistaList.get(selectedPosition);
            Intent intent = new Intent(Mostrar.this, EditarEntrevista.class);
            intent.putExtra("entrevista", entrevistaSeleccionada);

            // Pasa la fecha original a la actividad de edición
            intent.putExtra("fechaOriginal", entrevistaSeleccionada.getFecha());

            startActivity(intent);
        } else {
            Toast.makeText(Mostrar.this, "Selecciona una entrevista para editar", Toast.LENGTH_SHORT).show();
        }
    }
}