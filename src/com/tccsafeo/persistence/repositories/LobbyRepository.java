package com.tccsafeo.persistence.repositories;

import com.tccsafeo.config.MongoDBConfig;
import com.tccsafeo.persistence.entities.LobbyEntity;

public class LobbyRepository extends GenericRepository<LobbyEntity>{
    public LobbyRepository() {
        super(MongoDBConfig.mongoDbClient(), LobbyEntity.class);
    }
}
