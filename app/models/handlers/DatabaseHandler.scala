package models.handlers

import models.database.{Activity, Trip, User}
import models.input.UserFriend

import javax.inject._
import play.api.Configuration
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase, Session}

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala

class DatabaseHandler @Inject() (config: Configuration) {
  private val driver: Driver = GraphDatabase.driver(config.get[String]("NEO4J_URI"), AuthTokens.basic(config.get[String]("NEO4J_USERNAME"), config.get[String]("NEO4J_PASSWORD")))
  private val session: Session = driver.session

  /*
   * Runs a script and returns the entire response
   *
   * @param script: String = The script that will be run
   *
   * @return String = The string response from Neo4j
   */
  private def runScriptConsume(script: String): String = {
    val result = session.run(script)
    result.consume().toString
  }

  /*
   * Runs a script and returns a single map containing some data
   *
   * @param script: String = The script that will be run
   *
   * @return util.Map[String, AnyRef] = A map containing a set of values based on what the specified return was in the script
   */
  private def runScriptMap(script: String): util.Map[String, AnyRef] = {
    val result = session.run(script)
    result.next().asMap()
  }

  /*
   * Runs a script and returns many maps containing data
   *
   * @param script: String = The script that will be run
   *
   * @return List[util.Map[String, AnyRef]] = A list of maps containing sets of values based on what the specified return was in the script
   */
  private def runScriptManyMap(script: String): List[util.Map[String, AnyRef]] = {
    var returnList = List[util.Map[String, AnyRef]]()
    val result = session.run(script)
    while(result.hasNext) {
      returnList = result.next().asMap() :: returnList
    }
    returnList
  }

  /*
   * Runs a script and then calculates the rating of a trip or activity based on the results
   *
   * @param script: String = The script that will be run
   *
   * @return Double = The rating for the activity or trip specified in the script
   */
  private def updatedRating(script: String): Double = {
    val ratingMapList = runScriptManyMap(script)
    var total: Double = 0
    var counter = 0
    for (ratingMap <- ratingMapList) {
      total += ratingMap.get("r.rating").asInstanceOf[Double]
      counter += 1
    }

    total / counter
  }

  /*
   * Adds an Activity with no relationships to the database
   *
   * @param activity: Activity = A model containing the data that will be added to the database
   *
   * @return String = The string response from Neo4j
   */
  def addActivity(activity: Activity): String = {
    val script = s"CREATE (activity:Activity {activityID:'${activity.activityID}',rating:${activity.rating}})"
    runScriptConsume(script)
  }

  /*
   * Retrieves activity data from the database
   *
   * @param activityID: String = The id of the activity that is being looked for
   *
   * @return Activity = The activity retrieved from Neo4j
   */
  def getActivity(activityID: String): Activity = {
    val script = s"MATCH (activity:Activity) WHERE activity.activityID = '$activityID' RETURN activity.activityID, activity.rating"
    val map = runScriptMap(script)
    Activity(map.get("activity.activityID").toString, map.get("activity.rating").asInstanceOf[Double])
  }

  /*
   * Deletes activity data from the database
   *
   * @param activityID: String = The id of the activity that is being looked for
   *
   * @return String = The string response from Neo4j
   */
  def deleteActivity(activityID: String): String = {
    val script = s"MATCH (activity:Activity) WHERE activity.activityID ='$activityID' DETACH DELETE activity"
    runScriptConsume(script)
  }

  private def checkUserExists(user: String): Boolean = {
    val checkUserScript = s"MATCH (a:User {userID: '$user'}) WITH COUNT(a) > 0 AS node_exists RETURN node_exists"
    if (runScriptMap(checkUserScript).get("node_exists") == false) {
      return false
    }

    true
  }

  private def checkTripExists(tripID: String): Boolean = {
    val checkTripScript = s"MATCH (a:Trip {tripID: '$tripID'}) WITH COUNT(a) > 0 AS node_exists RETURN node_exists"
    if (runScriptMap(checkTripScript).get("node_exists") == false) {
      return false
    }

    true
  }

  private def checkActivityExists(activityID: String): Boolean = {
    val checkActivityScript = s"MATCH (a:Activity {activityID: '$activityID'}) WITH COUNT(a) > 0 AS node_exists RETURN node_exists"
    if (runScriptMap(checkActivityScript).get("node_exists") == false) {
      return false
    }

    true
  }

