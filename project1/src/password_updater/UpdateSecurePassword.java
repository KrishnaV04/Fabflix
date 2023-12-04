package password_updater;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;


public class UpdateSecurePassword {

    /*
     *
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT id, password from customers";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString("id");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format("UPDATE customers SET password='%s' WHERE id=%s;", encryptedPassword,
                    id);
            updateQueryList.add(updateQuery);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        connection.close();

        System.out.println("finished");

    }

}

/*


To run it on AWS under command line:
1. Clone this repository using.
2. `cd cs122b-project3-encryption-example`
3. change your mysql username and password in [UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java) and [VerifyPassword.java](src/main/java/VerifyPassword.java)
4. `mvn compile`
5. Have a backup of the "customers" table, run the following:
<br>`create table customers_backup(`
   <br>`id integer auto_increment primary key,`
   <br>`firstName varchar(50) not null,`
   <br>`lastName varchar(50) not null,`
   <br>`ccId varchar(20) not null,`
   <br>`address varchar(200) not null,`
   <br>`email varchar(50) not null,`
   <br>`password varchar(20) not null,`
   <br>`foreign key(ccId) references creditcards(id));`
<br>`insert into customers_backup select * from customers;`
6. to run `UpdateSecurePassword`:
   <br>`mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=UpdateSecurePassword`
7. to run `VerifyPassword`:
   <br>`mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=VerifyPassword`
8. When execute java program using maven in command line, if the program doesn't exist after it finishes, you can just kill it.
9. To recover the data in the "customers" table, run the following:
   <br>`update customers C1 set password = (select password from customers_backup C2 where C2.id = C1.id);`


 */
