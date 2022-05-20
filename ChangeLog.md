# Adobe Branch SDK Extension change log

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
