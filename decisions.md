## Architectural and design decisions

### Frontend

Organise the frontend into standalone components.

Architecture: 

Auth component which provides
* /register (creates user)
* /login (generates JWT token stored as cookie by backend)
* /logout (clears JWT token)

Tasks component which provides
* /task endpoints protected by an auth guard requiring the user to be authenticated.

Four components:
1. Task form component (main one for creating and listing tasks)
2. Login component
3. Logout component
4. Alert component for error handling

### Backend

Organise the backend into controllers, services and repositories for handling data flow.

Endpoints:

Two endpoints for Authentication (Logout handled in Spring security config):
1. /Register (saves user in db users table)
2. /Login (generates jwt token and stores as cookie)

Four endpoints for Tasks. Leveraging Spring security to handle a lot of the authentication allowing access to these endpoints:

1. GET /tasks
2. POST /tasks
3. PUT /tasks/{id}
4. DELET /tasks/{id}

CORS to restrict origins.

Cookies to store the jwt token to avoid sending the token back in the response and using localstorage. Approach also requires less code in front end.

H2 database to store users and tasks for ease.

Since the application is small I used a single TaskRequest and AuthRequest model which doubles up as the @Entity class, as opposed to have two separate 


## Trade offs made due to time 

### Error handling

A trade off I made due to time was the error handling across the front end and the backend. Currently the error handling is vague and provides minimal coverage. 

If I had more time/for a production system I would use Global Exception Handling in the backend with the @ControllerAdvice annotation to create a more uniform and consistent error handling strategy, using custom exceptions where appropriate, to forward more meaningful error messages to the front end to be displayed to the user.

### Environment variables / secrets management

Currently I have hardcoded quite a lot of variables in code and exposed secrets. In a real production system I would I store secrets and environment variables in a cloud management service.

### Cookie vulnerability

Tried to enable csrf protection but was encountering errors which was taking up a lot of time. Decided to disable this for development but would implement in prod.

## Improvements for production

Frontend:
* Split out the Task Form into two separate components - one for the task form view and one for the task list view.
* Scalability improvements such as caching results and adding pagination for getTasks. 
* Retry policies for HTTP requests.
* A generally nicer UI for an improved user journey/experience particularly around the login/registering componenet.
* Improved error handling

Backend:
* Utilise a more robust database for storing user and task tables and put indexes on owner.
* Rotate and store the JWT secret securely.
* Cookie security - in the backend I would set cookies parameters secure=true (https) and enable csrf (Cross-Site Request Forgery) protection for production.
* Configure JWT token refreshing so the user doesn't have to reauthenticate every time it expires. (JWT token also currently expires after a month so would reduce that to a few hours)
* Improved error handling and logging.






