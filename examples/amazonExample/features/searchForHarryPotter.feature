Feature: Go to the Amazon web site, search for Harry Porter at book section. Find Harry Potter and the Chamber of Secrets (book2) and add to cart.

  Background: settings for the test
    Given the following settings:
      | url				    | http://www.amazon.com	|
      | platform			| Vista     				|
      | browser           	| firefox					|
      | browser version	    | 23     					|


  Scenario: Verify that we have an empty cart on the initial portal page
    Given I am ON "PortalPage"
    Then I should EXPECT
      | emptyCart              | 0 items              |

  Scenario: Search Harry Porter at book
    Given I am ON "PortalPage"
    And I click "section"
    And I click "books"
    And I enter
    	| searchField                 | Harry Potter                 | 
    And I click "go"   
    Then I should be ON "SearchResultsPage"


  Scenario: Go to SearchResultsPage and verify  first result Special Edition Harry Potter Paperback Box Set 
    Given I am ON "SearchResultsPage"
    Then I should EXPECT
    | firstResult               | Special Edition Harry Potter Paperback Box Set            |
    | chamberOfSecretsBook2     | visible             										|    

  Scenario: Click on  ChamberOfSecretsBook2, I can see details
    Given I am ON "SearchResultsPage" 
    And I CLICK "chamberOfSecretsBook2"
    Then I should be ON "BookDetailPage"

  Scenario: check expects on PDP
    Given I am ON "BookDetailPage" 
    Then I should EXPECT    
    | kindlePrice           | $7.99           	|
    | hardCoverPrice     	| $13.94            |    
    | defaultPrice     		| $8.98            	|       

  Scenario: Add to cart
    Given I am ON "BookDetailPage""
    And I click "hardCoverPrice"  
    And I wait 5 seconds  
    And I click "addToCart"      
    Then I should be ON "PreCheckoutPage"

  Scenario: on pre-checkout page, I check all my items
    Given I am ON "PreCheckoutPage"
    Then I should EXPECT
      | orderSubtotal          	| 13.94 	|
      | cartCount          		| 1 		|      
      
  Scenario: now proceed to checkout
    Given I am ON "PreCheckoutPage"
	And I click "proceedToCheckout"
	Then I should be ON "SignInPage"
	           