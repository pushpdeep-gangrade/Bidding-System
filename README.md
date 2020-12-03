# Bidding-System
# Table of Contents
- [Authors](#authors)
- [Video Demo](#demo)
- [Project Wiki](#wiki)

## Authors <a name="authors"></a>
- Pushdeep Gangrade
- Katy Mitchell
- Valerie Ray
- Rockford Stoller

## Video Demo <a name="demo"></a>
# todo: link video

## Project Wiki  <a name="wiki"></a>
In this assignment you are going to use Firebase Cloud Functions and Push Notifications. The mobile app is focused on a realtime bidding application. The following are the user stories:

### User Side
- Using Firebase, the user is able to register with email/password. Once registered, the user can
  login to the app, trade and see their progress.
- The user is able to check their profile for their current virtual money balance, and can add money
  to their virtual account. Users can add money by specifying the amount of money to add.
- The user is able to post an item for bid. Each item has a start bid and a minimum final bid set by
  the user. The user posting the bid pays $1 to the system to be able to post an item.
- A user is only able to make a bid if they have enough balance in their virtual account. If not, then
  the transaction fails and the user is notified.
- The user is able to accept a bid on an item they posted.
- The user is not allowed to over spend their current balance
- The user is able to cancel their posted items.
- The user is able to cancel their bid if it is the highest for that item.
- The user is able to view their previous transactions.

### Push Notifications
- The user is notified via push notification when:
  - Their bid is no longer the winning bid
  - Their posted item has reached its min final bid.
  - They become the highest bidder. If a higher bid is made by another user, the previous highest
    bidder is notified that they are now losing the bid and that there is a new higher bid.
  - They don't have enough money in their virtual account to make a bid.
  - When another user cancels their bid, and they now hold the highest bid (as long as they have enough
    money in their account).

### Backend Design and DB Schema

- Upon accepting a bid, the money is transferred to the user who posted the item.
- When a user cancels their bid, the highest previous bid made by a user with enough money in their
  account is selected as the highest bidder.
- The app uses Firebase Cloud Functions and transactions to ensure atomic operations. The Firebase
  callable functions include:
  - `createUserProfile({balance, fname, lname})` creates a user profile from the signup screen
  - `addMoney({amount})` adds money to a user's account.
  - `postNewItem({item_name, starting_bid, min_final_bid}) posts a new bid item
  - `bidOnItem({itemId, bid})` creates a bid on a posted item
  - `acceptBidOnItem({itemId})` accepts the current highest bid for an item
  - `cancelItem({itemId})` removes a bid for an item

