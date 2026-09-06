package com.yogeshpaliyal.keypass.ui.redux.middlewares

import android.content.Intent
import com.yogeshpaliyal.keypass.MyApplication
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.generate.GeneratePasswordActivity
import com.yogeshpaliyal.keypass.ui.redux.actions.BatchActions
import com.yogeshpaliyal.keypass.ui.redux.actions.IntentNavigation
import com.yogeshpaliyal.keypass.ui.redux.states.KeyPassState
import org.reduxkotlin.Store
import org.reduxkotlin.middleware

/**
 * Middleware to handle intent navigation
 */
val intentNavigationMiddleware = middleware<KeyPassState> { store, next, action ->
    val state = store.state
    if (action is BatchActions) {
        action.actions.forEach {
            store.handleAction(it, state)
        }
    } else {
        store.handleAction(action, state)
    }

    next(action)
}

private fun Store<KeyPassState>.handleAction(action: Any, state: KeyPassState) {
    when (action) {
        is IntentNavigation.GeneratePassword -> {
            (state.context?.applicationContext as? MyApplication)?.knownActivityLaunchTriggered()
            val intent = Intent(state.context, GeneratePasswordActivity::class.java)
            state.context?.startActivity(intent)
        }

        is IntentNavigation.ShareApp -> {
            state.context?.let { context ->
                val sendIntent = Intent().apply {
                    this.action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_message))
                    type = "text/plain"
                }
                context.startActivity(
                    Intent.createChooser(
                        sendIntent,
                        context.getString(R.string.app_name)
                    )
                )
            }
        }
    }
}
