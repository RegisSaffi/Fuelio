package com.fuelio.fuelio.models;

import com.google.type.LatLng;

public class requestCard
{

    public String name="none";
    public String issue="none";
    public String date="date";
    private String status="none";
    private String id;
    String desc;

    public requestCard(){}

    public requestCard(String name, String issue, String date,String status,String desc,String id){

        this.name=name;
        this.issue=issue;
        this.date=date;
        this.status=status;
        this.id=id;
        this.desc=desc;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }
    public String getIssue()
    {
        return issue;
    }
    public String getDate()
    {
        return date;
    }
    public String getStatus(){return status;}
    public String getDesc(){return desc;}


}