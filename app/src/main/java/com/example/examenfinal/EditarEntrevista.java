package com.example.examenfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarEntrevista extends AppCompatActivity {

    private ImageView imagen;
    private EditText descripcionEditText, periodistaEditText;
    private Button btnGuardar;

    private Entrevista entrevista;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_entrevista);

        imagen = findViewById(R.id.imagen);
        descripcionEditText = findViewById(R.id.descripcion);
        periodistaEditText = findViewById(R.id.periodista);
        btnGuardar = findViewById(R.id.btnsave);

        SharedPreferences sharedPreferences = getSharedPreferences("entrevista_pref", MODE_PRIVATE);
        String imagenBase64 = sharedPreferences.getString("imagen_entrevista", "");

// Cargar la imagen si es necesario
        if (!imagenBase64.isEmpty()) {
            byte[] imagenBytes = Base64.decode(imagenBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            imagen.setImageBitmap(bitmap);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("entrevistas");

        Intent intent = getIntent();
        if (intent != null) {
            entrevista = intent.getParcelableExtra("entrevista");
            if (entrevista != null) {
                descripcionEditText.setText(entrevista.getDescripcion());
                periodistaEditText.setText(entrevista.getPeriodista());

                btnGuardar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editarEntrevista();
                    }
                });
            }
        }
    }


    private void editarEntrevista() {
        String nuevaDescripcion = descripcionEditText.getText().toString().trim();
        String nuevoPeriodista = periodistaEditText.getText().toString().trim();

        DatabaseReference entrevistaRef = databaseReference.child("entrevistas").child(entrevista.getIdEntrevista());
        entrevistaRef.child("descripcion").setValue(nuevaDescripcion);
        entrevistaRef.child("periodista").setValue(nuevoPeriodista);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("entrevista_editada", entrevista);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}