Feature: Money Transfer

  Scenario: Should add two users and transfer money
   Given MoneyTransfer is running
   When I add user id1 with name Thomas and availability 500
   And I add user id2 with name Brunone and availability 0
   And I transfer 100 money from id1 to id2
   Then it replies to size with 2
   And it shows that id1 has availability of 400
   And it shows that id2 has availability of 100

  Scenario: Should fail if I try to transfer more money than available
   Given MoneyTransfer is running
   When I add user id3 with name Brunone and availability 100
   And I add user id4 with name Thomas and availability 0
   Then I fail transfering 200 money from id3 to id4 because Brunone has not nough money
