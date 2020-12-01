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
        itemObj = JSON.parse(req.body.item);

        // Create item details
        var itemDetails = {
          owner: req.body.uid,
          item: itemObj,
          minfinalbid: parseFloat(req.body.minfinalbid),
          winningBid: {
            bidAmount: parseFloat(req.body.startbid),
            userId: req.body.uid,
        },
          previousbids: [
            {
              bidAmount: parseFloat(req.body.startbid),
              userId: req.body.uid,
            },
          ],
        }
        // Store item to firestore
        const writeResult = await admin.firestore().collection('Items').doc(itemObj.id).set(itemDetails);
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

// Endpoint for posting a bid on an item
exports.bidOnItem = functions.https.onRequest(async (req, res) => {
    if (typeof req.body.itemId === "undefined" || typeof req.body.bidAmount === "undefined" ||
        typeof req.body.uid === "undefined") {
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
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(req.body.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              res.json({
                result: `Unknown item: $${req.body.itemId}`,
              });
            }
            else {
              var amount = parseFloat(req.body.bidAmount) + 1
            //  console.log(amount + "*****" + itemData.data().currentbid);
              if (userProfile.balance >= amount && (parseFloat(req.body.bidAmount) >= parseFloat(itemData.data().winningBid.bidAmount))) {
                  // update new user balance (current - $1)
                  var userDetails = { balance: admin.firestore.FieldValue.increment(-1) }

                  // Update user's balance
                  const balanceWriteResult = await admin.firestore().collection('Users').doc(req.body.uid).update(userDetails);

                  // Create bid details
                  var bidDetails = {
                      bidAmount: parseFloat(req.body.bidAmount),
                      userId: req.body.uid,
                  }
                  // Store item to firestore
                  const itemDoc = await admin.firestore().collection('Items').doc(req.body.itemId);
                  const unionRes = await itemDoc.update({
                      previousbids: admin.firestore.FieldValue.arrayUnion(bidDetails),
                  });

                  var checkforWiningBid = await admin.firestore().collection('Items').doc(req.body.itemId).get();
                  var bidArray = await checkforWiningBid.data().previousbids;

                  console.log(bidArray);

                  const max = Math.max.apply(Math, bidArray.map(function (o) { return o.bidAmount; }))

                  console.log("***" + max)
              
                  const winBid = bidArray.find(element => element.bidAmount == max);
                  await itemDoc.update({
                      winningBid: winBid,
                  });

                  // Send back a message that we've succesfully written the message
                  res.json({ result: `Bid with ID: ${itemDoc.id} added`, bidDetails: bidDetails });
              }
              else {
                if (amount < parseFloat(itemData.data().winningBid.bidAmount)) {
                  res.json({
                    result: `Invalid bid: $${req.body.bidAmount}. Bid must be greater than or equal to the currentBid`,
                });
                }
                else {
                  // Else user does not have $ to bid on the item
                  res.json({
                      result: `Insufficient funds: $${userProfile.balance}`,
                  });
                }
              }
            }
        }
    }
})

// Endpoint for cancel a bid on an item
exports.cancelBid = functions.https.onRequest(async (req, res) => {
    if (typeof req.body.itemId === "undefined" ||  typeof req.body.uid === "undefined") {
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
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(req.body.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              res.json({
                result: `Unknown item: $${req.body.itemId}`,
              });
            }
            else {
              if (itemData.data().winningBid.userId === req.body.uid) {

                  if(itemData.data().owner === req.body.uid){
                    const cancelPostedItem = await admin.firestore().collection('Items').doc(req.body.itemId).delete();
  
                    // Send back a message that we've succesfully written the message
                    res.json({
                      result: `Your posted item has been canceled`,
                    });
                  }
                  else{
                    const filteredItems = itemData.data().previousbids.filter(item => item.userId !== itemData.data().winningBid.userId)
                    console.log(filteredItems);
  
                    const max = Math.max.apply(Math, filteredItems.map(function (o) { return o.bidAmount; }))
                    const winBid = filteredItems.find(element => element.bidAmount == max);
  
                    const itemDoc = await admin.firestore().collection('Items').doc(req.body.itemId);
                    const unionRes = await itemDoc.update({
                        winningBid: winBid,
                        previousbids: filteredItems,
                    });

                    // Send back a message that we've succesfully written the message
                    res.json({
                      result: `Bid canceled`,
                    });
  
                  }
              }
              else if(itemData.data().owner === req.body.uid){
                  const cancelPostedItem = await admin.firestore().collection('Items').doc(req.body.itemId).delete();

                  // Send back a message that we've succesfully written the message
                  res.json({
                    result: `Your posted item has been canceled`,
                  });
              }
              else {
                  // Else user does not have $1 to post the item
                  res.json({
                      result: `User does not have winning bid therefore can't cancel bid`,
                  });
              }
            }
        }
    }
})