  private def checkParticipantExists(userID: String, tripID: String): Boolean = {
    val checkUserTripRelationScript = s"MATCH (a:User {userID: '$userID'}), (b:Trip {tripID: '$tripID'}) RETURN EXISTS((a)-[:PARTICIPANT]-(b)) AS relation_exists"
    if (runScriptMap(checkUserTripRelationScript).get("relation_exists") == false) {
      return false
    }

    true
  }

  private def checkContainsExists(tripID: String, activityID: String): Boolean = {
    val checkTripActivityRelationScript = s"MATCH (a:Trip {tripID: '$tripID'}), (b:Activity {activityID: '$activityID'}) RETURN EXISTS((a)-[:CONTAINS]-(b)) AS relation_exists"
    if (runScriptMap(checkTripActivityRelationScript).get("relation_exists") == false) {
      return false
    }

    true
  }

  private def checkCompletedExists(userID: String, activityID: String): Boolean = {
    val checkUserActivityRelationScript = s"MATCH (a:User {userID: '$userID'}), (b:Activity {activityID: '$activityID'}) RETURN EXISTS((a)-[:COMPLETED]-(b)) AS relation_exists"
    if (runScriptMap(checkUserActivityRelationScript).get("relation_exists") == false) {
      return false
    }

    true
  }

  private def saveTripInDatabase(trip: Trip): String = {
    val tripScript = s"CREATE (trip:Trip {rating:${trip.rating},owner:'${trip.owner}',name:'${trip.name}',description:'${trip.description}',tripID:'${trip.tripID}',latitude:${trip.latitude},longitude:${trip.longitude},radius:${trip.radius}})"
    runScriptConsume(tripScript)
  }

  private def makeTripParticipant(userID: String, tripID: String): String = {
    val tripToUserScript = s"MATCH (a:Trip),(b:User) WHERE a.tripID='$tripID' AND b.userID='$userID' CREATE (a)-[:PARTICIPANT]->(b)"
    runScriptConsume(tripToUserScript)
  }

  private def makeActivityParticipant(userID: String, activityID: String): String = {
    val activityToUserScript = s"MATCH (a:Activity),(b:User) WHERE a.activityID='$activityID' AND b.userID='$userID' CREATE (a)-[:PARTICIPANT]->(b)"
    runScriptConsume(activityToUserScript)
  }

  private def makeParticipatedIn(userID: String, tripID: String, rating: Double): String = {
    val userToTripScript = s"MATCH (a:User),(b:Trip) WHERE a.userID='$userID' AND b.tripID='$tripID' CREATE (a)-[:PARTICIPATED_IN {rating:$rating, date:'${java.time.LocalDate.now.toString}'}]->(b)"
    runScriptConsume(userToTripScript)
  }

  private def makeContains(tripID: String, activityID: String): String = {
    val tripToActivityScript = s"MATCH (a:Trip),(b:Activity) WHERE a.tripID='$tripID' AND b.activityID='$activityID' CREATE (a)-[:CONTAINS]->(b)"
    runScriptConsume(tripToActivityScript)
  }

  private def makeCompleted(userID: String, activityID: String, rating: Double): String = {
    val userToActivityScript = s"MATCH (a:User),(b:Activity) WHERE a.userID='$userID' AND b.activityID='$activityID' CREATE (a)-[:COMPLETED {rating:$rating, date:'${java.time.LocalDate.now.toString}'}]->(b)"
    runScriptConsume(userToActivityScript)
  }

  private def setActivityRating(activityID: String): String = {
    val activityRatingScript = s"MATCH (a:Activity {activityID: '$activityID'})<-[r:COMPLETED]-(u:User) RETURN r.rating"

    val updateActivityRatingScript = s"MATCH (a:Activity {activityID: '$activityID'}) SET a.rating=${updatedRating(activityRatingScript)}"
    runScriptConsume(updateActivityRatingScript)
  }

  private def setTripRating(tripID: String): String = {
    val tripRatingScript = s"MATCH (a:Trip {tripID: '$tripID'})<-[r:PARTICIPATED_IN]-(u:User) RETURN r.rating"

    val updateActivityRatingScript = s"MATCH (a:Trip {tripID: '$tripID'}) SET a.rating=${updatedRating(tripRatingScript)}"
    runScriptConsume(updateActivityRatingScript)
  }

