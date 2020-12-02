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
exports.createUser = functions.https.onCall(async (data, context) => {
  if (typeof data.email === "undefined" || typeof context.auth.uid === "undefined" ||
  typeof data.firstname === "undefined" || typeof data.lastname === "undefined" ||
  typeof data.balance === "undefined") {
    return {
      result:"Bad request Check parameters or Body",
    };
  }
  else {
    // Create user details (user information and balance)
    const userDetails = {
      balance: parseFloat(data.balance),
      balanceonhold: 0,
      fname: data.firstname,
      lname: data.lastname,
      email: context.auth.token.email || null,
    }
    // Store user into firestore
    const writeResult = await admin.firestore().collection('Users').doc(context.auth.uid).set(userDetails);

    const transactionDetails = {
      history: [],
    };

    // Store transaction to firestore
    const writeResult2 = await admin.firestore().collection('TransactionHistory').doc(context.auth.uid).set(transactionDetails);
    // Send back that user was succesfully written to firestore
    return {
      result:`User with ID: ${writeResult.id} added`,
      userDetails: userDetails,
    };
  }
})

// Endpoint for adding money to user balance
exports.addBalance = functions.https.onCall(async (data, context) => {
  if (typeof context.auth.uid === "undefined" || typeof data.balance === "undefined") {
    return {
      result:"Bad request Check parameters or Body",
    };
  }
  else {
    // Create user details (amount added to balance)
    const userDetails = { balance: admin.firestore.FieldValue.increment(parseFloat(data.balance)) }
    // Update user's balance
    const writeResult = await admin.firestore().collection('Users').doc(context.auth.uid).update(userDetails);
    // Send back a message that user balance was succesfully updated
    return {
      result:`User with ID: ${context.auth.uid} balance updated`,
      userDetails: userDetails,
    };
  }
})

// Endpoint for getting a user profile
exports.getProfile = functions.https.onCall(async (data, context) => {
  if (typeof context.auth.uid === "undefined") {
    return {
      result:"Bad request Check parameters or Body",
    };
  }
  else {
    // Get user profile information
    const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
    const userProfile = readResult.data();
    if (typeof userProfile === "undefined") {
      //Else user not found
      return {
        result: `Unknown user: $${context.auth.uid}`,
      };
    }
    else {
      // Send user profile information
      return {
        result: {
          "fname": userProfile.fname,
          "lname": userProfile.lname, 
          "email": userProfile.email ,
          "balance": userProfile.balance,
          "balanceonhold": userProfile.balanceonhold,
        },
      };
    }
  }
})

// Endpoint for posting an item
exports.postItem = functions.https.onCall(async (data, context) => {
  if (typeof context.auth.uid === "undefined" || typeof data.item === "undefined" ||
  typeof data.startbid === "undefined" || typeof data.minfinalbid === "undefined") {
    return {
      result:"Bad request Check parameters or Body",
    };
  }
  else {
    // Read user profile from firestore
    const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
    const userProfile = readResult.data();
    if (typeof userProfile === "undefined") {
      // Else no user found
      res.json({
        result: `Unknown user: $${context.auth.uid}`,
      });
    }
    else {
      // Confirm user has funds to post item
      if (userProfile.balance >= 1) {
        // Create new user balance (current - $1)
        const userDetails = { balance: admin.firestore.FieldValue.increment(-1) }
        // Update user's balance
        const balanceWriteResult = await admin.firestore().collection('Users').doc(context.auth.uid).update(userDetails);
        itemObj = JSON.parse(data.item);

        // Create item details
        const itemDetails = {
          owner: context.auth.uid,
          item: itemObj,
          minfinalbid: parseFloat(data.minfinalbid),
          winningBid: {
            bidAmount: parseFloat(data.startbid),
            userId: context.auth.uid,
        },
          previousbids: [
            {
              bidAmount: parseFloat(data.startbid),
              userId: context.auth.uid,
            },
          ],
        }
        // Store item to firestore
        const writeResult = await admin.firestore().collection('Items').doc(itemObj.id).set(itemDetails);
        // Send back a message that we've succesfully written the message
        return {
          result: `Item with ID: ${writeResult.id} added`,
          itemDetails: itemDetails,
        };
      }
      else {
        // Else user does not have $1 to post the item
        return {
          result: `Insufficient funds: $${userProfile.balance}`,
        };
      }
    }
  }
})

