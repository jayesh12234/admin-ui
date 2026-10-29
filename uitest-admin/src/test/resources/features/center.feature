@regression @resources @centerCRUD
Feature: Registration Centers

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Registration center CRUD
    When the admin opens the "Centers" resource page
    And the admin clicks "Create Center"
    And the admin enters the following values:
      | name          | ${data} |
    And the admin selects the first "centerTypeCode" option
    And the admin enters the following values:
      | contactPerson | ${data} |
      | contactPhone  | ${data} |
      | longitude     | 1.1234  |
      | latitude      | 2.2345  |
      | addressLine1  | ${data} |
      | addressLine2  | ${data} |
      | addressLine3  | ${data} |
    And the admin selects the first location at every hierarchy level
    And the admin selects the first "zone" option if it is available
    And the admin selects the first "holidayZone" option
    And the admin enters the following values:
      | noKiosk | 10 |
    And the admin selects "45" in "processingTime"
    And the admin selects "9:00 AM" in "startTime"
    And the admin selects "5:00 PM" in "endTime"
    And the admin selects "1:00 PM" in "lunchStartTime"
    And the admin selects "2:00 PM" in "lunchEndTime"
    And the admin selects the first 5 working days
    And the admin adds the exceptional holiday "${holidayDateCenter}"
    And the admin submits the new record
    And the admin filters the centers by name "${data}"
    And the admin changes the center name to "${data}1"
    And the admin filters the centers by name "${data}1"
    And the admin activates the first record
    And the admin changes the center name to "${data}2"
    And the admin filters the centers by name "${data}2"
    And the admin deactivates the first record
    And the admin decommissions the first record
