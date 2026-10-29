@regression @masterdata @deviceSpecCRUD
Feature: Device Specifications master data

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Device specification CRUD
    When the admin opens the "Device Specifications" master data page
    And the admin clicks "Create"
    And the admin enters the following values:
      | name             | ${data} |
      | description      | ${data} |
      | brand            | ${data} |
      | model            | ${data} |
      | minDriverversion | ${data} |
    And the admin selects the first "deviceTypeCode" option
    And the admin submits the new record
    And the admin filters the list by "name" "${data}"
    And the admin changes "name" of the first record to "${data}1"
    And the admin filters the list by "name" "${data}1"
    And the admin activates the first record
    And the admin changes "name" of the first record to "${data}2"
    And the admin filters the list by "name" "${data}2"
    And the admin deactivates the first record
