@regression @resources @machineCRUD
Feature: Machines

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Machine CRUD
    When the admin opens the "Machines" resource page
    And the admin clicks "Create Machine"
    And the admin enters the following values:
      | name         | ${data}     |
      | serialNumber | 1234567     |
      | macAddress   | 1.2.3.4.5.6 |
      | ipAddress    | 2.3.4.5.6   |
    And the admin picks the date "${holidayDateCenter}"
    And the admin selects the first "machineSpecId" option
    And the admin enters the following values:
      | publicKey     | ${publicKey}     |
      | signPublicKey | ${signPublicKey} |
    And the admin selects the first "zone" option if it is available
    And the admin selects the first "regCenterId" option
    And the admin submits the new record
    And the admin filters the list by "name" "${data}"
    And the admin changes "name" of the first record to "${data}1"
    And the admin filters the list by "name" "${data}1"
    And the admin activates the first record
    And the admin changes "name" of the first record to "${data}2"
    And the admin filters the list by "name" "${data}2"
    And the admin deactivates the first record
    And the admin decommissions the first record
