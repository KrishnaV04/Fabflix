- # General
    - #### Team#: 2023-fall-cs122b-mango
    
    - #### Names: Raymond, Radhakrishna
    
    - #### Project 5 Video Demo Link: [https://drive.google.com/file/d/1S4QXT324b7siZw6OeEe6XEDrIgOODeeu/view?usp=sharing](https://drive.google.com/file/d/1S4QXT324b7siZw6OeEe6XEDrIgOODeeu/view?usp=sharing)

    - #### Instruction of deployment:
    - Clone the above repo, and run the createtable.sql storedprocedures.sql and movie-data.sql then execute the XML parser to parse through all data and store standford movie data onto the mysql database that must be avaialable on your computer. Then run ```mvn pacakge``` to build .war file and test out the website using the format ```http//<ip>:<port>/cs122b-project1-api-example/login.html``` note if you want to go to the load balancer and test our the master-slave replication use the load balancer ip address with the port ```80``` instead of the port ```8080```.

    - #### Collaborations and Work Distribution: Raymond - JMeter time Analysis Task 4 and Fabflix Scaling with LoadBalancer and MySQL; Radhakrishna - JDBC Connection Pooling, Master Slave Replication and LoadBalancer and Internal MySQL configuration


- # Connection Pooling
  
    **Prepared Statement Locations:**
    -  #### project1/src/EmployeeLoginServlet.java
    -  #### project1/src/LoginServlet.java
    -  #### project1/src/MovieAutocomplete.java
    -  #### project1/src/MovieListGenreServlet.java
    -  #### project1/src/MovieListTitleServlet.java
    -  #### project1/src/MovieListSearchServlet.java
    -  #### project1/src/SingleMovieServlet.java
    -  #### project1/src/SingleStarServlet.java

    - #### Connection Pooling Info: ```/project1/WebContent/META-INF/content.xml```
    
    - #### How Connection Pooling Works:
    - Connection pooling optimizes the utilization of connection resources to our MySQL server by efficiently reusing existing connections. This approach minimizes the processing time required for establishing entirely new connections. In our codebase, every servlet utilizing JDBC to interact with the database relies on the resource configuration specified in the context.xml file to establish connections. The content file, located at the specified path, contains details confirming the successful reuse of connections within our application.
    
    - #### How Connection Pooling works with two backend SQL:
    - Connection pooling is implemented efficiently with two backend SQL servers in our setup. Each database has its dedicated connection pool, ensuring that the workload is evenly distributed. When requests are processed through a load balancer, it intelligently directs them to the appropriate server or pool based on the nature of the request. This approach enhances efficiency by distributing the workload across two servers while still benefiting from connection pooling to minimize the creation of new connections. In essence, the load balancer facilitates the reuse of connections within the two backend servers through the utilization of Connection Pool technology. This strategy contributes to improved performance and resource utilization in our database interactions.
    

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - ```/project1/WebContent/META-INF/content.xml```
    - ```/project1/master_content.xml```
    - ```/project1/slave_content.xml```

    - #### How read/write requests were routed to Master/Slave SQL:
    - From the load balancer read requests can be sent to either the Master or the Slave, and when received the slave/master servers will use their content.xml to connect to their local database and execute the respective query. However in the instance that write requests are given to the master or the slave the master will continue to go to its local database using a connection from the connection pool if possible and the slave will connect to the master's database using the same concept however it is worth noting that the connection pools are seperate for both servers hence each one must make its own connection independent to the queries being sent to the other server.
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs:
    - The log processing scrip is located at 2023-fall-cs122b-mango/project1/logParser.py
    - It processes the script by reading from the log file which gets created when server receives requests and calculates the averages for TS and TJ. To run it we can use python3 logParser.py in the directory to get to the parsed outputs.
    - Note: all log files are located in the folder ```/project1/project5-logs/```


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![image1](project1/img/single-case-1.png)   | 92                         | 45.7272                             | 45.5265                   | In the scenario of a single AWS instance using connection pooling with HTTP/1 thread, it's notable that this configuration exhibits the smallest Average Query Time among all single-instance JMeter tests. This reduced query time can be attributed to the minimal stress factor on the instance due to the presence of only one thread. An intriguing observation lies in the marginal difference between Average Search Servlet Time and Average JDBC Time. This negligible time difference suggests that the bulk of the workload stems from JDBC tasks, indicating that the servlet execution time is almost exclusively dedicated to JDBC-related operations. This reiterates the efficiency of the connection pooling mechanism in handling the JDBC tasks with minimal overhead on servlet execution.|
| Case 2: HTTP/10 threads                        | ![image2](project1/img/single-case-2.png)   | 195                        | 117.9934                            | 117.8242                  | In the scenario of running HTTP/10 threads on a single AWS instance with connection pooling enabled, the Average Query Time exhibited a little more than a doubling from the HTTP/1 thread test, correlating with the heightened stress on the system due to concurrent threads. Notably, both the Average Search Servlet Time and Average JDBC Time remained closely aligned, indicating that the JDBC tasks predominantly influenced the overall performance. The inclusion of connection pooling mitigated the impact of increased loads, maintaining relatively stable response times. This integration notably enhanced efficiency by reusing connections, improving throughput and stability in handling multiple simultaneous requests. The pooled connections facilitated consistent response times, underscoring their role in optimizing resource utilization and database operations amidst heightened concurrency.           |
| Case 3: HTTPS/10 threads                       | ![image3](project1/img/single-case-3.png)   | 198                        | 120.1932                            | 119.9562                  | In the case of running HTTPS/10 threads on a single AWS instance, the observed slight increase in all metrics—Average Query Time, Average Search Servlet Time, and Average JDBC Time—compared to the HTTP counterpart aligns with anticipated results. This marginal elevation in response times is typical due to the additional encryption and decryption processes inherent in HTTPS communication. The encryption overhead adds a fraction of latency to each request, contributing to the modest rise in overall query execution times. Despite these slightly prolonged durations, the relative performance patterns remain consistent, highlighting the influence of encryption overhead on response times in secure communications.           |
| Case 4: HTTP/10 threads/No connection pooling  | ![image4](project1/img/single-case-4.png)   | 177                        | 101.1257                            | 87.0488                   | In the scenario of HTTP/10 threads without connection pooling, an unexpected observation emerged: the overall times were marginally quicker compared to the setup utilizing connection pooling. This unusual occurrence contradicts the common understanding that connection pooling optimizes resource utilization and enhances performance by reusing connections. One potential explanation could be the specific instance characteristics or configuration intricacies within the AWS EC2 environment. It should also be noted that running the JMeter tests during different periods of time had the timings of these tests adjust mildly, which could also be an explanation.|

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![image5](project1/img/scaled-case-1.png)   | 85                         | 7.4323                              | 7.2043                    | In a scaled setup with a Load Balancer and multiple instances handling the requests, observing lower metrics across the board, especially in Average Search Servlet Time and Average JDBC Time, can be attributed to the distributed workload. With multiple instances, the system shares the processing load, resulting in reduced individual stress on each server. This distribution minimizes contention for resources, optimizing the efficiency of JDBC tasks and servlet handling. Consequently, the lower times reflect the improved performance achieved through workload balancing among the instances.|
| Case 2: HTTP/10 threads                        | ![image6](project1/img/scaled-case-2.png)   | 121                        | 43.8831                             | 43.5584                   | In the scenario with HTTP/10 threads on the scaled architecture, similar to the single-thread case, we see reduced metrics compared to the non-scaled setup. This improvement in all metrics, especially in Search Servlet and JDBC times, is consistent with the distributed workload, benefiting from multiple instances handling the requests concurrently. The workload distribution across the instances effectively reduces the stress on individual servers, resulting in enhanced overall performance.|
| Case 3: HTTP/10 threads/No connection pooling  | ![image7](project1/img/scaled-case-3.png)   | 167                        | 56.4047                             | 56.1073                   | In the scenario with HTTP/10 threads and no connection pooling on the scaled architecture, the metrics are notably higher compared to the other scenarios within the scaled setup. The absence of connection pooling exacerbates the load on each instance, causing increased Average Query, Search Servlet, and JDBC times. With no connection pooling, each thread requires individual connections, leading to higher latency and increased resource utilization on each server, resulting in slower response times overall.|

- ## Overall Report Remarks:
- In our project, we employed JMeter to conduct load testing on both our load balancer server and a conventional website server, employing distinct thread groups and utilizing varied protocols, including HTTP and HTTPS. It has come to my attention that, upon running JMeter at different instances, there is a notable variance in the collected data. This phenomenon is attributed to the inherent dynamism of the AWS instance, which, when subjected to varying loads or stresses, may influence the performance of our servers.

- The output presented in the aforementioned tables is considered relatively accurate, bearing in mind the dynamic nature of the AWS infrastructure. However, it should be acknowledged that the data's absolute accuracy may be subject to fluctuations depending on the varying load conditions imposed on the AWS instance.

- Additionally, an observation surfaced during the analysis phase, revealing a similarity in query times, as well as times associated (TS) and (TJ). Contrary to our initial expectations, where the incorporation of a load balancer and connection pooling was anticipated to yield significant improvements in processing speed, the observed marginal impact could be indicative of enhancements occurring on a smaller scale. This realization may elucidate the observed data patterns.


