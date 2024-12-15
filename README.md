# Cleaning Scheduler
## Description
This application will make sure you never have to all of your cleaning at once. All you need to do is add some tasks, specify the availability of the cleaners and the application will provide you with an optimal cleaning schedule for the next weeks. It will balance the cleaning tasks considering the availability of the cleaners for each week, so that they will have more time to enjoy their weekends. 

You can:
- Specify the preferred cleaner for a task
- Let the scheduler know when the task was last done
- Repeat a task every so often
  - You can even specify a range in which the task needs to be repeated. That way the scheduler can sometimes shuffle tasks so that the load is evenly balanced.

In this README you can find out these how to install the scheduler and how to use it, what software is used and some other information about this project.

- [Cleaning Scheduler](#cleaning-scheduler)
  - [Description](#description)
  - [Installation](#installation)
    - [Backend Server](#backend-server)
    - [Frontend Server](#frontend-server)
    - [Database](#database)
  - [Usage](#usage)
    - [People](#people)
      - [Overview](#overview)
    - [Add a person](#add-a-person)
    - [Change a person](#change-a-person)
    - [Delete a person](#delete-a-person)
    - [Tasks](#tasks)
      - [Overview](#overview-1)
      - [Add a task](#add-a-task)
      - [Change a task](#change-a-task)
      - [Delete a task](#delete-a-task)
    - [Schedules](#schedules)
      - [Overview](#overview-2)
      - [Scoring and Scheduling](#scoring-and-scheduling)
      - [Create a new schedule](#create-a-new-schedule)
      - [Reschedule with locked tasks](#reschedule-with-locked-tasks)
      - [Delete a schedule](#delete-a-schedule)
  - [Software Stack](#software-stack)
  - [Personal Goals](#personal-goals)
  - [Technical Goals](#technical-goals)
  - [Requirements](#requirements)
  - [Authors and acknowledgment](#authors-and-acknowledgment)

## Installation
To get the application up and running, you need to set up the [backend server](#backend-server), the [frontend server](#frontend-server) and the [database](#database). 

For this, make sure that Java 21 is installed. See these instructions for Ubuntu:
https://www.rosehosting.com/blog/how-to-install-java-21-on-ubuntu-24-04/

**Important**: You need to run both the backend server and the frontend server to have a working application. This can be done using two different terminals and the commands described in the relevant sections.

### Backend Server
To get the backend server up and running, follow these instructions:
1. Make sure you are in the directory ```cleaning-directory```
2. Build the project:
```bash
mvn install
```
3. Run the jetty server:
```bash
mvn exec:java
```

**Note:** If you have already built the project previously, then you can skip step 2 and directly go to step 3.

Some extra commands that might come in handy:
- To do step 2 and 3 at once: 
```bash
mvn install exec:java
```
- To completely rebuild the project and do step 2/3:
```bash
mvn clean install exec:java
```


### Frontend Server
To get the frontend server up and running we use ```npm```. To install this using ```nvm```, follow these instructions: https://medium.com/@imvinojanv/how-to-install-node-js-and-npm-using-node-version-manager-nvm-143165b16ce1

Now
1. Navigate to the client directory of this project:
```bash
cd <path-to-directory>/cleaning-scheduler/client
```
2. Install all dependencies defined in package.json using the following command:
```bash
npm install
```
3. Now you can use the following commands from the ```client``` directory to run, check and build the frontend:
```bash
# Start the front-end server
npm run dev
# Check code for common mistakes and style conventions
npm run lint
# Create a production-worthy build of the client
npm run build
```
4. As soon as you have run the front-end server, you can find the application at http://localhost:5173/.

### Database
If you have already installed mysql server and have access to a user with sufficient rights, go to step 5.    

1. Install the mysql server:
     - Ubuntu: https://ubuntu.com/server/docs/install-and-configure-a-mysql-server
     - Windows: https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html
     - MacOS: https://dev.mysql.com/doc/refman/8.4/en/macos-installation.html
     - General installation guides: https://dev.mysql.com/doc/refman/8.4/en/installing.html
2. After installing the mysql server, start as the root using the password chosen during installation:
```bash
mysql -u root -p
```
3. Add a new user called ```bo``` identified by the password ```password``` with all privileges. The following commands give the user all privileges on all databases, change them accordingly if you prefer different privileges:
```bash
mysql> CREATE USER 'bo'@'localhost' IDENTIFIED BY 'password';
mysql> GRANT ALL PRIVILEGES ON *.* TO 'bo'@'localhost';
mysql> FLUSH PRIVILEGES;
mysql> exit;
```
4. Now navigate to the ```cleaning-scheduler``` directive and start mysql using your new user:
```bash
cd <path-to-directory>/cleaning-scheduler
mysql -u <username> -p
``` 
5. Set up the database using:
```bash
mysql> source persistence/db-init/init-db.sql
```

Troubleshooting:
- If you use the non-default mysql port, you might need to change TODO to the correct port. To find out what port you are using start mysql and use the following command:
```bash
mysql> SHOW VARIABLES LIKE 'port';
```

## Usage
### People
In the section "Add/Edit People", you can find an overview of all people that are currently considered by the scheduler. You can also delete, add or change people from this section.

#### Overview
On the right side of the page you will find an overview of all people that the scheduler currently knows about. By clicking on one of the people you can find some more information. See the following section for the properties a person has.

### Add a person
To add a person you need to provide the following information:
- Name
- Availability in minutes for each week that you want to schedule for. (If no value is given for a week, it is assumed that availability is 0 minutes.)

### Change a person
To change a person, click on the corresponding button and alter the relevant information. Remember to save the changes.

### Delete a person
To delete a person, click on the corresponding button. A person can only be deleted if there are no more tasks with the person as preferred assignee, and there are no more schedules that assign any tasks to this person. So first delete these tasks and schedules to allow for the deleting of this person.
Note that deleting is permanent and cannot be reverted.

### Tasks
In the section "Add/Edit Task", you can find an overview of all tasks that are currently considered by the scheduler. You can also delete, add or change tasks from this section

#### Overview
On the right side of the page you will find an overview of all tasks that the scheduler currently knows about. By clicking on one of the tasks you can find some more information. See the following section for the properties a task has.

#### Add a task
To add a task you need to provide the following information:
- Name
- Costs in minutes
- A preferred assignee for this task
- Whether this preference should be strict. (If this option is selected, the task will also be assigned to this person if this makes the schedule suboptimal)
- What week the task was last done at. (If it has never been done before, just select a random week far outside your intended scheduling range.)
- If the task should be done every so often or just once.
- If the task repeats, what the minimal amount of weeks between two repetitions should be.
- If the task repeats, what the maximal amount of weeks between two repetitions should be.

#### Change a task
To change a task, click on the corresponding button and alter the relevant information. Remember to save the changes.

#### Delete a task
To delete a task, click on the corresponding button. A task can only be deleted if there are no more schedules that assign this task. So first delete these schedules to allow for the deleting of this task.
Note that deleting is permanent and cannot be reverted.

### Schedules
In the section "Schedules", you can find all schedules that have been created until now. Furthermore, you can also create a new schedule using the people and tasks explained above.

#### Overview
Schedules are identified by the date at which the schedule was created. Furthermore, each schedule has a score assigned to it, which can also be found in the overview. (For more on the scoring, see the next section.) Finally, you can see the actual schedule and see what task is assigned to which person each week. You can also see here how many minutes each task takes.

#### Scoring and Scheduling
Currently, the scoring function and scheduling algorithm are a well-kept secret.

#### Create a new schedule
You can create a new schedule by clicking on the "New Schedule" button. A prompt will be opened asking what week you would like the schedule to begin, and for how many weeks you would like to make a schedule. By clicking "Schedule" a new schedule will be created and shown. Note that it might take up to 15 seconds to create the new schedule.

#### Reschedule with locked tasks
Currently, this feature is not supported.

#### Delete a schedule
To delete a schedule, click on the corresponding button.
Note that deleting is permanent and cannot be reverted.

## Software Stack
This project uses a variety of different frameworks and languages, and has been designed as a layered monolith. The domain was developed using Test Driven Development and currently has a code coverage of:
![Coverage](https://git.sogyo.nl/bflachs/cleaning-scheduler/badges/main/coverage.svg)

A list of the layers and the technologies used:
- Client layer:
  - Framework: React + TypeScript
  - Styling: TailwindCSS
  - Server: Vite
- API Layer:
  - Language: Java
  - Server: Jetty 
  - Tests: JUnit
- Domain Layer:
  - Language: Scala
  - Tests: ScalaTest
- Persistence Layer:
  - Language: Java
  - Database: MySQL 

## Personal Goals
For this project I had the following personal goals:
-  Don't think too much about the project during personal time.
-  Use a scheduling tool to keep an overview, rather than scheduling everything in my head.
-  Work in clear focused blocks to prevent falling down a rabbit-hole. 

## Technical Goals
For this project I had the following technical goals:
- Develop a functional style domain in Scala
- Develop using vertical slicing, i.e., add one feature to all layers before starting on the next feature. 
- Get more comfortable with complex plain SQL queries

## Requirements
The requirements for this project are defined using the [MoSCoW method](https://en.wikipedia.org/wiki/MoSCoW_method).

**Must**:
- Save tasks in the database
- Save people in the database
- Schedule with a maximum number of tasks per week
- View the schedule from the client
- Reschedule from the UI
- Schedule for 2 people

**Should**:
- Add/edit/remove tasks from the UI
- Schedule with a maximum number of hours per person per week
- Save the schedule in the database
- Export the schedule to .txt

**Could**:
- Schedule for more than 2 users
- Reschedule with locked tasks
- Add example tasks that can be easily added from UI
- Export to .pdf

**Won't**:
- Communicate over the web with other instances of the application
- Schedule tasks per day
- Export tasks to .iCal

## Authors and acknowledgment
This project has been built by Bo Flachs during a software engineering traineeship at Sogyo.

