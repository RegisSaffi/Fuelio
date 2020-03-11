package com.fuelio.fuelio.models;

public class service
{

    public String name="none";
    public String amount="none";
    private String id;
    boolean selected=false;
    boolean isSmall=false;

    public service(){}

    public service(String name, String amount, String id){

        this.name=name;
        this.amount=amount;
        this.id=id;
    }


    public void setSelected(boolean selected){this.selected=selected;}
    public boolean getSelected(){return selected;}

    public void setIsSmall(boolean isSmall){this.isSmall=isSmall;}
    public boolean getIsSmall(){return isSmall;}

    public String getName()
    {
        return name;
    }
    public String getId()
    {
        return id;
    }
    public String getAmount()
    {
        return amount;
    }



}