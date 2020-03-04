package main.java.ORM;

import main.java.Annotations.Column;
import main.java.Annotations.Entity;
import main.java.Annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class EntityManager<E> implements DbContext<E> {
    private Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        if (value == null || (int)value <= 0){
            return this.doInsert(entity, primary);
        }
        return this.doUpdate(entity,primary);
    }

    @Override
    public <E1> void doCreate(Class<E1> entity) throws SQLException {
        String query = "CREATE TABLE " + this.getTableName(entity) + " (";

        Field[] fields = entity.getDeclaredFields();

        for (int i = 0; i<fields.length; i++){
            Field currField = fields[i];
            String columnsDefinition = "";

            currField.setAccessible(true);

            columnsDefinition += this.getColumnName(currField)+ " "
                    + getDataType(currField);

            if(currField.isAnnotationPresent(Id.class)){
                columnsDefinition += " primary key auto_increment";
            }

            if(i < fields.length-1){
                columnsDefinition += ",\n";
            }else columnsDefinition += ")";


            query +=columnsDefinition;

            System.out.println();
        }

        connection.prepareStatement(query).execute();
    }

    private String getDataType(Field currField) {
        String result = "";

        switch(currField.getType().getSimpleName()){
            case "int" :
            case "Integer":
                result = "int";
                break;
            case "Date":
                result = "DATETIME";
                break;
            case "String":
                result = "varchar(50)";
                break;
        }

        return result;
    }

    private boolean doUpdate(E entity, Field primary) throws SQLException, IllegalAccessException {
        String query = "UPDATE " + this.getTableName(entity.getClass()) + " SET ";
        String fieldsNamesAndValues = "";

        Field[] fields = entity.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if(!field.getName().equals(primary.getName())){
                fieldsNamesAndValues +=   this.getFieldName(field)+"=";
                Object value = field.get(entity);

                if(value instanceof Date){
                    fieldsNamesAndValues+= "`" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "`";
                }else{
                    fieldsNamesAndValues += "`" + value + "`";
                }

                if (i < fields.length - 1) {
                    fieldsNamesAndValues += ", ";
                }
            }
        }
        return connection.prepareStatement(query).execute();

    }

    private String getFieldName(Field field) {
        String fieldName = "";

        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnotation = field.getAnnotation(Column.class);
            fieldName = columnAnotation.name();
        } else {
            fieldName = field.getName();
        }

        return fieldName;
    }

    private boolean doInsert(E entity, Field primary) throws SQLException, IllegalAccessException {
       if(!checkIfTableExists(entity.getClass())){
            this.doCreate(entity.getClass());
        }

        String query = "INSERT INTO " + this.getTableName(entity.getClass()) + " (";
        String columns = "";
        String values = "";

        Field[] fields = entity.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (!field.getName().equals(primary.getName())) {
                columns += '`' + this.getColumnName(field) + '`';

                Object value = field.get(entity);

                if (value instanceof Date) {
                    values += "'" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "'";
                } else {
                    values += "\'" + value + "\'";
                }

                if (i < fields.length - 1) {
                    columns += ", ";
                    values += ", ";
                }
            }
        }
        query += columns + ") " + "VALUES(" + values + ")";
        return connection.prepareStatement(query).execute();
    }

    private String getTableName(Class entity) {
        String tableName = "";

        tableName = ((Entity)entity.getAnnotation(Entity.class)).name() + "s";

        if (!tableName.trim().equals("")){
            tableName = entity.getSimpleName() + "s";
        }

        return tableName;
    }

    private String getColumnName(Field field) {
        String fieldName = "";

        fieldName = field.getAnnotation(Column.class).name();

        if (!fieldName.trim().equals("")){
            fieldName = field.getName();
        }

        return fieldName;
    }

    @Override
    public Iterable<E> find(Class<E> table) {
        return null;
    }

    @Override
    public Iterable<E> find(Class<E> table, String where) {
        return null;
    }

    @Override
    public E findFirst(Class<E> table) throws IllegalAccessException, InstantiationException, SQLException {
        Statement stmt = connection.createStatement();
        String	query = "Select * FROM" + this.getTableName(table) + "LIMIT 1";
        ResultSet rs = stmt.executeQuery(query);
        E entity = table.newInstance();
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }

    private void fillEntity(Class<E> table, ResultSet rs, E entity) throws SQLException, IllegalAccessException {
        Field[] fields = table.getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            this.fillField(field, entity, rs, field.getAnnotation(Column.class).name());
        }
    }

    private void fillField(Field field, E entity, ResultSet rs, String fieldName) throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        if(field.getType()== int.class || field.getType() == Integer.class) {
            field.set(entity, rs.getInt(fieldName));
        }else if(field.getType()== long.class || field.getType() == Long.class) {
            field.set(entity, rs.getLong(fieldName));
        }else if(field.getType() == String.class) {
            field.set(entity, rs.getString(fieldName));
        }else if(field.getType()== Date.class) {
            field.set(entity, rs.getDate(fieldName));
        }
    }

    @Override
    public E findFirst(Class<E> table, String where) throws IllegalAccessException, InstantiationException, SQLException {
        Statement stmt = connection.createStatement();
        String	query = "Select * FROM" + this.getTableName(table) +
                "WHERE 1 " +  (where != null ? "AND" + where : "") + "LIMIT 1";
        ResultSet rs = stmt.executeQuery(query);
        E entity = table.newInstance();
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }

    private Field getId(Class entity){
        return Arrays.stream(entity.getDeclaredFields()).
                filter(field -> field.isAnnotationPresent(Id.class)).
                findFirst().
                orElseThrow(() -> new UnsupportedOperationException("Entity does not have primary key."));
    }

    private boolean checkIfTableExists(Class entity) throws SQLException {
        String query = "select table_name from information_schema.TABLES\n" +
                "where table_schema = 'orm_db'\n" +
                "and table_name = '" + this.getTableName(entity) + "';";

        ResultSet resultSet = connection.prepareStatement(query).executeQuery();
        return resultSet.next();
    }
}
