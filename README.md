# Web-service-recommender
The concept of Web services has become a widely applied paradigm in research and industry,
with the number of services published on the Internet increasing rapidly over the last few 
years. The rapid increase has brought forth challenges in terms of large data which leads to 
time consuming expensive computation in-order to find services for recommendation. 
To overcome these challenges, many efficient mechanisms of web service discovery in response 
to user's request have been proposed in order to leverage web service selection process. 

### Implementation
The Dataset used for implementation is a freely available dataset provided by Zhang et al. (https://github.com/wsdream/wsdream-dataset). 
It consists of 142 users and 4500 web services with each web service being invoked at 64 time-intervals by every user. 
The non-functional parameters such as response time and throughput are considered for web service recommendation 
in our project. 

### Data preprocessing
On studying the dataset, certain pre-processing steps were carried out which involved removing duplicates 
from Web Service Response Time information and Web Service Throughput information. The web service information 
was incorporated with an additional column 'category'. It is used to categorize the web services based on its functionality. 
The need for this column arose as we were not able to filter the web services based on the functional requirements of the user. 
In addition to that, web service information file was converted to UTF-8 from ISO-8859 format 
for loading in PostgreSQL database using a python procedure. 

### Methodology
After preprocessing, the dataset is loaded into PostgreSQL database. 
The application, initially, records the user location and desired functionality. 
Now, based on the location entered, neighboring users are selected from the user information table. 
Also,based on the  desired functionality, candidate web services are generated. 
In order to recommend a web service efficiently from the candidate web services, 
location plays an important role as users of different location will have different 
experience(Quality-of-service values) for the same web service. After considering the location 
factor, it also becomes important to consider the temporal information as the web services used 
in the past will not necessarily provide the same quality of service. Thus considering location
and time as the two main factors, a weight matrix is calculated between two users for the same 
location using Pearson Correlation Coefficient. Response time and throughput are considered as 
QoS parameters in our implementation.


After the QoS values are predicted, recommendation for optimal web service is done using multi-criteria decision making technique \cite{hdioud2013multi}. The top-3 web services are recommended to the user.

-- The implementation is done in Java and uses Jama library for Matrix related computations. 
-- The back end is implemented using PostgreSQL database. 
-- A graphical user interface is developed using Swing to provide user-friendly experience. 

### Setup

#### BUILD OUTPUT DESCRIPTION
------------------------

When you build an Java application project that has a main class, the IDE
automatically copies all of the JAR
files on the projects classpath to your projects dist/lib folder. The IDE
also adds each of the JAR files to the Class-Path element in the application
JAR files manifest file (MANIFEST.MF).

To run the project from the command line, go to the dist folder and
type the following:

java -jar "timeWSR_CF.jar" 

Notes:

* If two JAR files on the project classpath have the same name, only the first
JAR file is copied to the lib folder.
* Only JAR files are copied to the lib folder.
If the classpath contains other types of files or folders, these files (folders)
are not copied.
* If a library on the projects classpath also has a Class-Path element
specified in the manifest,the content of the Class-Path element has to be on
the projects runtime path.
* To set a main class in a standard Java project, right-click the project node
in the Projects window and choose Properties. Then click Run and enter the
class name in the Main Class field. Alternatively, you can manually type the
class name in the manifest Main-Class element.

