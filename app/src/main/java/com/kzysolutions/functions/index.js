const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

exports.notifyPendingPayments = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  const uid = context.auth.uid;

  const flatsSnap = await admin.database().ref('Flats').orderByChild('netDue').startAt(1).once('value');
  const tokens = [];

  flatsSnap.forEach(fSnap => {
    const flatId = fSnap.key;
    const users = fSnap.child('users').val() || {};
    Object.keys(users).forEach(u => {
      admin.database().ref(`Users/${u}/fcmTokens`).get().then(tSnap => {
        tSnap.forEach(ts => tokens.push(ts.key));
      });
    });
  });

  if (!tokens.length) return { message: 'No pending dues or tokens.' };

  const message = {
    notification: {
      title: data.title || 'Payment Reminder',
      body: data.body || 'Your maintenance payment is pending. Please pay soon.'
    },
    tokens
  };

  const response = await admin.messaging().sendMulticast(message);
  return { successCount: response.successCount, failureCount: response.failureCount };
});
