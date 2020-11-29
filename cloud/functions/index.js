const functions = require('firebase-functions');

const admin = require('firebase-admin')
admin.initializeApp();

var database = admin.database();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
// https://console.firebase.google.com/project/auction-a09bd/overview
//
 exports.helloWorld = functions.https.onRequest((request, response) => {
   functions.logger.info("Hello logs!", {structuredData: true});
   response.send("Hello from Firebase!");
 });

//EXAMPLE CLOUD FUNCTION 1
// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
    // Grab the text parameter.
   // const original = req.query.text;

    var userId = Math.random().toString(36).substr(2, 9);
    // Push the new message into Cloud Firestore using the Firebase Admin SDK.
    database.ref('/Users/' + userId).set({ balance: 200, fname: "Bob", lname: "Smith" });
    //const writeResult = await admin.firestore().collection('messages').add({ original: original });
    // Send back a message that we've succesfully written the message
    res.json({ result: `Message with ID:` + userId });
});

exports.addMoney = functions.https.onRequest(async (req, res) => {
    // Grab the text parameter.
    // const original = req.query.text;
    var userId = Math.random().toString(36).substr(2, 9);
    // Push the new message into Cloud Firestore using the Firebase Admin SDK.
    database.ref('/Users/73z8bejce' ).set({ balance: 200});
    //const writeResult = await admin.firestore().collection('messages').add({ original: original });
    // Send back a message that we've succesfully written the message
    res.json({ result: `Message with ID:` + userId });
});

exports.updateBalance = functions.database.ref('/Users/{userId}/balance')
    .onWrite((change, context) => {
        // Only edit data when it is first created.
        if (change.before.exists()) {
            return null;
        }
        // Exit when the data is deleted.
        if (!change.after.exists()) {
            return null;
        }
        // Grab the current value of what was written to the Realtime Database.
        const previousBal = change.before.val();
        const added = change.after.val();
        console.log('Uppercasing', context.params.pushId, original);
        const uppercase = original.toUpperCase();
        const total = previousBal + added;
        // You must return a Promise when performing asynchronous tasks inside a Functions such as
        // writing to the Firebase Realtime Database.
        // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
        database.ref('/Users/{userId}/balance').set(total);
        return "updated";
    });






//EXAMPLE CLOUD FUNCTION 2
// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
    .onCreate((snap, context) => {
        // Grab the current value of what was written to Cloud Firestore.
        const original = snap.data().original;

        // Access the parameter `{documentId}` with `context.params`
        functions.logger.log('Uppercasing', context.params.documentId, original);

        const uppercase = original.toUpperCase();

        // You must return a Promise when performing asynchronous tasks inside a Functions such as
        // writing to Cloud Firestore.
        // Setting an 'uppercase' field in Cloud Firestore document returns a Promise.
        return snap.ref.set({ uppercase }, { merge: true });
    });

