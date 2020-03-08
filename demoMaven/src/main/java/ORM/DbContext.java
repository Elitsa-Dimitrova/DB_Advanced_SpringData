package main.java.ORM;

import java.sql.SQLException;

public interface DbContext<E> {
    boolean persist(E entity) throws IllegalAccessException, SQLException;

    <E> void doCreate (Class<E> entity) throws SQLException;
    <E> void doAlter (Class<E> entity) throws SQLException;
    Iterable<E> find(Class<E> table);
    Iterable<E> find(Class<E> table, String where);
    E findFirst(Class<E> table) throws IllegalAccessException, InstantiationException, SQLException;
    E findFirst(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException;
}
