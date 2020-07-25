package com.fuelio.fuelio.models;

public class vehicle
{

    public String name="none";
    public String plate="none";
    private String id;
    boolean selected=false;
    boolean isSmall=false;

    boolean isService=false;

    public vehicle(){}

    public vehicle(String name, String plate,String id){

        this.name=name;
        this.plate=plate;
        this.id=id;
    }


    public void setSelected(boolean selected){this.selected=selected;}
    public boolean getSelected(){return selected;}

    public void setIsSmall(boolean isSmall){this.isSmall=isSmall;}
    public boolean getIsSmall(){return isSmall;}

    public void setIsService(boolean isService){this.isService=isService;}
    public boolean getIsService(){return isService;}

    public String getName()
    {
        return name;
    }
    public String getId()
    {
        return id;
    }
    public String getPlate()
    {
        return plate;
    }



}