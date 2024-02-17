package com.example.pm1e17219;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.pm1e17219.Configuracion.SQLiteConexion;
import com.example.pm1e17219.Configuracion.Transacciones;
import com.example.pm1e17219.Models.Contactos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ActivityUpdate extends AppCompatActivity {
    EditText edtNombre, edtTelefono, edtNota, edtPais;
    SQLiteConexion Conexion;
    Button btnActualizar;
    ArrayList<Contactos> ListCountry;
    ArrayList<String> ArregloContactos;
    ImageView Img;
    Spinner spinner;
    private int idContact;

    static final int Peticion_AccesoCamara = 101;
    static final int Peticion_TomarFoto = 102;
    String currentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        edtNombre = (EditText) findViewById(R.id.edtNombre);
        edtTelefono = (EditText) findViewById(R.id.edtTelefono);
        edtNota = (EditText) findViewById(R.id.edtnota);
        edtPais = (EditText) findViewById(R.id.edtPais);
        btnActualizar = (Button)findViewById(R.id.btnUpdate);
        spinner = (Spinner)findViewById(R.id.SpinnerAct);
        Img = (ImageView)findViewById(R.id.imgActualizar);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        try {
            Conexion = new SQLiteConexion(this, Transacciones.namedb, null, 1);
            GetCountry();

            Bundle obtDatosAgrup = getIntent().getExtras();

            if(obtDatosAgrup != null){
                idContact = obtDatosAgrup.getInt("id");

                byte[] imageBytes = obtDatosAgrup.getByteArray("imagen");
                Bitmap viewImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                edtNombre.setText(obtDatosAgrup.getString("nombre"));
                edtTelefono.setText(String.valueOf(obtDatosAgrup.getInt("telefono", 0))); // Convertir a String
                edtPais.setText(obtDatosAgrup.getString("pais"));
                edtNota.setText(obtDatosAgrup.getString("nota"));
                Img.setImageBitmap(viewImage);
            }

            btnActualizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(idContact != 0){
                        updateContact(idContact,currentPhotoPath, String.valueOf(edtPais), String.valueOf(edtNombre), edtTelefono, String.valueOf(edtNota));
                    }else{
                        Toast.makeText(getApplicationContext(), "no hay datos para borrar", Toast.LENGTH_LONG).show();
                    }
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    idContact = ListCountry.get(i).getId();
                    spinner.setSelection(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });


        }catch (Exception ex){
            ex.printStackTrace();
        }


        Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Permisos();
            }
        });
    }

    private void updateContact(int id, String image, String pais, String nombre, EditText telefono, String nota){
        SQLiteDatabase db = Conexion.getWritableDatabase();


        ContentValues values = new ContentValues();

        if (nombre != null && !nombre.isEmpty()) {
            values.put(Transacciones.nombre, nombre);
        }if (telefono != null) {
            values.put(Transacciones.telefono, edtTelefono.getText().toString());
        }if (nota != null && !nota.isEmpty()) {
            values.put(Transacciones.nota, nota);
        }if (pais != null && !pais.isEmpty()) {
            values.put(Transacciones.pais, pais);
        }if (image != null && !image.isEmpty()) {
            values.put(Transacciones.foto, image);
        }

        String selection = Transacciones.id+ " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                Transacciones.table,
                values,
                selection,
                selectionArgs);


    }

    private void GetCountry() {
        SQLiteDatabase db = Conexion.getReadableDatabase();
        Contactos Country = null;
        ListCountry = new ArrayList<Contactos>();
        Cursor Cursor = db.rawQuery(Transacciones.SelectTableContactos, null);
        while (Cursor.moveToNext()) {
            Country = new Contactos();
            Country.setPais(Cursor.getString(4));
            ListCountry.add(Country);
        }
        Cursor.close();
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
}