// Endpoint for posting a bid on an item
exports.bidOnItem = functions.https.onCall(async (data, context) => {
    if (typeof data.itemId === "undefined" || typeof data.bidAmount === "undefined" ||
        typeof context.auth.uid === "undefined") {
        return {
          result:"Bad request Check parameters or Body",
        };
    }
    else {
        // Read user profile from firestore
        const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
        const userProfile = readResult.data();
        if (typeof userProfile === "undefined") {
            // Else no user found
            return {
                result: `Unknown user: $${context.auth.uid}`,
            };
        }
        else {
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(data.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              return {
                result: `Unknown item: $${data.itemId}`,
              };
            }
            else {
              var amount = parseFloat(data.bidAmount) + 1
            //  console.log(amount + "*****" + itemData.data().currentbid);
              if (userProfile.balance >= amount && (parseFloat(data.bidAmount) >= parseFloat(itemData.data().winningBid.bidAmount))) {
                  // update new user balance (current - $1)
                  const userDetails = { balance: admin.firestore.FieldValue.increment(-1) }

                  // Update user's balance
                  const balanceWriteResult = await admin.firestore().collection('Users').doc(context.auth.uid).update(userDetails);

                  // Create bid details
                  const bidDetails = {
                      bidAmount: parseFloat(data.bidAmount),
                      userId: context.auth.uid,
                      deviceToken: userProfile.deviceToken,
                   }
                  // Store item to firestore
                  const itemDoc = await admin.firestore().collection('Items').doc(data.itemId);
                  const unionRes = await itemDoc.update({
                      previousbids: admin.firestore.FieldValue.arrayUnion(bidDetails),
                  });

                  var checkforWiningBid = await admin.firestore().collection('Items').doc(data.itemId).get();
                  var bidArray = await checkforWiningBid.data().previousbids;

                  console.log(bidArray);

                  const max = Math.max.apply(Math, bidArray.map(function (o) { return o.bidAmount; }))

                  console.log("***" + max)
              
                  const winBid = bidArray.find(element => element.bidAmount == max);
                  await itemDoc.update({
                      winningBid: winBid,
                  });

                  // Send back a message that we've succesfully written the message
                  return {
                    result: `Bid with ID: ${itemDoc.id} added`,
                    bidDetails: bidDetails,
                  };
              }
              else {
                if (amount < parseFloat(itemData.data().winningBid.bidAmount)) {
                  return {
                    result: `Invalid bid: $${data.bidAmount}. Bid must be greater than or equal to the currentBid`,
                  };
                }
                else {
                  // Else user does not have $ to bid on the item
                  return {
                      result: `Insufficient funds: $${userProfile.balance}`,
                  };
                }
              }
            }
        }
    }
})

// Endpoint for cancel a bid on an item
exports.cancelBid = functions.https.onCall(async (data, context) => {
    if (typeof data.itemId === "undefined" ||  typeof context.auth.uid === "undefined") {
        return {
          result:"Bad request Check parameters or Body",
        };
    }
    else {
        // Read user profile from firestore
        const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
        const userProfile = readResult.data();
        if (typeof userProfile === "undefined") {
            // Else no user found
            return {
                result: `Unknown user: $${context.auth.uid}`,
            };
        }
        else {
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(data.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              return {
                result: `Unknown item: $${data.itemId}`,
              };
            }
            else {
              if (itemData.data().winningBid.userId === context.auth.uid) {

                  if(itemData.data().owner === context.auth.uid){
                    const cancelPostedItem = await admin.firestore().collection('Items').doc(data.itemId).delete();
  
                    // Send back a message that we've succesfully written the message
                    return {
                      result: `Your posted item has been canceled`,
                    };
                  }
                  else{
                    const filteredItems = itemData.data().previousbids.filter(item => item.userId !== itemData.data().winningBid.userId)
                    console.log(filteredItems);
  
                    const max = Math.max.apply(Math, filteredItems.map(function (o) { return o.bidAmount; }))
                    const winBid = filteredItems.find(element => element.bidAmount == max);
  
                    const itemDoc = await admin.firestore().collection('Items').doc(data.itemId);
                    const unionRes = await itemDoc.update({
                        winningBid: winBid,
                        previousbids: filteredItems,
                    });

                    // Send back a message that we've succesfully written the message
                    return {
                      result: `Bid canceled`,
                    };
  
                  }
              }
              else if(itemData.data().owner === context.auth.uid){
                  const cancelPostedItem = await admin.firestore().collection('Items').doc(data.itemId).delete();

                  // Send back a message that we've succesfully written the message
                  return {
                    result: `Your posted item has been canceled`,
                  };
              }
              else {
                  // Else user does not have $1 to post the item
                  return {
                      result: `User does not have winning bid therefore can't cancel bid`,
                  };
              }
            }
        }
    }
})

