package euphoria.psycho.notes.common;

public class Contexts {

}
         /*

         Request that a given application service be stopped.  If the service is
         not running, nothing happens.  Otherwise it is stopped.  Note that calls
         to startService() are not counted -- this stops the service no matter
         how many times it was started.

         Note that if a stopped service still has ServiceConnection
         objects bound to it with the BIND_AUTO_CREATE set, it will
         not be destroyed until all of these bindings are removed.  See
         the android.app.Service documentation for more details on a
         service's lifecycle.

         This function will throw SecurityException if you do not
         have permission to stop the given service.

         @param service Description of the service to be stopped.  The Intent must be either
         fully explicit (supplying a component name) or specify a specific package
         name it is targetted to.

         @return If there is a service matching the given Intent that is already
         running, then it is stopped and true is returned; else false is returned.

         @throws SecurityException If the caller does not have permission to access the service
         or the service can not be found.
         @throws IllegalStateException If the application is in a state where the service
         can not be started (such as not in the foreground in a state when services are allowed).

         @see #startService

         public abstract boolean stopService(Intent service);
         */
         /*

         Register a BroadcastReceiver to be run in the main activity thread.  The
         receiver will be called with any broadcast Intent that
         matches filter, in the main application thread.

         The system may broadcast Intents that are "sticky" -- these stay
         around after the broadcast has finished, to be sent to any later
         registrations. If your IntentFilter matches one of these sticky
         Intents, that Intent will be returned by this function
         and sent to your receiver as if it had just
         been broadcast.

         There may be multiple sticky Intents that match filter,
         in which case each of these will be sent to receiver.  In
         this case, only one of these can be returned directly by the function;
         which of these that is returned is arbitrarily decided by the system.

         If you know the Intent your are registering for is sticky, you can
         supply null for your receiver.  In this case, no receiver is
         registered -- the function simply returns the sticky Intent that
         matches filter.  In the case of multiple matches, the same
         rules as described above apply.

         See BroadcastReceiver for more information on Intent broadcasts.

         As of android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH, receivers
         registered with this method will correctly respect the
         Intent#setPackage(String) specified for an Intent being broadcast.
         Prior to that, it would be ignored and delivered to all matching registered
         receivers.  Be careful if using this for security.

         Note: this method cannot be called from a
         BroadcastReceiver component; that is, from a BroadcastReceiver
         that is declared in an application's manifest.  It is okay, however, to call
         this method from another BroadcastReceiver that has itself been registered
         at run time with registerReceiver, since the lifetime of such a
         registered BroadcastReceiver is tied to the object that registered it.

         @param receiver The BroadcastReceiver to handle the broadcast.
         @param filter Selects the Intent broadcasts to be received.

         @return The first sticky intent found that matches filter,
         or null if there are none.

         @see #registerReceiver(BroadcastReceiver, IntentFilter, String, Handler)
         @see #sendBroadcast
         @see #unregisterReceiver

         @Nullable
         public abstract Intent registerReceiver(@Nullable BroadcastReceiver receiver,
         IntentFilter filter);
         */
         /*

         Broadcast the given intent to all interested BroadcastReceivers, allowing
         an optional required permission to be enforced.  This
         call is asynchronous; it returns immediately, and you will continue
         executing while the receivers are run.  No results are propagated from
         receivers and receivers can not abort the broadcast. If you want
         to allow receivers to propagate results or abort the broadcast, you must
         send an ordered broadcast using
         sendOrderedBroadcast(Intent, String).

         See BroadcastReceiver for more information on Intent broadcasts.

         @param intent The Intent to broadcast; all receivers matching this
         Intent will receive the broadcast.
         @param receiverPermission (optional) String naming a permission that
         a receiver must hold in order to receive your broadcast.
         If null, no permission is required.

         @see android.content.BroadcastReceiver
         @see #registerReceiver
         @see #sendBroadcast(Intent)
         @see #sendOrderedBroadcast(Intent, String)
         @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)

         public abstract void sendBroadcast(@RequiresPermission Intent intent,
         @Nullable String receiverPermission);
         */
         /*

         Return the context of the single, global Application object of the
         current process.  This generally should only be used if you need a
         Context whose lifecycle is separate from the current context, that is
         tied to the lifetime of the process rather than the current component.

         Consider for example how this interacts with
         registerReceiver(BroadcastReceiver, IntentFilter):

         If used from an Activity context, the receiver is being registered
         within that activity.  This means that you are expected to unregister
         before the activity is done being destroyed; in fact if you do not do
         so, the framework will clean up your leaked registration as it removes
         the activity and log an error.  Thus, if you use the Activity context
         to register a receiver that is static (global to the process, not
         associated with an Activity instance) then that registration will be
         removed on you at whatever point the activity you used is destroyed.
         If used from the Context returned here, the receiver is being
         registered with the global state associated with your application.  Thus
         it will never be unregistered for you.  This is necessary if the receiver
         is associated with static data, not a particular component.  However
         using the ApplicationContext elsewhere can easily lead to serious leaks
         if you forget to unregister, unbind, etc.


         public abstract Context getApplicationContext();
         */
         /*

         Similar to startService(Intent), but with an implicit promise that the
         Service will call android.app.Service#startForeground(int, android.app.Notification)
         startForeground(int, android.app.Notification) once it begins running.  The service is given
         an amount of time comparable to the ANR interval to do this, otherwise the system
         will automatically stop the service and declare the app ANR.

         Unlike the ordinary startService(Intent), this method can be used
         at any time, regardless of whether the app hosting the service is in a foreground
         state.

         @param service Identifies the service to be started.  The Intent must be
         fully explicit (supplying a component name).  Additional values
         may be included in the Intent extras to supply arguments along with
         this specific start call.

         @return If the service is being started or is already running, the
         ComponentName of the actual service that was started is
         returned; else if the service does not exist null is returned.

         @throws SecurityException If the caller does not have permission to access the service
         or the service can not be found.

         @see #stopService
         @see android.app.Service#startForeground(int, android.app.Notification)

         @Nullable
         public abstract ComponentName startForegroundService(Intent service);
         */
         /*

         Request that a given application service be started.  The Intent
         should either contain the complete class name of a specific service
         implementation to start, or a specific package name to target.  If the
         Intent is less specified, it logs a warning about this.  In this case any of the
         multiple matching services may be used.  If this service
         is not already running, it will be instantiated and started (creating a
         process for it if needed); if it is running then it remains running.

         Every call to this method will result in a corresponding call to
         the target service's android.app.Service#onStartCommand method,
         with the intent given here.  This provides a convenient way
         to submit jobs to a service without having to bind and call on to its
         interface.

         Using startService() overrides the default service lifetime that is
         managed by bindService: it requires the service to remain
         running until stopService is called, regardless of whether
         any clients are connected to it.  Note that calls to startService()
         do not nest: no matter how many times you call startService(),
         a single call to stopService will stop it.

         The system attempts to keep running services around as much as
         possible.  The only time they should be stopped is if the current
         foreground application is using so many resources that the service needs
         to be killed.  If any errors happen in the service's process, it will
         automatically be restarted.

         This function will throw SecurityException if you do not
         have permission to start the given service.

         Note: Each call to startService()
         results in significant work done by the system to manage service
         lifecycle surrounding the processing of the intent, which can take
         multiple milliseconds of CPU time. Due to this cost, startService()
         should not be used for frequent intent delivery to a service, and only
         for scheduling significant work. Use bindService bound services
         for high frequency calls.


         @param service Identifies the service to be started.  The Intent must be
         fully explicit (supplying a component name).  Additional values
         may be included in the Intent extras to supply arguments along with
         this specific start call.

         @return If the service is being started or is already running, the
         ComponentName of the actual service that was started is
         returned; else if the service does not exist null is returned.

         @throws SecurityException If the caller does not have permission to access the service
         or the service can not be found.
         @throws IllegalStateException If the application is in a state where the service
         can not be started (such as not in the foreground in a state when services are allowed).

         @see #stopService
         @see #bindService

         @Nullable
         public abstract ComponentName startService(Intent service);
         */
         /*

         Launch a new activity.  You will not receive any information about when
         the activity exits.

         Note that if this method is being called from outside of an
         android.app.Activity Context, then the Intent must include
         the Intent#FLAG_ACTIVITY_NEW_TASK launch flag.  This is because,
         without being started from an existing Activity, there is no existing
         task in which to place the new activity and thus it needs to be placed
         in its own separate task.

         This method throws ActivityNotFoundException
         if there was no Activity found to run the given Intent.

         @param intent The description of the activity to start.
         @param options Additional options for how the Activity should be started.
         May be null if there are no options.  See android.app.ActivityOptions
         for how to build the Bundle supplied here; there are no supported definitions
         for building it manually.

         @throws ActivityNotFoundException &nbsp;

         @see #startActivity(Intent)
         @see PackageManager#resolveActivity

         public abstract void startActivity(@RequiresPermission Intent intent,
         @Nullable Bundle options);
         */