package main;

import main.java.ORM.Connector;
import main.java.ORM.EntityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws SQLException, IllegalAccessException, IOException {
        Connector connector = new Connector();
        connector.createConnection("root", "moitaparola97", "orm_db");

        EntityManager<entities.User> em = new EntityManager(connector.getConnection());

       // em.doCreate(entities.User.class);

        entities.User user = new entities.User("pesho", "pass", 20, new Date());
        entities.User user2 = new entities.User("gosho", "pass1", 30, new Date());

        em.persist(user);
        em.persist(user2);

    }

}