  /*
   * Saves a trip and all of its data and relationships to the database
   *
   * @param trip: Trip = A container holing all relevant trip data
   * @param activities: List[Activity] = A list of containers holding data on all activities completed on the trip
   *
   * @return List[String] = A list of all of the string responses from Neo4j
   */
  def saveTrip(trip: Trip, activities: List[Activity], categories: List[String]): List[String] = {
    var returnList = List[String]()

    if (!checkUserExists(trip.owner)) {
      return List("Error, owner does not exist")
    }

    if (!checkTripExists(trip.tripID)) {
      returnList = saveTripInDatabase(trip) :: returnList
    }

    if (!checkParticipantExists(trip.owner, trip.tripID)) {
      returnList = makeTripParticipant(trip.owner, trip.tripID) :: returnList

      returnList = makeParticipatedIn(trip.owner, trip.tripID, trip.rating) :: returnList
    }

    for(activity <- activities) {
      if (!checkActivityExists(activity.activityID)) {
        addActivity(activity)
      }

      if (!checkContainsExists(trip.tripID, activity.activityID)) {
        returnList = makeContains(trip.tripID, activity.activityID) :: returnList
      }

      if (!checkCompletedExists(trip.owner, activity.activityID)) {
        returnList = makeActivityParticipant(trip.owner, activity.activityID) :: returnList

        returnList = makeCompleted(trip.owner, activity.activityID, activity.rating) :: returnList
      }

      returnList = setActivityRating(activity.activityID) :: returnList
    }

    returnList = setTripRating(trip.tripID) :: returnList

    updateNeighbors(trip.owner)

    savePreferences(trip.owner, categories)

    returnList
  }

  /*
   * Retrieves a single trip's information
   *
   * @param name: String = The name of the trip being looked for
   *
   * @return Trip = The trip retrieved from the database
   */
  def getTrip(tripID: String): Trip = {
    val script = s"MATCH (trip:Trip) WHERE trip.tripID = '$tripID' RETURN trip.rating, trip.name, trip.owner, trip.description, trip.latitude, trip.longitude, trip.radius"
    val map = runScriptMap(script)
    Trip(map.get("trip.rating").asInstanceOf[Double], map.get("trip.owner").toString, map.get("trip.name").toString, map.get("trip.description").toString, tripID, map.get("trip.latitude").asInstanceOf[Double], map.get("trip.longitude").asInstanceOf[Double], map.get("trip.radius").asInstanceOf[Double])
  }

  /*
   * Deletes trip data from the database
   *
   * @param name: String = The name of the trip that is being looked for
   *
   * @return String = The string response from Neo4j
   */
  def deleteTrip(tripID: String): String = {
    val script = s"MATCH (trip:Trip) WHERE trip.tripID ='$tripID' DETACH DELETE trip"
    runScriptConsume(script)
  }

  /*
   * Adds a user to the database
   *
   * @param trip: User = A container holing all relevant user data
   *
   * @return String = The string response from Neo4j
   */
  def addUser(user: User): String = {
    val new_interests = user.interests.map(x => "\"" + x + "\"")
    val new_preferences = user.preferences.map(x => "\"" + x + "\"")

    val script = s"CREATE (user:User {userID:'${user.userID}',email:'${user.email}',name:'${user.name}'," +
      s"interests:${new_interests.toArray.mkString("[", ", ", "]")},preferences:${new_preferences.toArray.mkString("[", ", ", "]")}})"

    runScriptConsume(script)
  }

  /*
   * Retrieves a single user's information
   *
   * @param username: String = The username of the user being looked for
   *
   * @return User = The user retrieved from the database
   */
  def getUser(username: String): User = {
    val script = s"MATCH (user:User) WHERE user.userID = '$username' RETURN user.userID, user.email, user.name, user.interests, user.preferences"
    val map = runScriptMap(script)
    User(map.get("user.userID").toString, map.get("user.email").toString, map.get("user.name").toString, map.get("user.interests").asInstanceOf[java.util.List[String]].asScala.toList, map.get("user.preferences").asInstanceOf[java.util.List[String]].asScala.toList)
  }

  /*
   * Deletes user data from the database
   *
   * @param username: String = The username of the user that is being looked for
   *
   * @return String = The string response from Neo4j
   */
  def deleteUser(userID: String): String = {
    val script = s"MATCH (user:User) WHERE user.userID ='$userID' DETACH DELETE user"
    runScriptConsume(script)
  }

  private def updateIndividualNeighborStrength(user1: String, user2: String, map: util.Map[String, AnyRef]): Unit = {
    val script2: String = s"MATCH (:User {userID: '$user1'})-[rel:NEIGHBORS]->(:User {userID: '$user2'}) SET rel.neighborStrength = ${calculateNeighborStrength(user1, user2, map.get("rel.friend").asInstanceOf[Boolean])}"
    runScriptConsume(script2)
  }

