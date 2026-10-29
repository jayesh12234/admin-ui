@regression @masterdata @blocklistedwordsCRUD
Feature: Blocklisted Words master data

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Blocklisted word CRUD
    When the admin opens the "Blocklisted Words" master data page
    And the admin clicks "Create"
    And the admin enters the following values:
      | word        | ${blocklistedWord} |
      | description | ${data}            |
    And the admin submits the new record
    And the admin filters the list by "word" "${blocklistedWord}"
    And the admin changes "word" of the first record to "${blocklistedWord}auto"
    And the admin filters the list by "word" "${blocklistedWord}A"
    And the admin activates the first record
    And the admin changes "word" of the first record to "${blocklistedWord}B"
    And the admin filters the list by "word" "${blocklistedWord}B"
    And the admin deactivates the first record
