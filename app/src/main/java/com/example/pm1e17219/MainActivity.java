package com.example.pm1e17219;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pm1e17219.Configuracion.SQLiteConexion;
import com.example.pm1e17219.Configuracion.Transacciones;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    EditText edtNombre, edtTelefono, edtNota;
    Spinner pais;
    Button btnSalvar, btnContactos, btnAgregarContacto;

    ImageView Img;
    private static final int IMAGE_CAPTURE = 1;
    private byte[] fotoTomada;
    static final int Peticion_AccesoCamara = 101;
    static final int Peticion_TomarFoto = 102;

    Button btnFoto;
    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtNombre = (EditText) findViewById(R.id.edtNombre);
        edtTelefono = (EditText) findViewById(R.id.edtTelefono);
        edtNota = (EditText) findViewById(R.id.edtNota);
        btnSalvar = (Button) findViewById(R.id.btnGuardar);
        btnContactos = (Button) findViewById(R.id.btnContactos);
        btnAgregarContacto = (Button) findViewById(R.id.btnAgregarContacto);
        pais = (Spinner) findViewById(R.id.spinner);
        Img = (ImageView) findViewById(R.id.Img);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pais.setAdapter(adapter);


        btnAgregarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edtNombre.setText("");
                edtTelefono.setText("");
                edtNota.setText("");

                pais.setSelection(0);

                Img.setImageDrawable(null);

                Toast.makeText(getApplicationContext(), " ", Toast.LENGTH_SHORT).show();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = edtNombre.getText().toString();
                String telefono = edtTelefono.getText().toString();
                if (nombre.isEmpty()) {
                    edtNombre.setError("Este campo no puede estar vacío");
                }
                else if(telefono.isEmpty()) {
                    edtTelefono.setError("Este campo no puede estar vacío");
                }else{
                    AddContact();
                }


            }
        });

        btnContactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityList.class);
                startActivity(intent);
            }
        });

        Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Permisos();
            }
        });
    }


    private void Permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(this, new String[]{   Manifest.permission.CAMERA},
                    Peticion_AccesoCamara);

        }
        else
        {
            //Tomar foto
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Peticion_AccesoCamara){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                dispatchTakePictureIntent();
            }else{
                Toast.makeText(getApplicationContext(), "Permiso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {


            }

            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm1e17219.fileprovider", /*Se obtiene del build gradle Module(Ayuda a definir que pertenece a esta aplicación)*/
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Peticion_TomarFoto);
            }
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Peticion_TomarFoto && resultCode == RESULT_OK){


            try {
                File foto = new File(currentPhotoPath);
                Img.setImageURI(Uri.fromFile(foto));

            }catch (Exception ex) {
                ex.toString();
            }

        }

    }


    private void AddContact () {
        try {
            if (currentPhotoPath!= null) {

                SQLiteConexion Conexion = new SQLiteConexion(this, Transacciones.namedb, null, 1);

                SQLiteDatabase db = Conexion.getWritableDatabase();

                ContentValues Valores = new ContentValues();
                Valores.put(Transacciones.nombre, edtNombre.getText().toString());
                Valores.put(Transacciones.telefono, edtTelefono.getText().toString());
                Valores.put(Transacciones.nota, edtNota.getText().toString());
                Valores.put(Transacciones.pais, pais.getSelectedItem().toString());
                Valores.put(Transacciones.foto, currentPhotoPath);

                Long Result = db.insert(Transacciones.table, Transacciones.id, Valores);

                Toast.makeText(this, "Los datos se registraron correctamente", Toast.LENGTH_LONG).show();
                db.close();
            } else {
                Toast.makeText(this, "Debes tomar una foto antes de guardar el contacto.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
        }
    }



}