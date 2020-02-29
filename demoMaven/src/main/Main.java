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
        connector.createConnection("root", "moitaparola97", "soft_uni");
        entities.User user = new entities.User("pesho", "pass", 20, new Date());
        EntityManager<entities.User> em = new EntityManager(connector.getConnection());
        em.persist(user);

        System.out.println("Input user id:");
        BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();
        user = new entities.User("Tosho","pass",20, new Date());
        em.persist(user);
        System.out.print(user.getAge());
    }

}
