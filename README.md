# DBMS-GUI


The overview of project structure:

- Backend: manipulate the connection and result from Mircosoft SQL Server
  - DataGetter: directly get some general data, eg. table names in the database
  - Metrics: Metric class for each Data Quality Dimensions 
  - ObjectClass: customized object class
  - Reference: decide how to manipulate CSV data
  
- Frontend: handle the interaction between Backend and the users' requests

Functionality:

The user will start the programm with this page:
 
 ![alt text](https://raw.githubusercontent.com/Ryuuko/DBMS-GUI/master/Images/LoginPage.png?token=AGAODQPZU3BHR2ZV76EXZXC636XZW)
 
After they have successfully logged in, they will reach the metrics page:

 ![alt text](https://raw.githubusercontent.com/Ryuuko/DBMS-GUI/master/Images/MetricsPage.png?token=AGAODQIJO6ZJJM3XND47O62636YGQ)
 
The small window will display the result of the Metrics:

 ![alt text](https://raw.githubusercontent.com/Ryuuko/DBMS-GUI/master/Images/smallResult.png?token=AGAODQMQ7KQ4PXGI2QR6332636YJI)
 
It's possible to compare results with the enlarged function

![alt text](https://raw.githubusercontent.com/Ryuuko/DBMS-GUI/master/Images/enlargedResult.png?token=AGAODQP3ICDZ76O2P7HLVAC636YIS)

Comments on other components:
- target/Data_Quality_Metrics-1.0.jar is the programm itself
- DBMS-GUI/src/main/java/Main.java provides a "Dummy" Main.class in order to implement JavaFx in jar.File
