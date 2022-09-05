package com.tccsafeo.data.repository;

import org.mongodb.morphia.Datastore;

import com.tccsafeo.config.MongoDBConfig;
import com.tccsafeo.data.entity.LobbyEntity;

public class LobbyRepository extends GenericRepository<LobbyEntity>
{
    public LobbyRepository(Datastore datastore)
    {
        super(new MongoDBConfig().mongoDbClient(), LobbyEntity.class);
    }
}