// Endpoint for accept a bid on an item
exports.acceptBid = functions.https.onCall(async (data, context) => {
    if (typeof data.itemId === "undefined" || typeof context.auth.uid === "undefined") {
        return {
          result:"Bad request Check parameters or Body",
        };
    }
    else {
        // Read user profile from firestore
        const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
        const userProfile = readResult.data();
        if (typeof userProfile === "undefined") {
            // Else no user found
            return {
                result: `Unknown user: $${context.auth.uid}`,
            };
        }
        else {
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(data.itemId).get();
            if (typeof itemData.data() === "undefined") {
              //Else user not found
              return {
                result: `Unknown item: $${data.itemId}`,
              };
            }
            else {
              if (itemData.data().owner === context.auth.uid && itemData.data().winningBid.bidAmount >= itemData.data().minfinalbid) {

                  const ownerRef = await admin.firestore().collection('Users').doc(context.auth.uid);
                  //const userId = itemData.data().winningBid.userId;
                  const winnerId = itemData.data().winningBid.userId;
                  const itemName = itemData.data().item.name;

                  const amount = parseFloat(itemData.data().winningBid.bidAmount);

                  //var userDetails = {
                  //    balanceonhold: admin.firestore.FieldValue.increment(-amount),
                      //balance: admin.firestore.FieldValue.increment(-amount)
                  //}

                  const ownerDetails = {
                      balance: admin.firestore.FieldValue.increment(amount),
                  }


                  const ownerDocRef = admin.firestore().collection('Users').doc(context.auth.uid);
                  //const userDocRef = admin.firestore().collection('Users').doc(userId);

                  await ownerDocRef.update(ownerDetails);
                  //await userDocRef.update(userDetails);

                  const itemDeleted = await admin.firestore().collection('Items').doc(data.itemId).delete();

                  let date_ob = new Date();
                  const transactionDetails = {
                      history: admin.firestore.FieldValue.arrayUnion({
                        sellerId: context.auth.uid,
                        item: itemName,
                        price: amount,
                        date: date_ob,
                      }),
                  }

                  // Store transaction to firestore
                  const writeResult = await admin.firestore().collection('TransactionHistory').doc(winnerId).update(transactionDetails);

                  // Send back a message that we've succesfully written the message
                  return {
                    result: `Bid accepted`,
                  };

              }
              else {
                  // Else user does not have $1 to post the item
                  return {
                      result: `Owner can't accept bid. Bid amount should be more than finalBidAmount.`,
                  };
              }
            }
        }
    }
})

// Endpoint for posting a bid on an item
exports.cancelItem = functions.https.onCall(async (data, context) => {
    if (typeof data.itemId === "undefined" || typeof context.auth.uid === "undefined") {
        res.status(BAD_REQUEST).send("Bad request Check parameters or Body");
    }
    else {
        // Read user profile from firestore
        const readResult = await admin.firestore().collection('Users').doc(context.auth.uid).get();
        const userProfile = readResult.data();
        if (typeof userProfile === "undefined") {
            // Else no user found
            return {
                result: `Unknown user: $${context.auth.uid}`,
            };
        }
        else {
            // Confirm user has funds to post item and amount is greater than minimum bid
            const itemData = await admin.firestore().collection('Items').doc(data.itemId).get();
            if (itemData.data().owner === context.auth.uid) {

                const result = await admin.firestore().collection('Items').doc(data.itemId).delete();
                // Send back a message that we've succesfully deleted the item
                return {
                    result: `Item Deleted`,
                };

            }
            else {
                // Else user does not have $1 to post the item
                return {
                    result: `User does not have permission to delete this item`,
                };
            }
        }
    }
})

exports.getHistory = functions.https.onCall(async (data, context) => {
  if (typeof context.auth.uid === "undefined") {
    return {
      result:"Bad request Check parameters or Body",
    };
  }
  else {
    // Get user profile information
    const readResult = await admin.firestore().collection('TransactionHistory').doc(context.auth.uid).get();
    const userHistory = readResult.data();
    if (typeof userHistory === "undefined") {
      //Else user not found
      return {
        result: `Unknown user: $${context.auth.uid}`,
      };
    }
    else {
      // Send user profile information
      return {
        result: {
          "history": userHistory.history,
        },
      };
    }
  }
})

exports.updateWinningBid = functions.firestore
    .document('Items/{itemId}')
    .onUpdate((change, context) => {
  
        const newBidWinner = change.after.data().winningBid.userId;
   
        const previousBidWinner = change.before.data().winningBid.userId;
 
        //check if bid added or cenceld by user
        if (change.before.data().previousbids.length > change.after.data().previousbids.length) {
            //send notification to both the user
      /*      var prevmessage = {
                notification: {
                    title: 'Winning Bid',
                    body: 'Your bid is no more the highest',
                },
                token: change.before.data().winningBid.deviceToken,
            };
            admin.messaging().send(prevmessage)
                .then((response) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
                });*/


            // notification to new Bid Winner
            var newmessage = {
                notification: {
                    title: 'Winning Bid',
                    body: 'Congrats! Your bid is the highest',
                },
                token: change.after.data().winningBid.deviceToken,
            };
            admin.messaging().send(newmessage)
                .then((response) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
                });

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

        /*   const prevmessage1 = {
                notification: {
                    title: 'Winning Bid',
                    body: 'Congrats! Your bid is the highest',
                },
               token: change.before.data().winningBid.deviceToken,
            };
            admin.messaging().send(prevmessage1)
                .then((response) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
                });*/


            // notification to new Bid Winner
            const newmessage1 = {
                notification: {
                    title: 'Winning Bid',
                    body: 'Your bid is no more the highest',
          
                },
                token: change.before.data().winningBid.deviceToken,
            };
            admin.messaging().send(newmessage1)
                .then((response) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
                });

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