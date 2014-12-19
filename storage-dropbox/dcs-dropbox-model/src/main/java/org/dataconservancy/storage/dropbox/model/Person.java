/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.storage.dropbox.model;

import java.io.Serializable;

public class Person implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private String occupation;
    private String organization;
    private String username;
    private String password;
    private String secretQuestion;
    private String secretAnswer;
    private String email;
    private String prefix;
    private String middleName;
    private String suffix;
    
    public Person() {
    }
    
    public Person(String firstName, String lastName, String occupation, String organization, String username,
            String password, String secretQuestion, String secretAnswer, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.occupation = occupation;
        this.organization = organization;
        this.username = username;
        this.password = password;
        this.secretQuestion = secretQuestion;
        this.secretAnswer = secretAnswer;
        this.email = email;
    }
    
    /**
     * Creates a new Person {@link #equals(Object) equal} to {@code toCopy}.
     * 
     * @param toCopy
     *            the Person to copy, must not be {@code null}
     * @throws IllegalArgumentException
     *             if {@code toCopy} is {@code null}.
     */
    public Person(Person toCopy) {
        this.firstName = toCopy.getFirstName();
        this.lastName = toCopy.getLastName();
        this.middleName = toCopy.getMiddleName();
        this.prefix = toCopy.getPrefix();
        this.suffix = toCopy.getSuffix();
        this.email = toCopy.getEmail();
        this.occupation = toCopy.getOccupation();
        this.organization = toCopy.getOrganization();
        this.secretQuestion = toCopy.getSecretQuestion();
        this.secretAnswer = toCopy.getSecretAnswer();
        this.username = toCopy.getUsername();
        this.password = toCopy.getPassword();
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * @return the occupation
     */
    public String getOccupation() {
        return occupation;
    }
    
    /**
     * @param occupation
     *            the occupation to set
     */
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    
    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }
    
    /**
     * @param organization
     *            the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return the secretQuestion
     */
    public String getSecretQuestion() {
        return secretQuestion;
    }
    
    /**
     * @param secretQuestion
     *            the secretQuestion to set
     */
    public void setSecretQuestion(String secretQuestion) {
        this.secretQuestion = secretQuestion;
    }
    
    /**
     * @return the secretAnswer
     */
    public String getSecretAnswer() {
        return secretAnswer;
    }
    
    /**
     * @param secretAnswer
     *            the secretAnswer to set
     */
    public void setSecretAnswer(String secretAnswer) {
        this.secretAnswer = secretAnswer;
    }
    
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * @param prefix
     *            the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }
    
    /**
     * @param middleName
     *            the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    /**
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }
    
    /**
     * @param suffix
     *            the suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof Person)) {
            return false;
        }

        Person other = (Person) obj;

        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equalsIgnoreCase(other.getPrefix())) {
            return false;
        }
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        }
        if (middleName == null) {
            if (other.middleName != null) {
                return false;
            }
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        }
        if (suffix == null) {
            if (suffix != null) {
                return false;
            }
        } else if (!suffix.equalsIgnoreCase(other.getSuffix())) {
            return false;
        }
        if (occupation == null) {
            if (occupation != null) {
                return false;
            }
        } else if (!occupation.equalsIgnoreCase(other.getOccupation())) {
            return false;
        }
        if (organization == null) {
            if (organization != null) {
                return false;
            }
        } else if (!organization.equalsIgnoreCase(other.getOrganization())) {
            return false;
        }
        if (secretQuestion == null) {
            if (secretQuestion != null) {
                return false;
            }
        }
        else if (!secretQuestion.equalsIgnoreCase(other.getSecretQuestion())) {
            return false;
        }
        if (secretAnswer == null) {
            if (secretAnswer != null) {
                return false;
            }
        }
        else if (!secretAnswer.equalsIgnoreCase(other.getSecretAnswer())) {
            return false;
        }
        if (email == null) {
            if (email != null) {
                return false;
            }
        } else if (!email.equalsIgnoreCase(other.getEmail())) {
            return false;
        }
        if (password == null) {
            if (password != null) {
                return false;
            }
        } else if (!password.equalsIgnoreCase(other.getPassword())) {
            return false;
        }

        return true;
    }
    
    @Override
    public String toString() {
        return "Person{" + "firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", middleName='"
                + middleName + '\'' + ", prefix='" + prefix + '\'' + ", suffix='" + suffix + '\'' + ", username='"
                + username + '\'' + ", password='" + password + '\'' + ", email='" + email + '\'' + ", occupation='"
                + occupation + '\'' + ", organization='" + organization + '\'' + ", secretQuestion='" + secretQuestion
                + '\'' + ", secretAnswer='" + secretAnswer + '}';
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        if (firstName == null) {
            result = prime * result;
        }
        if (middleName == null) {
            result = prime * result;
        }
        if (lastName == null) {
            result = prime * result;
        }
        
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        
        result = prime * result + ((occupation == null) ? 0 : occupation.hashCode());
        
        result = prime * result + ((organization == null) ? 0 : organization.hashCode());
        
        result = prime * result + ((secretQuestion == null) ? 0 : secretQuestion.hashCode());
        
        result = prime * result + ((secretAnswer == null) ? 0 : secretAnswer.hashCode());

        result = prime * result + ((username == null) ? 0 : username.hashCode());
        
        result = prime * result + ((password == null) ? 0 : password.hashCode());

        return result;
    }

}
