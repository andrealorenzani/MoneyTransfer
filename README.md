# MoneyTransfer
This is the solution for the challenge. I made the following assumptions and took the following decisions:
 * The point was not the algorithm, but the technologies and the way in which all of them were plug togheter
 * The in-memory datastore should just contain the informations needed for the test (in my case users with their availability of money)
 * The solution has to be easy. I was thinking about what happen if one of the user from/to which the money has to be transferred is not in our company. This requires an external call, or checks or... and those made the solution complicated
 * There are several things that were not needed: how to create the in-memory store and check it. I could have read it from a file. But I decided to add API for adding users, changing their availability and check their money
 * I avoided things completely out of scope (removing users...)
 * I didn't write automatic tests for whatever was not required
 * I wrote the code keeping in mind scalability, expandibility and keeping everything at its best
 
## Architecture
For the language I chose **Scala**, because I love it, I know you use it, I work with Java and I enjoy using a language that I currently cannot use on daily bases.
I don't really like using huge libraries for small projects. I needed to start an application server without a container and without Spring (that, anyway, is huge). I could have coded a Netty-based solution, but I preferred **Finagle** because it is cleaner to code for a basic usage.
The server accepts connection on the port 8080. Then it passes the request to a **RestService** that dispatch the request based on the method of the request itself:
 * *GET* is usually used to retrieve data. I used it to retrieve a **User** by id, all the users or the number of users currently stored in the database
 * *POST* is usually used to create new data. I used it to create a new **User**. After the creation, the user has zero as availability of money
 * *DELETE* is for delete data, but I didn't need it (as all the other methods not mentioned here)
 * *PUT* is used for updating, so I used it for transferring money from two users and for adding money to an user
 
My *RestService* delegates to two handlers the management of the requests: a **UserHandler** for everything connected to a *User*, and a **TransferHandler** for adding money to an *User* or transferring it between two *User*s.
The Api is driven by the querystring. Check the following section for knowing how it works. The response of the Api is usually in JSON format, a valid format if (as written in the test spec) we are working for an internal component that is invoked by other internal components.
In case of error the Api replies with an error message (I could have easily put that in a JSON).

### Pros
All classes require the external objects to be injected. To keep it simple I avoided to use an injection library, and I used factory objects (like RequestHandler object or DataStore object).
Handlers (**TransferHandler** and **UserHandler**) implements the trait **RequestHandler** that lets you just define routes and handling of the request, giving back error handling and abstracting from the implementation. It is very easy to modify RestService for taking a RequestHandler per each method and completely generalise from the project.
The same for the **DataStore**. It defines the required methods so that we can easily plug another DataStore in the project.
Everything is (unit)tested.

### Cons
I had no time to setup an integration test with cucumber. Tomorrow I am not at home and I will do it not before Monday.

## Usage
To start the server:
 * sbt run
 
To run unit tests
 * sbt test
 
Please note that integration tests (Cucumber) are provided, but in my environment I use a plugin for running them.
 
To call the API you can use CURL like that
 * Give the number of the users in the store: *curl -v http://localhost:8080/size*
 * Return all the users in the store: *curl -v http://localhost:8080/*
 * Add a user in the store: *curl -v --request POST --data '' http://localhost:8080/user/_name_/_id_*
 * Retrieve user with *id* from the store: *curl -v http://localhost:8080/user/_id_*
 * Add money to an user: *curl -v --request PUT --data '' http://localhost:8080/add/_id_/_money_*
 * **Transfer money**: *curl -v --request PUT http://localhost:8080/move/_idSource_/_money_/_idDest_*

I look forward for your evaluation
