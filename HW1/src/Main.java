import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "minions_db";

    private static Connection connection;
    private static String query;
    private static PreparedStatement preparedStatement;
    private static BufferedReader reader;

    public static void main(String[] args) throws SQLException, IOException {

        reader = new BufferedReader(new InputStreamReader(System.in));

        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "moitaparola97");

        connection = DriverManager.
                getConnection(CONNECTION_STRING + DATABASE_NAME, properties);

        //2. Get villains` names
        //getVillainsNamesAndMinionsCount();

        //3.	Get Minion Names
        //getMinionNamesEx();

        //4.	Add Minion
        //addMinionEx();

        //5.	Change Town Names Casing
        //changeTownNamesCasing();

        //6.	*Remove Villain
        //removeVillain();

        //7.	Print All Minion Names
        //printAllMinionNamesEx();

        //8.	Increase Minions Age
        //increaseMinionAgeEx();

        //9.	Increase Age Stored Procedure

        //Add procedure:
        //use minions_db;
        //DELIMITER  %%
        //create procedure usp_get_older (minion_id int)
        //begin
        //    UPDATE minions
        //    set age = age+1
        //    where id = minion_id;
        //end %%

        //increaseAgeWithStoredProcedure();

        System.out.println();
    }

    private static void increaseAgeWithStoredProcedure() throws IOException, SQLException {
        System.out.println("Enter minion Id:");
        int minionId = Integer.parseInt(reader.readLine());

        query = "call usp_get_older (?)";

        CallableStatement callableStatement = connection.prepareCall(query);
        callableStatement.setInt(1, minionId);

        callableStatement.execute();

        String query = "select name, age from minions\n" +
                "where id = ?";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, minionId);

        ResultSet resultSet = preparedStatement.executeQuery();

        printResultSet(resultSet);

    }

    private static void increaseMinionAgeEx() throws IOException, SQLException {
        System.out.println("Minions IDs: ");
        String[] input = reader.readLine().split(" ");
        Integer[] minionsId = new Integer[input.length];

        for (int i=0; i<input.length;i++){

            int currId = Integer.parseInt(input[i]);
            changeMinionAge(currId);
            changeMinionNames(currId);
        }

        printAllMinions();

    }

    private static void printAllMinions() throws SQLException {
        String query = "select name, age from minions\n";

        preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        printResultSet(resultSet);
    }

    private static void printResultSet(ResultSet resultSet) throws SQLException {

        while (resultSet.next()) {
            System.out.printf("%s %d%n", resultSet.getString("name"),
                    resultSet.getInt("age"));
        }
    }

    private static void changeMinionAge(int minionId) throws SQLException {
        String query = "update minions\n" +
                "join (\n" +
                "select name, id from minions\n" +
                "where id = ?) as temp\n" +
                "on temp.id = minions.id\n" +
                "set minions.age = minions.age + 1;";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, minionId);

        preparedStatement.execute();
    }

    private static void changeMinionNames(int minionId) throws SQLException {
        String query = "update minions\n" +
                "join (\n" +
                "select name, id from minions\n" +
                "where id = ?) as temp\n" +
                "on temp.id = minions.id\n" +
                "set minions.name = LOWER (minions.name);";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, minionId);

        preparedStatement.execute();

    }

    private static void printAllMinionNamesEx() throws SQLException {
        int minionsCount = getMinionsCount();

        for (int i = 1, j=minionsCount; i <= j; i++, j--) {

            if (i==j)
            {
                System.out.printf("%s%n", getEntityNameByID(i, "minions"));
                return;
            }

            if(!checkIfEntityExists(i, "minions")){
                i++;
            }
            System.out.printf("%s%n", getEntityNameByID(i, "minions"));

            if(!checkIfEntityExists(j, "minions")){
                j--;
            }
            System.out.printf("%s%n", getEntityNameByID(j, "minions"));
        }
    }

    private static int getMinionsCount() throws SQLException {
        query = "select count(id) as count from minions;";

        preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();

        int numOfCapturedMinions = resultSet.getInt("count");
        return numOfCapturedMinions;
    }

    private static void removeVillain() throws IOException, SQLException {
        System.out.println("Villain id:");
        int villainId = Integer.parseInt(reader.readLine());

        if(!checkIfEntityExists(villainId, "villains")){
            System.out.println("No such villain was found");
        }else {
            int numOfcapturedMinions = getCountOfCapturedMinionsByVillain(villainId);
            String villainName = getEntityNameByID(villainId, "villains");

            releaseMinions(villainId);
            deleteVillain(villainId);

            System.out.printf("%s was deleted%n", villainName);
            System.out.printf("%d minions released",numOfcapturedMinions);

        }
    }

    private static void releaseMinions(int villain_id) throws SQLException {
        query = "delete from minions_villains where villain_id = ?";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, villain_id);

        preparedStatement.execute();

    }

    private static void deleteVillain(int villain_id) throws SQLException {
        query = "delete from villains where id = ?";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, villain_id);

        preparedStatement.execute();

    }

    private static int getCountOfCapturedMinionsByVillain(int villain_id) throws SQLException {
        query = "select count(minion_id) as count from minions_villains\n" +
                "where villain_id = ?";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, villain_id);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();

        int numOfCapturedMinions = resultSet.getInt("count");
        return numOfCapturedMinions;
    }

    private static void changeTownNamesCasing() throws IOException, SQLException {
        System.out.println("Input a country name: ");
        String country = reader.readLine();
        
        updateTownNamesOfTownsIn(country);

    }

    private static void updateTownNamesOfTownsIn(String country) throws SQLException {
        String query = "update towns\n" +
                "join (\n" +
                "select name from towns\n" +
                "where country = ?) as temp\n" +
                "on temp.name = towns.name\n" +
                "set towns.name = UPPER(towns.name);";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, country);

        preparedStatement.execute();

        ResultSet resultSet = getChangedNames(country);

        formatOutPut(resultSet);
    }

    private static void formatOutPut(ResultSet resultSet) throws SQLException {
        int size =0;
        if(!resultSet.next()){
            System.out.println("No town names were affected.");
        }else {
            resultSet.last();    // moves cursor to the last row
            size = resultSet.getRow(); // get row id


            resultSet.beforeFirst();

            System.out.printf("%d town names were affected.%n", size);

            List<String> names = new ArrayList<>();

            while(resultSet.next()){
                names.add(resultSet.getString("name"));
            }
            System.out.println(names);
        }


    }

    private static ResultSet getChangedNames(String country) throws SQLException {
        query = "select name from towns where country= ?";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, country);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet;
    }

    private static void addMinionEx() throws IOException, SQLException {
        System.out.println("Minion: ");
        String[] minionParams = reader.readLine().split("\\s");

        String mName = minionParams[0];
        int mAge = Integer.parseInt(minionParams[1]);
        String mTown = minionParams[2];


        System.out.println("Villain: ");
        String[] villainParams = reader.readLine().split("\\s");

        String vName = villainParams[0];

        if(!checkIfEntityExistsByName(mTown, "towns")){
            addEntityIntoTowns(mTown);
        }


        if (!checkIfEntityExistsByName(vName,"villains")){
            addEntityIntoVillains(vName);
        }

        if (!checkIfEntityExistsByName(mName,"minions")){
            addEntityIntoMinions(mName,mAge, mTown);
        }


        addVillainMinionRelationship(
                getEntityIdByName
                        (vName, "villains"),
                            getEntityIdByName(mName, "minions"));
    }

    private static void addVillainMinionRelationship(int villain_id, int minion_id) throws SQLException {
        String nQuery = "insert into minions_villains values(?,?)";

        preparedStatement = connection.prepareStatement(nQuery);
        preparedStatement.setInt(1,minion_id);
        preparedStatement.setInt(2, villain_id);

        preparedStatement.execute();

        System.out.printf("Successfully added %s to be minion of %s.",
                getEntityNameByID(minion_id, "minions"),
                getEntityNameByID(villain_id, "villains"));
    }

    private static void addEntityIntoMinions(String minionName, int minionAge, String minionTown) throws SQLException {
        int townID= getEntityIdByName(minionTown, "towns");

        String newQuery = "insert into minions (`name`, age, town_id) value(?, " +
                minionAge+ " , ?);";

        preparedStatement = connection.prepareStatement(newQuery);
        preparedStatement.setString(1, minionName);
//        preparedStatement.setInt(2, minionAge);
        preparedStatement.setInt(2, townID);

        preparedStatement.execute();
    }

    private static int getEntityIdByName(String entityName, String tableName) throws SQLException {
        query = "select id from "+tableName+ " where name = ?;";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,entityName);


        ResultSet resultSet = preparedStatement.executeQuery();

         resultSet.next();
         return resultSet.getInt("id");
    }

    private static void addEntityIntoVillains(String villainName) throws SQLException {
        query = "insert into villains (`name`, evilness_factor) value(?, 'evil');";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, villainName);

        preparedStatement.execute();
        System.out.printf("Villain %s was added to the database.%n", villainName);
    }

    private static void addEntityIntoTowns(String mTown) throws SQLException {
        query = "insert into towns(`name`, country) value(?, null);";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, mTown);

        preparedStatement.execute();
        System.out.printf("Town %s was added to the database.%n", mTown);
    }

    private static boolean checkIfEntityExistsByName(String name, String tableName) throws SQLException {
        query = "select * from "+ tableName+ " where name = ?";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,name);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    private static void getMinionNamesEx() throws IOException, SQLException {
        System.out.println("Enter villain id: ");
        Integer villain_id = Integer.parseInt(reader.readLine());

        if(!checkIfEntityExists(villain_id, "villains")){
            System.out.printf("No villain with ID %d exists in the database.", villain_id);
            return;
        }
        
        System.out.printf("Villain: %s%n", getEntityNameByID(villain_id, "villains"));

        getMinionNamesAndAgeByVillainId(villain_id);
    }

    private static void getMinionNamesAndAgeByVillainId(Integer villain_id) throws SQLException {
        query = "select name, age from minions\n" +
                "join minions_villains mv on minions.id = mv.minion_id\n" +
                "where villain_id = ?;";

        preparedStatement=connection.prepareStatement(query);
        preparedStatement.setInt(1, villain_id);

        ResultSet resultSet = preparedStatement.executeQuery();

        int minionN= 1;

        while (resultSet.next()){
            System.out.printf("%d. %s %d%n",
                    minionN, resultSet.getString(1),resultSet.getInt(2));
            minionN++;
        }

    }

    private static String getEntityNameByID(Integer entity_id, String tableName) throws SQLException {
        query = "select name from " + tableName + " where id = ? ";

        preparedStatement=connection.prepareStatement(query);
        preparedStatement.setInt(1, entity_id);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return  resultSet.getString("name");
    }

    private static boolean checkIfEntityExists(Integer entity_id, String tableName) throws SQLException {
        query = "select * from " + tableName+ " where id = ? ";
        preparedStatement=connection.prepareStatement(query);
        preparedStatement.setInt(1, entity_id);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    private static void getVillainsNamesAndMinionsCount() throws SQLException {
        query = "select name, count(minion_id) as count\n" +
                "from villains\n" +
                "join minions_villains mv\n" +
                "on villains.id = mv.villain_id\n" +
                "group by villain_id\n" +
                "having count > 15\n" +
                "order by count desc;";

        preparedStatement = connection.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            System.out.printf("%s, %d %n", resultSet.getString("name"), resultSet.getInt("count"));
        }
    }
}
