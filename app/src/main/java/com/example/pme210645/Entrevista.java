package com.example.pme210645;

public class Entrevista {
    private String id;
    private String descripcion;
    private String periodista;
    private String audio;
    private String fecha;
    private String img;

    public Entrevista(String id, String descripcion, String periodista, String audio, String fecha, String img) {
        this.id = id;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.audio = audio;
        this.fecha = fecha;
        this.img = img;
    }

    public Entrevista() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public String getAudio() {
        return audio;
    }

    public String getFecha() {
        return fecha;
    }

    public String getImg() {
        return img;
    }

}
