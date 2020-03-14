package main.java;
/*extra Main class as the entrance to connect to the actual Main class
 * specific only for the JavaFX's case
 * see:
 * https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle
 * */

import com.DBMS.Frontend.LoginPage;

public class Main {
    public static void main(String[] args) {
        LoginPage.main(args);
    }
}
