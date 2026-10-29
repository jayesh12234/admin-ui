@regression @masterdata @documentTypesCRUD
Feature: Document Types master data

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Document type CRUD
    When the admin opens the "Document Types" master data page
    And the admin clicks "Create"
    And the admin enters the following values:
      | code        | ${data} |
      | name        | ${data} |
      | description | ${data} |
    And the admin submits the new record
    And the admin filters the list by "name" "${data}"
    And the admin changes "name" of the first record to "${data}1"
    And the admin filters the list by "name" "${data}1"
    And the admin activates the first record
    And the admin changes "name" of the first record to "${data}2"
    And the admin filters the list by "name" "${data}2"
    And the admin deactivates the first record
