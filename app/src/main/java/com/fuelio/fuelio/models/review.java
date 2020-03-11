package com.fuelio.fuelio.models;

public class review
{

    public String name="none";
    public String review="none";
    private String id;
    private int rating=0;

    public review(){}

    public review(String name, String review, String id,int rating){

        this.name=name;
        this.review=review;
        this.id=id;
        this.rating=rating;
    }

    public String getName()
    {
        return name;
    }
    public String getId()
    {
        return id;
    }
    public String getReview()
    {
        return review;
    }
    public int getRating(){return rating;}


}