@regression @bulkupload @bulkUploadCRUD
Feature: Master data bulk upload

  Background:
    Given the admin user is logged in to the admin portal

  Scenario Outline: Bulk upload <table> insert update and delete
    When the admin opens the master data bulk upload page
    And the admin uploads the "<table>" file with the "Insert" operation
    Then the upload transaction should complete
    When the admin uploads the "<table>" file with the "Update" operation
    Then the upload transaction should complete
    When the admin uploads the "<table>" file with the "Delete" operation
    Then the upload transaction should complete

    Examples:
      | table          |
      | device_type    |
      | machine_master |
