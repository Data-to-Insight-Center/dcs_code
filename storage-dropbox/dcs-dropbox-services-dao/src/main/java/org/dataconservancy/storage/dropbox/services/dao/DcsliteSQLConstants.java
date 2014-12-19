/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.storage.dropbox.services.dao;

public interface DcsliteSQLConstants {

    public static final String INSERT_USER = "INSERT INTO users (username,password,app_key,app_secret,first_name,last_name,occupation,organization,secret_question,secret_answer,email) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_USER = "UPDATE users SET app_key = ?, app_secret = ? WHERE username = ?";

    public static final String UPDATE_SESSION_ID = "UPDATE users SET sid = ? WHERE username = ?";

    public static final String CHECK_USER = "SELECT username FROM users WHERE username = ?";

    public static final String AUTHENTICATE = "SELECT username, password FROM users WHERE username = ? OR email = ?";

    public static final String RETRIEVE_TOKENS = "SELECT app_key, app_secret FROM users WHERE username = ?";

    public static final String CHECK_SESSION_ID = "SELECT username FROM users WHERE sid = ?";

    public static final String GET_USERNAME = "SELECT username FROM users WHERE email = ?";

    public static final String VALIDATE_EMAIL_ANSWER = "SELECT username FROM users WHERE email = ? AND secret_answer = ?";

    public static final String VALIDATE_EMAIL = "SELECT username FROM users WHERE email = ?";

    public static final String RETRIEVE_SECRET_QUESTION = "SELECT secret_question FROM users WHERE email = ?";

    public static final String RESET_PASSWORD = "UPDATE users SET password = ? WHERE username = ?";
    
    public static final String SET_SESSION_ID_TO_NULL = "UPDATE users SET sid = NULL WHERE username = ?";
    
    public static final String CREATE_TABLE = "CREATE TABLE USERS (id INT GENERATED ALWAYS AS IDENTITY,username VARCHAR(1024) NOT NULL,password VARCHAR(1024) NOT NULL,app_key VARCHAR(1024),app_secret VARCHAR(1024),first_name VARCHAR(1024),last_name VARCHAR(1024),occupation VARCHAR(1024),organization VARCHAR(1024),secret_question VARCHAR(1024),secret_answer VARCHAR(1024),email VARCHAR(1024),sid VARCHAR(1024),PRIMARY KEY (id))";
}
