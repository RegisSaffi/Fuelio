package com.fuelio.fuelio.models;

public class userStation
{

    public String name="none";
    public String phone="none";
    private String type="none";
    private String id;


    public userStation(String name, String phone, String type, String id){

        this.name=name;
        this.phone=phone;
        this.type=type;
        this.id=id;

    }

    public String getName()
    {
        return name;
    }
    public String getId()
    {
        return id;
    }
    public String getPhone()
    {
        return phone;
    }
    public String getType(){return type;}

}