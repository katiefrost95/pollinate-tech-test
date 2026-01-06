##Architectural design descisions:

Implemented based on user behaviours.

Angular standalone component

##Trade offs:

Focus on two user behaviours: create and get and completing end to end flow for those.

##Improvements:

###Backend
Persist to database
store jwt secret more securely (env secret)
For production: rotate and store jwt.secret securely (env var / secrets manager), use TLS, use a real UserDetailsService backed by DB, add token revocation/blacklist if needed, tighten allowed algorithms and claims.
for login cookie storage will set secure true in production
using cookies so less front end dev

handling of password through backend systems as plain text

disable CSRF for the API. That removes the XSRF token requirement for POSTs. - introduces security vulnerability but time.

###Frontend

Have html in own html file instead of written in templates
CORS method calling the backend - switch to a proxy or something else 
Have a date picker installed for due date of task

localstorage for saving information such as tokens or user data isn't recommended as it can pose a security threat and expose your user information

A separately hosted login page is an improvement security-wise because this way the password is never directly handled by our application code in the first place.

logout header flow

standalone components

no option to register user, very flimsy login

cache the response for production as it grows

cannot save task due today

##Running

###Prereqs

Backend:
mvn
java 21

generate a jwt secret and add to application properties
mvn clean package

http://localhost:8080/h2-console

Frontend:
node
angular cli

ng serve

