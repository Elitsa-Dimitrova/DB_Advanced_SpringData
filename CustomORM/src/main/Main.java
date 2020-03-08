package main;

import main.java.ORM.Connector;
import main.java.ORM.EntityManager;
import main.java.entities.UserSalary;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws SQLException, IllegalAccessException, IOException {
        Connector connector = new Connector();
        connector.createConnection("root", "moitaparola97", "orm_db");

        EntityManager<entities.User> em = new EntityManager(connector.getConnection());

        //em.doCreate(entities.User.class);

        entities.User userS = new entities.User("pesho", "pass", 20, new Date(), 500.);
        //UserSalary userS2 = new UserSalary("gosho", "pass1", 30, new Date(), 4000.);

        em.persist(userS);




    }

}
