# Adobe Branch SDK Extension change log

- 3.0.3
  * Sept 2, 2025
  * Update Branch Android SDK to 5.20.0
  * Updated compile dependency versions
  
- 3.0.2
  * Dec 16, 2024
  * Update Branch Android SDK to 5.15.0
  * Fix to include compile dependencies

- 3.0.1
  * Nov 19, 2024
  * Fix for missing .aar in 3.0.0

- 3.0.0
  * Nov 18, 2024
  * Update Branch Android SDK to 5.14.0
  * Updated AEP SDKs to v3

- v2.1.0
  * April 1, 2024
  * Update Branch Android SDK to 5.11.0
  * Updated AEP SDKs
  
- v2.0.0
  * June 20, 2023
  * Update Branch Android SDK to 5.4.2
  * Updated from ACPCore to AEPCore

- v1.4.0
  * January 12, 2023
  * Update Branch Android SDK to 5.2.7

- v1.3.5
  * June 2, 2022
  * Update Branch Android SDK to 5.1.5

- v1.3.4
  * May 20, 2022
  * Update Branch Android SDK to 5.1.4
  * Changed plugin_name to "AdobeBranchExtension"

- v1.3.3
  * February 10, 2022
  * Update Branch Android SDK to 5.1.0
  * Added api to add device data to shared state of tracked events.

- v1.3.2
  * April 29, 2021
  * Update Branch Android SDK to 5.0.8

- v1.3.1
  * April 23, 2021
  * Update Branch Android SDK to 5.0.7
  * Add api to filter events by name

- v1.3.0
  * _*Master Release*_ - September 14, 2020
  * Pick up Adobe IDs more intelligently
  * Treat Adobe state and action names as Branch event names, instead of lumping them under one name, "Analytics Track".

- v1.2.1
  * _*Master Release*_ - February 21, 2020
  * Fix session initialization from recent apps list
  * Fix intra-app linking from NonLauncherActivities when reInitSession is used

- v1.2.0
  * _*Master Release*_ - February 18, 2020
  * Minor: delay session initialization to allow enough time to collect Adobe IDs
  * Clean up extension registration flow

- v1.1.3
  * _*Master Release*_ - November 19, 2019
  * Patch: automatically pick up all Adobe IDs and pass it to Branch
  
- v1.1.2
  * _*Master Release*_ - November 19, 2019
  * Automatically pick up Adobe ID and pass it to Branch
