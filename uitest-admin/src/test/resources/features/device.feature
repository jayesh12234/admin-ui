@regression @resources @deviceCRUD
Feature: Devices

  Background:
    Given the admin user is logged in to the admin portal

  Scenario: Device CRUD
    When the admin opens the "Devices" resource page
    And the admin clicks "Create Device"
    And the admin enters the following values:
      | name         | ${data} |
      | serialNumber | ${data} |
      | macAddress   | 1.1234  |
      | ipAddress    | 2.2345  |
    And the admin picks the date "${validityDate}"
    And the admin selects the first "deviceSpecId" option
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