  private def updateNeighborStrength(user1: String, user2: String): Unit = {
    val script1: String = s"MATCH (:User {userID: '$user1'})-[rel:NEIGHBORS]->(:User {userID: '$user2'}) RETURN rel.friend"
    val map: util.Map[String, AnyRef] = runScriptMap(script1)
    updateIndividualNeighborStrength(user1, user2, map)
    updateIndividualNeighborStrength(user2, user1, map)
  }

  private def updateNeighbors(username: String): Unit = {
    val activitiesList: List[Activity] = getActivitiesFromUser(username)
    for(activity <- activitiesList){
      val usernames: List[String] = getUsersFromActivity(activity.activityID)
      for(foundUsername <- usernames){
        if(username != foundUsername){
          val foundNeighbors: List[String] = getNeighbors(foundUsername)
          if(foundNeighbors.contains(username)){
            updateNeighborStrength(username, foundUsername)
          }
          else{
            makeNeighbors(username, foundUsername)
            makeNeighbors(foundUsername, username)
          }
        }
      }
    }
  }

  /*
   * Gets all users that have completed an activity
   *
   * @param activityID: String = The id of the activity being queried on
   *
   * @return List[String] = A list of usernames
   */
  private def getUsersFromActivity(activityID: String): List[String] = {
    val participants = s"MATCH (:Activity {activityID: '$activityID'})-[:PARTICIPANT]->(user:User) RETURN user.userID"
    returnUsernames(participants)
  }

  /*
   * Makes the first user neighbors with the second user
   *
   * @param user1: String = The name of the first user that is being looked for
   * @param user2: String = The name of the second user that is being looked for
   * @param friend: Boolean = Whether or not the users are friends
   *
   * @return String = The string response from Neo4j
   */
  private def makeNeighbors(user1: String, user2: String, friend: Boolean = false): String = {
    val neighborScript = s"MATCH (a:User),(b:User) WHERE a.userID='$user1' AND b.userID='$user2' CREATE (a)-[:NEIGHBORS {friend:$friend, neighborStrength:${calculateNeighborStrength(user1, user2, friend)}}]->(b)"
    runScriptConsume(neighborScript)
  }

  /*
   * Makes two users friends with one another
   *
   * @param users: UserFriend = A container holding the names of the two users to make friends
   *
   * @return List[String] = A list of the string responses from Neo4j
   */
  def makeFriend(users: UserFriend): List[String] = {
    var returnList = List[String]()
    returnList = makeNeighbors(users.user1, users.user2, friend = true) :: returnList
    returnList = makeNeighbors(users.user2, users.user1, friend = true) :: returnList
    returnList
  }

  /*
   * Calculates the strength of connection between two neighbors
   *
   * @param user1: String = The name of the first user to be compared
   * @param user2: String = The name of the second user to be compared
   * @param friend: Boolean = Whether or not the two users are friends
   *
   * @return Int = The strength of connection between the users
   */
  private def calculateNeighborStrength(user1: String, user2: String, friend: Boolean): Int = {
    var neighborStrength: Int = 0
    if(friend){
      neighborStrength += 10
    }
    val user1Activities: List[Activity] = getActivitiesFromUser(user1)
    val user2Activities: List[Activity] = getActivitiesFromUser(user2)

    for(activity1 <- user1Activities){
      neighborStrength = checkSameActivity(activity1, user2Activities, neighborStrength)
    }

    neighborStrength
  }

  /*
   * Checks whether the list contains the activity to be looked for and then adds 1 to neighbor strength if it is there
   *
   * @param activity: Activity = The activity that will be looked for in the list
   * @param activityList: List[Activity] = A list of activities to search for the previous activity
   * @param neighborStrength: Int = The neighborStrength between the users
   *
   * @return Int = The updated neighbor strength based on if the activity was in the list
   */
  private def checkSameActivity(activity: Activity, activityList: List[Activity], neighborStrength: Int): Int = {
    for (listActivity <- activityList) {
      if (activity.activityID == listActivity.activityID) {
        return neighborStrength + 1
      }
    }

    neighborStrength
  }

  /*
   * Gets a list of activities completed by a user
   *
   * @param username: String = The name of the user to be queried on
   *
   * @return List[Activity] = The activities the user has completed
   */
  private def getActivitiesFromUser(username: String): List[Activity] = {
    val containedActivities = s"MATCH (:User {userID: '$username'})-[:COMPLETED]->(activity:Activity) RETURN activity.activityID, activity.rating"
    val mapList = runScriptManyMap(containedActivities)
    mapActivities(mapList)
  }

