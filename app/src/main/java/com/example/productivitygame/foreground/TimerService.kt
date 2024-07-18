package com.example.productivitygame.foreground

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.productivitygame.MainActivity
import com.example.productivitygame.R
import com.example.productivitygame.ui.TimerContainer
import com.example.productivitygame.ui.TimerState
import com.example.productivitygame.ui.screens.CountdownItem
import com.example.productivitygame.ui.screens.TimerDestination
import com.example.productivitygame.ui.screens.WorkSegment
import com.example.productivitygame.ui.utils.DEFAULT_DURATION_FORMAT
import com.example.productivitygame.ui.utils.format
import kotlin.time.DurationUnit
import kotlin.time.toDuration


const val timerChannelId = "CHANNEL_TWO"
// Can be fixed since there should only be one timer running
const val foregroundNotificationId = 100
const val timerChannelName = "Timer Tracking"
const val timerChannelDesc = "Visual indicator of background stopwatch"



class TimerService: Service() {

    // in class scope to allow access by member functions but can only be initialised upon onCreate()
    // as Context is not yet available
    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var contentTitle = "Work Started!"
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var resumeAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var openAppPendingIntent: PendingIntent
    private val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        TimerContainer.setOnSegmentMillisChangedListener(::updateNotificationTime)
        TimerContainer.setOnTimerRunStateChangedListener(::updateNotificationState)
        TimerContainer.setOnTimerFinishedListener(::onTimerSessionCompleted)
        TimerContainer.setOnNewCountdownItemListener(::updateContentTitle)
        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val deepLinkIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = "myapp://${TimerDestination.route}?${TimerDestination.focusPlanNameArg}=${TimerContainer.focusPlan?.name}".toUri()
        }
        val deepLinkPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(1234, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }
        openAppPendingIntent = PendingIntent.getActivity(
            this,
            20,
            openAppIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        // NOTE: State is opposite of action
        // initialise actions
        pauseAction = createAction(this, TimerState.RUNNING)
        resumeAction = createAction(this, TimerState.PAUSED)
        nextAction = createAction(this, TimerState.SEGMENT_ENDED)

        notificationBuilder = NotificationCompat.Builder(this, timerChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            // TODO: SubText should be the name of task being completed
            //.setSubText("Testing")
            .setOngoing(true)
            .setContentIntent(deepLinkPendingIntent)
            .addAction(pauseAction)
            .setStyle(mediaStyle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) notificationBuilder?.setCategory(Notification.CATEGORY_STOPWATCH)
        Log.d("FOREGROUND", "Foreground service started")
    }

    // Only alternative is recreating the builder without the actions,
    // or adding the actions only upon notification (which means it would be added again each time
    // duration is updated)
    @SuppressLint("RestrictedApi")
    private val clearActionsNotificationExtender = NotificationCompat.Extender { builder ->
        builder.mActions.clear()
        builder
    }

    private fun createAction(context: Context, timerState: TimerState): NotificationCompat.Action {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, TimerServiceReceiver::class.java).apply {
                action = timerState.intentAction
            },
            FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            timerState.currentActionIconRes,
            timerState.title,
            pendingIntent
        ).build()
    }

    private fun startForeground() {
        try {
            val notification = notificationBuilder?.build()
            if (notification != null) {
                ServiceCompat.startForeground(
                    /* service = */ this,
                    /* id = */ foregroundNotificationId, // Cannot be 0
                    /* notification = */ notification,
                    /* foregroundServiceType = */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                Log.e("FOREGROUND", "Notification is null")
            }
        } catch (e: Exception) {
            Log.e("FOREGROUND", e.toString())
        }
    }
    // Callback used for Timer object to update notification
    private fun updateNotificationTime(segmentDurationMillis: Long) {
        val durationString = segmentDurationMillis
            .toDuration(DurationUnit.MILLISECONDS)
            .format(DEFAULT_DURATION_FORMAT)
        val notification = notificationBuilder
            ?.setContentText(durationString)
            ?.setContentTitle(contentTitle)
            ?.build()
        notificationManager?.notify(
            foregroundNotificationId,
            notification
        )
    }
    // Callback to end service and notify user for when the session is completed
    private fun onTimerSessionCompleted() {
        notificationManager?.notify(
            foregroundNotificationId,
            //TODO: Make this more different from usual notification
            NotificationCompat.Builder(this, timerChannelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Timer")
                .setContentText("Session Completed!")
                .setContentIntent(openAppPendingIntent)
                .build()
        )
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun updateContentTitle(countdownItem: CountdownItem) {
        val segmentName = if (countdownItem is WorkSegment) "Working" else "Break Time"
        with(TimerContainer.workCompletionProgress) {
            val workProgress = "($first/$second work completed!)"
            contentTitle = "$segmentName $workProgress"
        }


    }

    private fun updateNotificationState(timerState: TimerState) {
        val action = when (timerState) {
            TimerState.PAUSED -> resumeAction
            TimerState.RUNNING -> pauseAction
            TimerState.SEGMENT_ENDED -> nextAction
        }
        notificationManager?.notify(
            foregroundNotificationId,
            notificationBuilder
                ?.extend(clearActionsNotificationExtender)
                ?.addAction(action)
                ?.build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground()
        return START_STICKY
    }
}

class TimerServiceManager(private val context: Context) {
    init {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        createNotificationChannel(notificationManager!!)
    }
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            timerChannelId,
            timerChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = timerChannelDesc
        notificationManager.createNotificationChannel(channel)

    }
    fun initTimerService() {
        val intent = Intent(context, TimerService::class.java)
        context.startForegroundService(intent)
    }
    fun cancelTimerService() {
        context.stopService(Intent(context, TimerService::class.java))
    }
}