/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.storage.dropbox.services.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.dataconservancy.storage.dropbox.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLHelper implements DcsliteSQLConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLHelper.class);

    private static Connection conn = null;
    
    private static PreparedStatement stmt = null;
    
    private static String DATABASE_CONNECTION_PROPERTIES = "/org/dataconservancy/lite/database.properties";
    
    private static boolean derbyFlag = false;

    private static ObjectPool pool = null;

    static {
        try {
            pool = initSQLConnectionPool();
        }
        catch (Exception e) {
            LOGGER.error("Unable to load database connection properties from " + DATABASE_CONNECTION_PROPERTIES + ":"
                    +
                e.getMessage(), e);
        }
    }
    
    /**
     * Creates a temporary table for Derby.
     */
    public static void createTable() {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(CREATE_TABLE);
            stmt.executeUpdate();
        }
        catch (Exception e) {
            LOGGER.error("Failed to create the table", e);
        }
        try {
            stmt.close();
            pool.returnObject(conn);
        }
        catch (Exception e) {
            LOGGER.error("Failed to close the statement.", e);
        }
    }

    /**
     * Creates a new user in the database.
     * 
     * @param firstName
     * @param lastName
     * @param occupation
     * @param organization
     * @param username
     * @param password
     * @param secretQuestion
     * @param secretAnswer
     * @param email
     * @return String
     */
    public static String createUser(Person person) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(INSERT_USER);
            stmt.setString(1, person.getUsername());
            stmt.setString(2, person.getPassword());
            // Setting Dropbox App Key and Secret to nothing (for derby) and to Null (for MySQL)
            if (derbyFlag) {
                stmt.setString(3, "nothing");
                stmt.setString(4, "nothing");
            }
            else {
                stmt.setNull(3, 0);
                stmt.setNull(4, 0);
            }
            stmt.setString(5, person.getFirstName());
            stmt.setString(6, person.getLastName());
            stmt.setString(7, person.getOccupation());
            stmt.setString(8, person.getOrganization());
            stmt.setString(9, person.getSecretQuestion());
            stmt.setString(10, person.getSecretAnswer());
            stmt.setString(11, person.getEmail());
            int i = stmt.executeUpdate();
            if (i > 0) {
                LOGGER.debug("Created user!");
                return person.getUsername();
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to create the user", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Updates the user's account with its unique Dropbox App Key and App Secret for seamless future authentication.
     * 
     * @param appKey
     * @param appSecret
     * @param username
     */
    public static void updateUser(String appKey, String appSecret, String username) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(UPDATE_USER);
            stmt.setString(1, appKey);
            stmt.setString(2, appSecret);
            stmt.setString(3, username);
            System.out.println("Executing the query.");
            int i = stmt.executeUpdate();
            if (i > 0) {
                // Just making sure...
                LOGGER.debug("updated account with app_key <" + appKey + "> and app_secret <" + appSecret + ">");
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to update the user with their appKey and appSecret", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
    }
    
    /**
     * Checks database to see if the chosen username is available or not.
     * 
     * @param username
     * @return boolean
     */
    public static Boolean isUsernameAvailable(String username) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(CHECK_USER);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LOGGER.debug("Username is available!");
                return false;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to check the username", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return true;
    }
    
    /**
     * Authenticates the user.
     * 
     * @param username
     * @return HashMap<String, String>
     */
    public static HashMap<String, String> authenticate(String username) {
        HashMap<String, String> account = new HashMap<String, String>();
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(AUTHENTICATE);
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account.put("username", rs.getString("username"));
                if (derbyFlag) {
                    // Getting simple String password for Derby.
                    account.put("password", rs.getString("password"));
                }
                else {
                    account.put("password", new String(rs.getBytes("password")));
                }
                return account;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to authenticate the user", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return account;
    }
    
    /**
     * Retrieves the user's Dropbox App Key and App Secret to authenticate the Dropbox session.
     * 
     * @param username
     * @return HashMap<String, String>
     */
    public static HashMap<String, String> retrieveTokens(String username) {
        HashMap<String, String> tokens = new HashMap<String, String>();
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(RETRIEVE_TOKENS);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("app_key") != null && rs.getString("app_secret") != null) {
                    tokens.put("key", rs.getString("app_key"));
                    tokens.put("secret", rs.getString("app_secret"));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to retrieve the tokens.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return tokens;
    }
    
    /**
     * Sets user's session ID to take advantage of cookies and login the user automatically.
     * 
     * @param username
     * @param sid
     * @return String
     */
    public static boolean setSessionId(String username, String sid) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(UPDATE_SESSION_ID);
            stmt.setString(1, sid);
            stmt.setString(2, username);
            int i = stmt.executeUpdate();
            if (i > 0) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to set the session ID.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return false;
    }
    
    /**
     * Checks to see if the user has a valid session ID associated with their account.
     * 
     * @param sid
     * @return String
     */
    public static String checkSessionId(String sid) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(CHECK_SESSION_ID);
            stmt.setString(1, sid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Retrieves user's username from the database.
     * 
     * @param recipient
     * @return String
     */
    public static String getUsername(String recipient) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(GET_USERNAME);
            stmt.setString(1, recipient);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username;
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not fetch the username", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Validates user's account based on their specific sercret question/answer.
     * 
     * @param email
     * @param secretAnswer
     * @return String
     */
    public static String validateAccount(String email, String secretAnswer) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(VALIDATE_EMAIL_ANSWER);
            stmt.setString(1, email);
            stmt.setString(2, secretAnswer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username;
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not validate the account.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Validates user based on their email address.
     * 
     * @param email
     * @return String
     */
    public static String validateAccount(String email) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(VALIDATE_EMAIL);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username;
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not validate the account.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Resets user's password.
     * 
     * @param username
     * @param pwHash
     * @return boolean
     */
    public static Boolean resetPassword(String username, String pwHash) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(RESET_PASSWORD);
            stmt.setString(1, pwHash);
            stmt.setString(2, username);
            int i = stmt.executeUpdate();
            if (i > 0) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not reset the password.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return false;
    }
    
    /**
     * Checks to see if the email is already used or not.
     * 
     * @param email
     * @return boolean
     */
    public static Boolean isEmailAvailable(String email) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(VALIDATE_EMAIL);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return false;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to check the email.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return true;
    }
    
    /**
     * Retrieves user's secret question.
     * 
     * @param email
     * @return String
     */
    public static String retrieveSecretQuestion(String email) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(RETRIEVE_SECRET_QUESTION);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String secretQuestion = rs.getString("secret_question");
                return secretQuestion;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to retrieve the Secret Question.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return null;
    }
    
    /**
     * Sets user's session ID to null if they logout of the system.
     * 
     * @param username
     * @return boolean
     */
    public static Boolean setSessionIdToNull(String username) {
        try {
            conn = (Connection) pool.borrowObject();
            stmt = conn.prepareStatement(SET_SESSION_ID_TO_NULL);
            stmt.setString(1, username);
            int i = stmt.executeUpdate();
            if (i > 0) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to set the session ID to null.", e);
        }
        finally {
            try {
                stmt.close();
                pool.returnObject(conn);
            }
            catch (Exception e) {
                LOGGER.error("Failed to close the statement.", e);
            }
        }
        return false;
    }

    /**
     * Loads the properties file.
     * 
     * @param filename
     * @return Properties
     * @throws IOException
     */
    public static Properties loadPropertiesFile(String filename) throws IOException {
        Properties props = new Properties();
        InputStream in = SQLHelper.class.getResourceAsStream(filename);
            if (in != null) {
                props.load(in);
                in.close();
            }
        // }
        else {
            throw new IOException("Unable to resolve classpath resource '" + filename + "'");
        }
        return props;
    }
    
    private static ObjectPool initSQLConnectionPool() {
        try {
            Properties props = loadPropertiesFile(DATABASE_CONNECTION_PROPERTIES);
            String driver = props.getProperty("driver");
            String url = props.getProperty("dbUrl");
            String username = props.getProperty("username");
            String password = props.getProperty("password");
            if (props.getProperty("driver").contains("derby")) {
                derbyFlag = true;
            }
            
            PoolableObjectFactory sqlPoolableObjectFactory = new SQLPoolableObjectFactory(driver, url, username,
                    password, derbyFlag);
            Config config = new GenericObjectPool.Config();
            config.maxActive = 10;
            config.testOnBorrow = true;
            config.testWhileIdle = true;
            config.timeBetweenEvictionRunsMillis = 10000;
            config.minEvictableIdleTimeMillis = 60000;
            
            GenericObjectPoolFactory genericObjectPoolFactory = new GenericObjectPoolFactory(sqlPoolableObjectFactory,
                    config);
            ObjectPool pool = genericObjectPoolFactory.createPool();
            return pool;
        }
        catch (Exception e) {
            LOGGER.error("Could not create the SQL Pool", e);
            return null;
        }
    }

}
