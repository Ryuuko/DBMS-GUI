package com.DBMS.Backend.ObjectClass;

public final class LoginParameter {
    private String hostname;
    private String databaseName;
    private String userName;
    private String password;

    public LoginParameter(String hostnameInput, String databaseNameInput,
                          String userNameInput, String passwordInput) {
        this.hostname = hostnameInput;
        this.databaseName = databaseNameInput;
        this.userName = userNameInput;
        this.password = passwordInput;

    }

    public String getHostname() {
        return hostname;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
