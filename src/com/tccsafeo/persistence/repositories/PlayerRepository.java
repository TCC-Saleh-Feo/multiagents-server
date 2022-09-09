package com.tccsafeo.persistence.repositories;

import org.mongodb.morphia.Datastore;

import com.tccsafeo.config.MongoDBConfig;
import com.tccsafeo.persistence.entities.PlayerEntity;

public class PlayerRepository extends GenericRepository<PlayerEntity> {

    public PlayerRepository()
    {
        super(new MongoDBConfig().mongoDbClient(), PlayerEntity.class);
    }
}
