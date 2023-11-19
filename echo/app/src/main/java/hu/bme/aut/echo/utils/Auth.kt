package hu.bme.aut.echo.utils

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

fun getSigninClient(activity: Activity, clientId: String): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(activity, gso)
}