  /*
   * Maps activity data to activity objects
   *
   * @param mapList: List[util.Map[String, AnyRef]] = A list of maps of activity data
   *
   * @return List[Activity] = A list of activities
   */
  private def mapActivities(mapList: List[util.Map[String, AnyRef]]): List[Activity] = {
    var returnList: List[Activity] = List[Activity]()

    for (map <- mapList) {
      returnList = Activity(map.get("activity.activityID").toString, map.get("activity.rating").asInstanceOf[Double]) :: returnList
    }

    returnList
  }

  /*
   * Gets all trips the specified user has completed
   *
   * @param username: String = The name of the user whose trips are to be fetched
   *
   * @return List[Trip] = A list of all trips completed by the specified user
   */
  def getTrips(username: String): List[Trip] = {
    var returnList: List[Trip] = List[Trip]()
    val participatedInTrips = s"MATCH (:User {userID: '$username'})-[:PARTICIPATED_IN]->(trip:Trip) RETURN trip.owner, trip.name, trip.rating, trip.description, trip.tripID, trip.latitude, trip.longitude, trip.radius"
    val mapList = runScriptManyMap(participatedInTrips)
    for (map <- mapList) {
      returnList = Trip(map.get("trip.rating").asInstanceOf[Double], map.get("trip.owner").toString, map.get("trip.name").toString, map.get("trip.description").toString, map.get("trip.tripID").toString, map.get("trip.latitude").asInstanceOf[Double], map.get("trip.longitude").asInstanceOf[Double], map.get("trip.radius").asInstanceOf[Double]) :: returnList
    }
    returnList
  }

  /*
   * Gets all activities the specified trip contains
   *
   * @param name: String = The name of the trip whose activities are to be fetched
   *
   * @return List[Activity] = A list of all activities contained in the specified trip
   */
  def getActivities(tripID: String): List[Activity] = {
    val containedActivities = s"MATCH (:Trip {tripID: '$tripID'})-[:CONTAINS]->(activity:Activity) RETURN activity.activityID, activity.rating"
    val mapList = runScriptManyMap(containedActivities)
    mapActivities(mapList)
  }

  /*
   * Gets all friends of the specified user
   *
   * @param username: String = The name of the user whose friends are to be fetched
   *
   * @return List[String] = A list of all of the usernames of the specified users friends
   */
  def getFriends(username: String): List[String] = {
    val friend = s"MATCH (:User {userID: '$username'})-[:NEIGHBORS {friend: true}]->(user:User) RETURN user.userID"
    returnUsernames(friend)
  }

  /*
   * Gets all friends of the specified user
   *
   * @param username: String = The name of the user whose friends are to be fetched
   *
   * @return List[String] = A list of all of the usernames of the specified users friends
   */
  def getNeighbors(username: String): List[String] = {
    val friend = s"MATCH (:User {userID: '$username'})-[:NEIGHBORS]->(user:User) RETURN user.userID"
    returnUsernames(friend)
  }

  /*
   * Runs the query to get a list of usernames based on that query
   *
   * @param script: String = The script to be run
   *
   * @return List[String] = A list of usernames
   */
  private def returnUsernames(script: String): List[String] = {
    var returnList: List[String] = List[String]()
    val mapList = runScriptManyMap(script)
    for (map <- mapList) {
      returnList = map.get("user.userID").toString :: returnList
    }
    returnList
  }

  def savePreferences(username: String, preferences: List[String]): String = {
    val user: User = getUser(username)
    var preferenceList = List[String]()
    for(preference <- user.preferences){
      if(!preferences.contains(preference)){
        preferenceList = preference :: preferenceList
      }
    }
    for(preference <- preferences){
      preferenceList = preference :: preferenceList
    }
    val new_preferences = preferenceList.map(x => "\"" + x + "\"")
    val script: String = s"MATCH (user:User {userID: '$username'}) SET user.preferences = ${new_preferences.toArray.mkString("[", ", ", "]")}"
    runScriptConsume(script)
  }

  def getNeighborPreferences(username: String): List[String] = {
    var allPreferences = List[String]()
    val neighbors = getNeighbors(username)

    for(neighbor <- neighbors) {
      val preferences = getPreferences(neighbor)
      for(preference <- preferences) {
        if(!allPreferences.contains(preference)) {
          allPreferences = preference :: allPreferences
        }
      }
    }

    allPreferences
  }

  private def getPreferences(username: String): List[String] = {
    val script = s"MATCH (user:User) WHERE user.userID = '$username' RETURN user.preferences"
    val map = runScriptMap(script)
    map.get("user.preferences").asInstanceOf[java.util.List[String]].asScala.toList
  }
}
