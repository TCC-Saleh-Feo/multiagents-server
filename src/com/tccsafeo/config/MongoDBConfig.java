package com.tccsafeo.config;

import java.util.concurrent.TimeUnit;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

public class MongoDBConfig
{
    private MongoClient _mongoClient() {
        try {
            MongoClientOptions.Builder options = new MongoClientOptions.Builder()
                    .connectionsPerHost(30);
            MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost:27017", options);
            return new MongoClient(mongoClientURI);
        } catch (MongoClientException e) {
            System.out.println("ERROR TO CONNECT");
        }
        return null;
    }

    public Datastore mongoDbClient()
    {
        Morphia morphia = new Morphia();
        Datastore datastore = morphia.createDatastore(_mongoClient(), "MultiagentDB");
        return datastore;
    }
}