// Endpoint for accept a bid on an item
exports.acceptBid = functions.https.onRequest(async (req, res) => {
    if (typeof req.body.itemId === "undefined" || typeof req.body.uid === "undefined") {
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
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(req.body.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              res.json({
                result: `Unknown item: $${req.body.itemId}`,
              });
            }
            else {
              if (itemData.data().owner === req.body.uid && itemData.data().winningBid.bidAmount >= itemData.data().minfinalbid) {

                  const ownerRef = await admin.firestore().collection('Users').doc(req.body.uid);
                  //const userId = itemData.data().winningBid.userId;

                  var amount = parseFloat(itemData.data().winningBid.bidAmount);

                  //var userDetails = {
                  //    balanceonhold: admin.firestore.FieldValue.increment(-amount),
                      //balance: admin.firestore.FieldValue.increment(-amount)
                  //}

                  var ownerDetails = {
                      balance: admin.firestore.FieldValue.increment(amount),
                  }


                  const ownerDocRef = admin.firestore().collection('Users').doc(req.body.uid);
                  //const userDocRef = admin.firestore().collection('Users').doc(userId);

                  await ownerDocRef.update(ownerDetails);
                  //await userDocRef.update(userDetails);

                  const itemDeleted = await admin.firestore().collection('Items').doc(req.body.itemId).delete();


                  // Send back a message that we've succesfully written the message
                  res.json({
                    result: `Bid accepted`,
                  });

              }
              else {
                  // Else user does not have $1 to post the item
                  res.json({
                      result: `Owner can't accept bid. Bid amount should be more than finalBidAmount.`,
                  });
              }
            }
        }
    }
})

// Endpoint for posting a bid on an item
exports.cancelItem = functions.https.onRequest(async (req, res) => {
    if (typeof req.body.itemId === "undefined" || typeof req.body.uid === "undefined") {
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
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(req.body.itemId).get();
            if (itemData.data().owner === req.body.uid) {

                const result = await admin.firestore().collection('Items').doc(req.body.itemId).delete();
                // Send back a message that we've succesfully deleted the item
                res.json({
                    result: `Item Deleted`,
                });

            }
            else {
                // Else user does not have $1 to post the item
                res.json({
                    result: `User does not have permission to delete this item`,
                });
            }
        }
    }
})

exports.updateWinningBid = functions.firestore
    .document('Items/{itemId}')
    .onUpdate((change, context) => {
        
        // Get an object representing the document
        // e.g. {'name': 'Marie', 'age': 66}
        const newBidWinner = change.after.data().winningBid.userId;
        // ...or the previous value before this update
        const previousBidWinner = change.before.data().winningBid.userId;

        //check if bid added or cenceld by user
        if (change.before.data().previousbids.length > change.after.data().previousbids.length) {
            //send notification to both the user

            //decrement balance on hold of previous user and increment the balnceon hold of after user
            if (change.before.data().winningBid.userId != change.before.data().owner) {
              let amount1 = parseFloat(change.before.data().winningBid.bidAmount);
              var userDetailsprev = {
                balance: admin.firestore.FieldValue.increment(amount1),
                balanceonhold: admin.firestore.FieldValue.increment(-amount1),
              }
              const docRef = admin.firestore().collection('Users').doc(previousBidWinner);
              const balanceWriteResult = docRef.update(userDetailsprev);
            }

            if (change.after.data().winningBid.userId != change.after.data().owner) {
              const docRefNew = admin.firestore().collection('Users').doc(newBidWinner);
              let amount2 = parseFloat(change.after.data().winningBid.bidAmount);
              var userDetailsafter = {
                balance: admin.firestore.FieldValue.increment(-amount2),
                balanceonhold: admin.firestore.FieldValue.increment(amount2),
              }
              const balanceOnHoldWriteResult = docRefNew.update(userDetailsafter);
            }

        }
        else {
               //send notification to both the user
            //decrement balanceonhold of new user and increment the balnceonhold of previous user
            if (change.after.data().winningBid.userId != change.after.data().owner) {
              var amount3 = parseFloat(change.after.data().winningBid.bidAmount);
              var userDetailsNew = {
                balance: admin.firestore.FieldValue.increment(-amount3),
                balanceonhold: admin.firestore.FieldValue.increment(amount3),
              }
              const docRef = admin.firestore().collection('Users').doc(newBidWinner);
              const balanceWriteResult = docRef.update(userDetailsNew);
            }

            //previous bid winner
            if (change.before.data().winningBid.userId != change.before.data().owner) {
              const docRefNew = admin.firestore().collection('Users').doc(previousBidWinner);
              let amount4 = parseFloat(change.before.data().winningBid.bidAmount);
              var userDetailsBefore = {
                balance: admin.firestore.FieldValue.increment(amount4),
                balanceonhold: admin.firestore.FieldValue.increment(-amount4),
              }
              const balanceOnHoldWriteResult = docRefNew.update(userDetailsBefore);
            }
        }
            console.log("previous value" + previousBidWinner);

        console.log("new value" + newBidWinner);


        return null
    });

exports.updateOnItemDelete = functions.firestore
    .document('Items/{itemId}')
    .onDelete((change, context) => {

        const bidWinner = change.data().winningBid.userId;
        const amount = parseFloat(change.data().winningBid.bidAmount);

        //update balnceOnHold of winneg bider
        var userDetailsNew = { balanceonhold: admin.firestore.FieldValue.increment(-amount) }
        const docRef = admin.firestore().collection('Users').doc(bidWinner);
            const balanceWriteResult = docRef.update(userDetailsNew);

        return null
    });






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