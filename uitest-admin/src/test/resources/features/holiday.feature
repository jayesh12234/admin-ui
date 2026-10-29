@regression @masterdata @holidaysCRUD
Feature: Holidays master data

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Holiday CRUD
    When the admin opens the "Holidays" master data page
    And the admin clicks "Create"
    And the admin enters the following values:
      | holidayName | ${data} |
      | holidayDesc | ${data} |
    And the admin picks the date "${holidayDate}"
    And the admin selects the first "locationCode" option
    And the admin submits the new record
    And the admin filters the list by "holidayName" "${data}"
    And the admin changes "holidayName" of the first record to "${data}1"
    And the admin filters the list by "holidayName" "${data}1"
    And the admin activates the first record
    And the admin changes "holidayName" of the first record to "${data}2"
    And the admin filters the list by "holidayName" "${data}2"
    And the admin deactivates the first record
