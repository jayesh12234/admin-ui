@regression @masterdata @dynamicFieldCRUD
Feature: Dynamic Fields master data

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Dynamic field CRUD
    When the admin opens the create dynamic field form
    And the admin enters the following values:
      | code        | ${data}    |
      | name        | Automation |
      | description | Automation |
      | value       | ${data}    |
    And the admin submits the new record
    And the admin filters the list by "description" "Automation"
    And the admin changes "code" of the first record to "${data}1"
    And the admin filters the list by "description" "Automation"
    And the admin activates the first record
    And the admin changes "code" of the first record to "${data}2"
    And the admin filters the list by "description" "Automation"
    And the admin deactivates the first record
