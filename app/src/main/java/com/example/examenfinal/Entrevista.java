package com.example.examenfinal;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public class Entrevista implements Parcelable {
    private String idEntrevista;
    private String descripcion;
    private String periodista;
    private Date fecha;
    private String imagen;
    private String audio;

    public Entrevista() {
    }

    public Entrevista(String idEntrevista, String descripcion, String periodista, Date fecha, String imagen, String audio) {
        this.idEntrevista = idEntrevista;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imagen = imagen;
        this.audio = audio;
    }

    public String getIdEntrevista() {
        return idEntrevista;
    }

    public void setIdEntrevista(String idEntrevista) {
        this.idEntrevista = idEntrevista;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    protected Entrevista(Parcel in) {
        idEntrevista = in.readString();
        descripcion = in.readString();
        periodista = in.readString();
        // Lee los demás atributos según su orden
    }

    public static final Creator<Entrevista> CREATOR = new Creator<Entrevista>() {
        @Override
        public Entrevista createFromParcel(Parcel in) {
            return new Entrevista(in);
        }

        @Override
        public Entrevista[] newArray(int size) {
            return new Entrevista[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idEntrevista);
        dest.writeString(descripcion);
        dest.writeString(periodista);
        // Escribe los demás atributos según su orden
    }
}
