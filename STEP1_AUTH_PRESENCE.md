# Step 1 complete: Auth + realtime + presence + E2EE keys

Shipped:
- Real Firebase Phone Auth (PhoneAuthManager). Fake local OTP removed from ViewModel.
- PresenceManager: online, lastSeen, typing in Firestore.
- IdentityKeyManager: per-user P-256 key pair. Private key stays on device. Public key saved to users/{uid}.
- FCM service OlinamMessagingService + POST_NOTIFICATIONS permission.
- ChatViewModel.sendOtp / verifyOtp now use Firebase. markOnline publishes presence + public key.

Firebase console still required:
1. Enable Phone authentication provider.
2. Add firebase-messaging dependency if build fails (BOM already present; add implementation("com.google.firebase:firebase-messaging")).
3. Cloud Function to send FCM when a new message is written (client cannot push to another user by itself).

Realtime text chat was already on Firestore listeners. This step makes auth real, presence real, and identity keys ready for pairwise E2EE.
