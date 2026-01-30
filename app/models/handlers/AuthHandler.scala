package models.handlers
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory

import java.util.Collections

class AuthHandler {
  /*def authorize(idTokenString: String): Boolean = {
    val verifier: GoogleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
      // Specify the CLIENT_ID of the app that accesses the backend:
      .setAudience(Collections.singletonList(CLIENT_ID))
      // Or, if multiple clients access the backend:
      //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
      .build()

    // (Receive idTokenString by HTTPS POST)

    val idToken: GoogleIdToken = verifier.verify(idTokenString)
    if (idToken != null) {
      val payload: Payload = idToken.getPayload

      // Print user identifier
      val userId: String = payload.getSubject
      println("User ID: " + userId)

      // Get profile information from payload
      val email: String = payload.getEmail
      val emailVerified: Boolean = java.lang.Boolean.valueOf(payload.getEmailVerified)
      val name: String = payload.get("name").asInstanceOf[String]
      val pictureUrl: String = payload.get("picture").asInstanceOf[String]
      val locale: String = payload.get("locale").asInstanceOf[String]
      val familyName: String = payload.get("family_name").asInstanceOf[String]
      val givenName: String = payload.get("given_name").asInstanceOf[String]

      // Use or store profile information
      // ...
      true
    } else {
      println("Invalid ID token.")
      false
    }
  }*/
}
