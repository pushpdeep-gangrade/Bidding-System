const functions = require('firebase-functions');

const admin = require('firebase-admin');
const { user } = require('firebase-functions/lib/providers/auth');
admin.initializeApp();

//Status encoded
const OK = 200;
const BAD_REQUEST = 400;
const UNAUTHORIZED = 401;
const CONFLICT = 403;
const NOT_FOUND = 404;
const INTERNAL_SERVER_ERROR = 500;

// Endpoint for creating a user
exports.createUser = functions.https.onRequest(async (req, res) => {
  if (typeof req.body.email === "undefined" || typeof req.body.uid === "undefined" ||
  typeof req.body.firstname === "undefined" || typeof req.body.lastname === "undefined" ||
  typeof req.body.balance === "undefined") {
    res.status(BAD_REQUEST).send("Bad request Check parameters or Body");
  }
  else {
    // Create user details (user information and balance)
    var userDetails = {
      balance: parseFloat(req.body.balance),
      balanceonhold: 0,
      fname: req.body.firstname,
      lname: req.body.lastname,
      email: req.body.email,
    }
    // Store user into firestore
    const writeResult = await admin.firestore().collection('Users').doc(req.body.uid).set(userDetails);
    // Send back that user was succesfully written to firestore
    res.json({result:`User with ID: ${writeResult.id} added`, userDetails: userDetails});
  }
})

// Endpoint for adding money to user balance
exports.addBalance = functions.https.onRequest(async (req, res) => {
  if (typeof req.body.uid === "undefined" || typeof req.body.balance === "undefined") {
    res.status(BAD_REQUEST).send("Bad request Check parameters or Body");
  }
  else {
    // Create user details (amount added to balance)
    var userDetails = { balance: admin.firestore.FieldValue.increment(parseFloat(req.body.balance)) }
    // Update user's balance
    const writeResult = await admin.firestore().collection('Users').doc(req.body.uid).update(userDetails);
    // Send back a message that user balance was succesfully updated
    res.json({result:`User with ID: ${req.body.uid} balance updated`, userDetails: userDetails});
  }
})

// Endpoint for getting a user profile
exports.getProfile = functions.https.onRequest(async (req, res) => {
  if (typeof req.body.uid === "undefined") {
    res.status(BAD_REQUEST).send("Bad request Check parameters or Body");
  }
  else {
    // Get user profile information
    const readResult = await admin.firestore().collection('Users').doc(req.body.uid).get();
    const userProfile = readResult.data();
    if (typeof userProfile === "undefined") {
      //Else user not found
      res.json({
        result: `Unknown user: $${req.body.uid}`,
      });
    }
    else {
      // Send user profile information
      res.json({
        result: {
          "fname": userProfile.fname,
          "lname": userProfile.lname, 
          "email": userProfile.email ,
          "balance": userProfile.balance,
          "balanceonhold": userProfile.balanceonhold,
        },
      });
    }
  }
})

// Endpoint for posting an item
exports.postItem = functions.https.onRequest(async (req, res) => {
  if (typeof req.body.uid === "undefined" || typeof req.body.item === "undefined" ||
  typeof req.body.startbid === "undefined" || typeof req.body.minfinalbid === "undefined") {
    res.status(BAD_REQUEST).send("Bad request Check parameters or Body");
  }
  else {
    // Read user profile from firestore
    const readResult = await admin.firestore().collection('Users').doc(req.body.uid).get();
    const userProfile = readResult.data();
    if (typeof userProfile === "undefined") {
      // Else no user found
      res.json({
        result: `Unknown user: $${req.body.uid}`,
      });
    }
    else {
      // Confirm user has funds to post item
      if (userProfile.balance >= 1) {
        // Create new user balance (current - $1)
        var userDetails = { balance: admin.firestore.FieldValue.increment(-1) }
        // Update user's balance
        const balanceWriteResult = await admin.firestore().collection('Users').doc(req.body.uid).update(userDetails);

        // Create item details
        var itemDetails = {
          owner: req.body.uid,
          item: req.body.item,
          currentbid: req.body.startbid,
          bidholder: req.body.uid,
          minfinalbid: req.body.minfinalbid,
          previousbids: [],
        }
        // Store item to firestore
        const writeResult = await admin.firestore().collection('Items').add(itemDetails);
        // Send back a message that we've succesfully written the message
        res.json({result:`Item with ID: ${writeResult.id} added`, itemDetails: itemDetails});
      }
      else {
        // Else user does not have $1 to post the item
        res.json({
          result: `Insufficient funds: $${userProfile.balance}`,
        });
      }
    }
  }
})

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

//EXAMPLE CLOUD FUNCTION 1
// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original
// exports.addMessage = functions.https.onRequest(async (req, res) => {
//     // Grab the text parameter.
//     const original = req.query.text;
//     // Push the new message into Cloud Firestore using the Firebase Admin SDK.
//     const writeResult = await admin.firestore().collection('messages').add({ original: original });
//     // Send back a message that we've succesfully written the message
//     res.json({ result: `Message with ID: ${writeResult.id} added.` });
// });

//EXAMPLE CLOUD FUNCTION 2
// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
// exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
//     .onCreate((snap, context) => {
//         // Grab the current value of what was written to Cloud Firestore.
//         const original = snap.data().original;

//         // Access the parameter `{documentId}` with `context.params`
//         functions.logger.log('Uppercasing', context.params.documentId, original);

//         const uppercase = original.toUpperCase();

//         // You must return a Promise when performing asynchronous tasks inside a Functions such as
//         // writing to Cloud Firestore.
//         // Setting an 'uppercase' field in Cloud Firestore document returns a Promise.
//         return snap.ref.set({ uppercase }, { merge: true });
//     });