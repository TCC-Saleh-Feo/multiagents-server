package com.tccsafeo.data.repository;

import org.mongodb.morphia.Datastore;

public abstract class GenericRepository<T>
{
    private Datastore _datastore;

    private Class<T> _kclass;

    public GenericRepository(Datastore datastore, Class<T> kclass)
    {
        this._datastore = datastore;
        this._kclass = kclass;
    }

    public void save(T object)
    {
        this._datastore.save(object);
    }

    public T findById(String id)
    {
        return this._datastore.createQuery(_kclass)
                .field("id")
                .contains(id)
                .get();
    }
}
